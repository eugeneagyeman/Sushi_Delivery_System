package comp1206.sushi.common.StockManagement;

import comp1206.sushi.common.Ingredient;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;

public class IngredientChecker implements Runnable, Serializable {
    private StockManagement stockManagement;
    private final BlockingQueue<Ingredient> ingredientsQueue;

    public IngredientChecker(StockManagement stockManagement, BlockingQueue<Ingredient> queue) {
        this.stockManagement = stockManagement;
        this.ingredientsQueue = queue;
    }

    @Override
    public void run() {
        try {
            while (StockManagement.isRestockIngredientsEnabled()) {
                checkIngredients();
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " thread interrupted");
            Thread.currentThread().interrupt();
        }
    }

    private void checkIngredients() throws InterruptedException {
            if (ingredientsQueue.remainingCapacity()==0) {
                System.out.println("Queue is full " + Thread.currentThread().getName() + " is waiting");
            }
            for (Ingredient ingredient : stockManagement.getIngredientsStock().keySet()) {
                int quantity = stockManagement.getIngredientsStock().get(ingredient).intValue();
                int restockThreshold = ingredient.getRestockThreshold().intValue();

                if (quantity < restockThreshold) {
                    System.out.println("Ingredient Queue: Adding " + ingredient.getName());
                    ingredientsQueue.put(ingredient);
                }
            }
            Thread.sleep(180000);
    }
}
