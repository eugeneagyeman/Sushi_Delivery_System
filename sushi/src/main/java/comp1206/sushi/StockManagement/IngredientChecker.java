package comp1206.sushi.StockManagement;

import comp1206.sushi.common.Ingredient;
import comp1206.sushi.common.Order;

import java.util.concurrent.BlockingQueue;

public class IngredientChecker extends StockManagement implements Runnable {
    private final BlockingQueue<Ingredient> ingredientsQueue;
    private final BlockingQueue<Order> orderQueue;

    public IngredientChecker(BlockingQueue<Ingredient> queue, BlockingQueue<Order> orders) {
        this.ingredientsQueue = queue;
        this.orderQueue = orders;
    }

    @Override
    public void run() {
        try {
            while (isRestockIngredientsEnabled()) {
                checkIngredients();
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " thread interrupted");
            Thread.currentThread().interrupt();
        }
    }

    private void checkIngredients() throws InterruptedException {
        synchronized (ingredientsQueue) {
            while(ingredientsQueue.remainingCapacity()==0) {
                System.out.println("Queue is full " + Thread.currentThread().getName() + " is waiting");
                ingredientsQueue.wait();
            }
            for (Ingredient ingredient : getIngredientsStock().keySet()) {
                int quantity = getIngredientsStock().get(ingredient).intValue();
                int restockThreshold = ingredient.getRestockThreshold().intValue();

                if (quantity < restockThreshold) {
                    System.out.println("Ingredient Queue: Adding " + ingredient.getName());
                    ingredientsQueue.add(ingredient);
                    ingredientsQueue.notifyAll();
                }
            }
            Thread.sleep(60000);

        }
    }
}
