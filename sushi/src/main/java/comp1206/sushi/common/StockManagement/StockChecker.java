package comp1206.sushi.common.StockManagement;

import comp1206.sushi.common.Dish;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;

public class StockChecker implements Runnable, Serializable {
    private StockManagement stockManagement;
    private final BlockingQueue<Dish> dishesQueue;

    public StockChecker(StockManagement stockManagement, BlockingQueue<Dish> queue) {
        this.stockManagement = stockManagement;
        this.dishesQueue = queue;
    }

    @Override
    public void run() {
        try {
            while (stockManagement.isRestockDishesEnabled()) {
                checkDishes();
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " thread interrupted");
            Thread.currentThread().interrupt();
        } catch(NullPointerException ignored) {

        }
    }

    private void checkDishes() throws InterruptedException, NullPointerException {
        while (dishesQueue.remainingCapacity() == 0) {
            System.out.println("Queue is full " + Thread.currentThread().getName() + " is waiting");
            Thread.sleep(10000);
        }

        for (Dish dish : stockManagement.getDishesStock().keySet()) {

            int quantity = stockManagement.getDishesStock().get(dish).intValue();
            int restockThreshold = dish.getRestockThreshold().intValue();

            if (quantity < restockThreshold) {
                System.out.println("Putting " + dish.getName() + " in the dishesQueue");
                dishesQueue.put(dish);
                Thread.sleep(1000);
            }
        }

    }
}
