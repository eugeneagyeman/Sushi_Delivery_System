package comp1206.sushi.common;

import comp1206.sushi.Communication.ServerComms;
import comp1206.sushi.StockManagement.StockManagement;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Drone extends Model implements Runnable, Serializable {
    public static final long serialVersionUID = -1250134517313411885L;
    private final Lock dishCheckLock = new ReentrantLock(true);
    private Number speed;
    private Number progress;
    private Number capacity;
    private Number battery;
    private String status;
    private Postcode base;
    private Postcode source;
    private Postcode destination;
    private BlockingQueue<Ingredient> ingredientQueueInstance;
    private BlockingQueue<Order> orderQueueInstance;
    private ServerComms updateCommunications;
    private volatile boolean exit = false;

    public Drone(Number speed, Postcode restaurantBase, ServerComms updateCommunications) {
        this.updateCommunications = updateCommunications;
        this.setSpeed(speed);
        this.setCapacity(1);
        this.setBattery(100);
        this.setBase(restaurantBase);
    }

    public Drone() {

    }

    public Number getSpeed() {
        return speed;
    }

    public synchronized void setSpeed(Number speed) {
        this.speed = speed;
    }

    public Number getProgress() {
        return progress;
    }

    public synchronized void setProgress(Number progress) {
        this.progress = progress;
        notifyUpdate("progress", this.progress, progress);
    }

    @Override
    public String getName() {
        return "Drone (" + getSpeed() + " speed)";
    }

    public Postcode getBase() {
        return base;
    }

    public void setBase(Postcode base) {
        this.base = base;
    }

    public Postcode getSource() {
        return source;
    }

    public synchronized void setSource(Postcode source) {
        this.source = source;
    }

    public Postcode getDestination() {
        return destination;
    }

    public synchronized void setDestination(Postcode destination) {
        this.destination = destination;
    }

    public Number getCapacity() {
        return capacity;
    }

    public synchronized void setCapacity(Number capacity) {
        this.capacity = capacity;
    }

    public Number getBattery() {
        return battery;
    }

    public synchronized void setBattery(Number battery) {
        this.battery = battery;
    }

    public String getStatus() {
        return status;
    }

    public synchronized void setStatus(String status) {
        notifyUpdate("status", this.status, status);
        this.status = status;
    }

    public synchronized void setQueue(BlockingQueue<Ingredient> ingredientQueue, BlockingQueue orderQueue) {
        this.ingredientQueueInstance = ingredientQueue;
        this.orderQueueInstance = orderQueue;
    }

    public void grabIngredient(Ingredient ingredient, Postcode source) throws InterruptedException {
        setStatus("Collecting: " + ingredient.getName());
        setSource(source);
        setDestination(ingredient.getSupplier().getPostcode());

        setProgress(0);

        Long distance = ingredient.getSupplier().getDistance().longValue();
        Long speed = this.speed.longValue();

        travel(distance, speed);
        setStatus("Destination reached... " + ingredient.getName() + "....");
        Thread.sleep(500);
        setDestination(base);
        setSource(ingredient.getSupplier().getPostcode());
        setStatus(ingredient.getName() + " Collected. Returning to base...");
        travel(distance, speed);
        synchronized (ingredient) {
            StockManagement.restockIngredient(ingredient);
        }
        setProgress(0);
        notifyUpdate();

    }


    public void grabOrder(Order order) throws InterruptedException, UnableToDeliverException {
        setProgress(null);
        setStatus("Preparing to deliver order: " + order.getName());
        try {
            updateCommunications.sendMsg(String.format("%d:%s", order.getOrderID(), order.getStatus()), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<Dish, Number> orderContents = order.getContents();

        dishCheckLock.lock();
        Map<Dish, Number> dishStock = StockManagement.getDishesStock();

        for (Map.Entry<Dish, Number> entry : orderContents.entrySet()) {
            Dish currentDish = entry.getKey();
            Number orderQty = entry.getValue();
            int stockAmt = dishStock.get(currentDish).intValue();

            if (orderQty.intValue() > stockAmt) {
                setStatus("Cannot deliver order" + order.getName() + "as dishes still need to be made");
                try {
                    updateCommunications.sendMsg(String.format("%d:%s", order.getOrderID(), order.getStatus()), false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                throw new UnableToDeliverException("Cannot deliver order" + order.getName() + "as dishes still need to be made");
            } else {
                setStatus("Dishes available, getting ready to deliver.");
                try {
                    updateCommunications.sendMsg(String.format("%d:%s", order.getOrderID(), order.getStatus()), false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                orderContents.forEach((key, value) -> {
                    dishStock.replace(key, dishStock.get(key).intValue() - value.intValue());
                });
            }
        }

        dishCheckLock.unlock();

        setStatus("In Transit: Delivering to:" + order.getUser().getName());
        try {
            updateCommunications.sendMsg(String.format("%d:%s", order.getOrderID(), order.getStatus()), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setProgress(0);
        Long customerDistance = order.getUser().getPostcode().getDistance().longValue();
        Long speed = this.speed.longValue();
        travel(customerDistance, speed);
        setStatus("Order delivered...returning to base");
        travel(customerDistance, speed);


    }

    private void travel(Long distance, Long speed) throws InterruptedException {
        long timeTillDestination = distance / speed;
        double timeElapsed = (double) 0;

        while (timeElapsed < timeTillDestination) {
            timeElapsed += 1;
            Thread.sleep(1000);
            setProgress(Math.round((timeElapsed / timeTillDestination) * 100));
        }

    }

    //TODO: Order Build...


    @Override
    public void run() {
        while (!exit) {
            try {
                while (true) {
                    if (ingredientQueueInstance.peek() == null && orderQueueInstance.peek() == null) {
                        this.setStatus("Idle");
                        //wait(3000);
                    } else if (ingredientQueueInstance.peek() != null) {
                        Ingredient ingredientToCollect = ingredientQueueInstance.take();
                        grabIngredient(ingredientToCollect, base);
                    } else if (orderQueueInstance.peek() != null) {
                        Order orderToDeliver = orderQueueInstance.take();
                        try {
                            grabOrder(orderToDeliver);
                        } catch (UnableToDeliverException e) {
                            System.out.println("Couldn't deliver");
                            orderQueueInstance.add(orderToDeliver);
                            return;
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NullPointerException notinitiliased) {

            }

        }
    }

    public void stop() {
        this.exit = true;
    }

    private class UnableToDeliverException extends Exception {
        public UnableToDeliverException(String message) {
            super(message);
        }
    }
}
