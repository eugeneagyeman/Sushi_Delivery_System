package comp1206.sushi.StockManagement;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;

import java.util.concurrent.BlockingQueue;

public class StockChecker extends StockManagement implements Runnable {
    private final BlockingQueue<Dish> dishesQueue;

    public StockChecker(BlockingQueue<Dish> queue) {
        this.dishesQueue = queue;
    }

    @Override
    public void run() {
        try {
            while (isRestockDishesEnabled()) {
                checkDishes();
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " thread interrupted");
            Thread.currentThread().interrupt();
        }
    }

    private void checkDishes() throws InterruptedException {
        synchronized (dishesQueue) {
            while (dishesQueue.remainingCapacity() == 0) {
                System.out.println("Queue is full " + Thread.currentThread().getName() + " is waiting");
                dishesQueue.wait();
            }

            for (Dish dish : getDishesStock().keySet()) {
                int quantity = getDishesStock().get(dish).intValue();
                int restockThreshold = dish.getRestockThreshold().intValue();

                if (quantity < restockThreshold) {
                    System.out.println("Putting " + dish.getName() + " in the dishesQueue");
                    dishesQueue.add(dish);
                    Thread.sleep(1000);
                    dishesQueue.notifyAll();
                }
            }
            //Thread.sleep(30000);
        }

    }

}
