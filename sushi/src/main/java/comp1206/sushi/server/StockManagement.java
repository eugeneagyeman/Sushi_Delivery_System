package comp1206.sushi.server;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Integer.valueOf;

public class StockManagement {
    private final static Lock dishesLock = new ReentrantLock(true);
    private final static Lock ingredientsLock = new ReentrantLock(true);
    static boolean restockIngredientsEnabled;
    static boolean restockDishesEnabled = true;
    private static Map<Ingredient, Number> ingredientsStock = new ConcurrentHashMap<>();
    private static Map<Dish, Number> getDishesStock = new ConcurrentHashMap<>();
    private List<Dish> dishes;
    private List<Ingredient> ingredients;

    public static Map<Ingredient, Number> getIngredientsStock() {
        ingredientsLock.tryLock();

        try {
            return ingredientsStock;
        } finally {
            ingredientsLock.unlock();

        }
    }


    public static Map<Dish, Number> getDishesStock() {
        dishesLock.lock();
        try {
            return getDishesStock;
        } finally {
            dishesLock.unlock();
        }
    }

    public static void restockIngredient(Ingredient ingredient) {
        int restockThreshold = ingredient.getRestockThreshold().intValue();
        int restockAmount = ingredient.getRestockAmount().intValue();

        ingredientsLock.lock();
        try {
            int quantity = ingredientsStock.get(ingredient).intValue();

            if (quantity < restockThreshold) {
                ingredientsStock.replace(ingredient, quantity + restockAmount);
            }
        } finally {
            ingredientsLock.unlock();
        }

    }

    public static boolean isRestockIngredientsEnabled() {
        return restockIngredientsEnabled;
    }

    public static void setRestockIngredientsEnabled(boolean restockIngredientsEnabled) {
        StockManagement.restockIngredientsEnabled = restockIngredientsEnabled;
    }

    public static boolean isRestockDishesEnabled() {
        return restockDishesEnabled;
    }

    public static void setRestockDishesEnabled(boolean restockDishesEnabled) {
        StockManagement.restockDishesEnabled = restockDishesEnabled;
    }

    private void ingredientsTracker() {

        for (Ingredient existingIngredient : getIngredients()) {
            System.out.println("Ingredient: " + existingIngredient.getName()
                    + " Quantity:" + ingredientsStock.get(existingIngredient));
        }
    }

    private void dishesTracker() {

        for (Dish existingDish : getDishes()) {
            System.out.println("Dish: " + existingDish.getName()
                    + " Quantity: " + getDishesStock.get(existingDish));
        }
    }

    public List<Dish> getDishes() {
        dishesLock.tryLock();
        try {
            getDishesStock.keySet();
            dishes = new ArrayList<Dish>(getDishesStock.keySet());
        } finally {
            dishesLock.unlock();
        }

        return dishes;
    }

    public Dish getDish(String name) {
        for (Dish dishes : getDishes()) {
            if (dishes.getName().equals(name)) {
                return dishes;
            }
        }
        return null;
    }

    public List<Ingredient> getIngredients() {
        Set<Ingredient> ingredientsSet;
        synchronized (StockManagement.class) {
            ingredientsSet = ingredientsStock.keySet();
        }

        ingredients = Collections.synchronizedList(new ArrayList<Ingredient>(ingredientsSet));
        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> is) {
        ingredientsLock.tryLock();
        try {
            ingredients = is;

        } finally {
            ingredientsLock.unlock();
        }
    }

    void dishIngredientFinder(String itemName, String itemQuantity) {
        for (Dish dish : getDishes()) {
            if (dish.getName().equals(itemName)) {
                StockManagement.getDishesStock().replace(dish, valueOf(itemQuantity));
            }
        }

        for (Ingredient ingredient : getIngredients()) {
            if (ingredient.getName().equals(itemName)) {
                StockManagement.getIngredientsStock().replace(ingredient, valueOf(itemQuantity));
            }
        }
    }


}

class StockChecker extends StockManagement implements Runnable {
    private final BlockingQueue<Dish> queue;

    StockChecker(BlockingQueue<Dish> queue) {
        this.queue = queue;
    }

    @Override
    public synchronized void run() {
        try {
            while (isRestockDishesEnabled()) {
                for (Dish dish : getDishesStock().keySet()) {
                    int quantity = getDishesStock().get(dish).intValue();
                    int restockThreshold = dish.getRestockThreshold().intValue();

                    if (quantity <= restockThreshold) {
                        System.out.println("Putting " + dish.getName() + " in the queue");

                        queue.put(dish);
                        Thread.sleep(1000);
                        notifyAll();

                    }
                }
                Thread.sleep(35000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}