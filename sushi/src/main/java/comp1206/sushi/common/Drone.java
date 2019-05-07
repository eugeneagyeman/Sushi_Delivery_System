package comp1206.sushi.common;

import comp1206.sushi.server.StockManagement;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Drone extends Model implements Runnable, Serializable {
    public static final long serialVersionUID = -1250134517313411885L;
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

    public Drone(Number speed,Postcode restaurantBase) {
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


    public Number getProgress() {
        return progress;
    }

    public synchronized void setProgress(Number progress) {
        this.progress = progress;
        notifyUpdate("progress", this.progress, progress);
    }

    public synchronized void setSpeed(Number speed) {
        this.speed = speed;
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

    public synchronized void grabIngredient(Ingredient ingredient,Postcode source) throws InterruptedException {
        setStatus("Collecting: " + ingredient.getName());
        setSource(source);
        setDestination(ingredient.getSupplier().getPostcode());

        setProgress(0);

        Double distance = ingredient.getSupplier().getDistance().doubleValue();
        Double speed = this.speed.doubleValue();

        travel(distance, speed);
        setStatus("Destination reached... " + ingredient.getName() + "....");
        Thread.sleep(500);
        setDestination(base);
        setSource(ingredient.getSupplier().getPostcode());
        setStatus(ingredient.getName() + " Collected. Returning to base...");

        travel(distance,100.0);
        //travel(distance, speed);
        StockManagement.restockIngredient(ingredient);
        setProgress(0);
        notifyUpdate();

    }

    public synchronized void grabOrder(Order order) throws InterruptedException, UnableToDeliverException {
        setStatus("Preparing to deliver order: " + order.getName());
        setProgress(null);
        Map<Dish, Number> orderContents = order.getContents();
        Map<Dish, Number> dishStock = StockManagement.getDishesStock();

        for (Map.Entry<Dish, Number> entry : orderContents.entrySet()) {
            Dish currentDish = entry.getKey();
            Number orderQty = entry.getValue();
            int stockAmt = dishStock.get(currentDish).intValue();

            if (orderQty.intValue() > stockAmt) {
                setStatus("Cannot deliver order" + order.getName() + "as dishes still need to be made");
                throw new UnableToDeliverException("Cannot deliver order" + order.getName() + "as dishes still need to be made");
            }
        }
        setStatus("Dishes available, getting ready to deliver.");
        orderContents.forEach((key, value) -> {
            dishStock.replace(key, dishStock.get(key).intValue() - value.intValue());
        });

        setStatus("In Transit: Delivering to User:" + order.getUser().getName());
        setProgress(0);
        Double customerDistance = (Double) order.getUser().getPostcode().getDistance();
        Double speed = this.speed.doubleValue();
        travel(customerDistance, speed);
        setStatus("Order delivered...returning to base");
        travel(customerDistance, speed);


    }

    private void travel(Double distance, Double speed) throws InterruptedException {
        Double timeTillDestination = distance / speed;
        Double timeElapsed = (double) 0;
        while (timeElapsed < timeTillDestination) {
            timeElapsed += 1;
            Thread.sleep(1000);
            setProgress(Math.round((timeElapsed / timeTillDestination) * 100));
        }

    }

    //TODO: Order Build...


    @Override
    public synchronized void run() {
        try {
            while (true) {
                if (ingredientQueueInstance.peek() == null && orderQueueInstance.peek() == null) {
                    this.setStatus("Idle");
					wait(3000);
                } else if (ingredientQueueInstance.peek() != null) {
                    Ingredient ingredientToCollect = ingredientQueueInstance.take();
                    grabIngredient(ingredientToCollect,base);
                } else if (orderQueueInstance.peek() != null) {
                    Order orderToDeliver = orderQueueInstance.take();
                    try {
                        grabOrder(orderToDeliver);
                    } catch (UnableToDeliverException e) {
                        orderQueueInstance.add(orderToDeliver);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private class UnableToDeliverException extends Exception {
        public UnableToDeliverException(String message) {
            super(message);
        }
    }
}
