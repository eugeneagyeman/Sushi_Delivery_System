package comp1206.sushi.common.StockManagement;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Integer.valueOf;

public class StockManagement implements Serializable {
    private final static Lock dishesLock = new ReentrantLock(true);
    private final static Lock ingredientsLock = new ReentrantLock(true);
    static boolean restockIngredientsEnabled = true;
    static boolean restockDishesEnabled = true;
    private  volatile Map<Ingredient, Number> ingredientsStock = new ConcurrentHashMap<>();
    private  volatile Map<Dish, Number> dishesStock = new ConcurrentHashMap<>();
    private  volatile List<Dish> dishes;
    private  volatile List<Ingredient> ingredients;


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

    public void setIngredientsStock(Map<Ingredient, Number> ingredients) {
        ingredientsStock = ingredients;
    }

    public void restockIngredient(Ingredient ingredient) throws NullPointerException{
        int restockThreshold = ingredient.getRestockThreshold().intValue();
        int restockAmount = ingredient.getRestockAmount().intValue();

        ingredientsLock.lock();
        try {
            int quantity = ingredientsStock.get(ingredient).intValue();

            if (quantity < restockThreshold) {
                ingredientsStock.replace(ingredient, quantity + restockAmount);
            }
        } catch (NullPointerException e) {

        } finally
        {
            ingredientsLock.unlock();
        }

    }

    public Map<Dish, Number> getDishesStock() {
        dishesLock.lock();
        try {
            return dishesStock;
        } finally {
            dishesLock.unlock();
        }
    }

    public Map<Ingredient, Number> getIngredientsStock() {
        ingredientsLock.lock();
        try {
            return ingredientsStock;
        } finally {
            ingredientsLock.unlock();

        }
    }

    public List<Dish> getDishes() {
        dishesLock.lock();
        try {
            dishesStock.keySet();
            dishes = new ArrayList<Dish>(dishesStock.keySet());
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

    public synchronized  List<Ingredient> getIngredients() {
        Set<Ingredient> ingredientsSet;
            ingredientsSet = ingredientsStock.keySet();


        ingredients = Collections.synchronizedList(new ArrayList<Ingredient>(ingredientsSet));
        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> is) {
        ingredientsLock.lock();
        try {
            ingredients = is;

        } finally {
            ingredientsLock.unlock();
        }
    }

    public void dishIngredientFinder(String itemName, String itemQuantity) {
        for (Dish dish : getDishes()) {
            if (dish.getName().equals(itemName)) {
                getDishesStock().replace(dish, valueOf(itemQuantity));
            }
        }

        for (Ingredient ingredient : getIngredients()) {
            if (ingredient.getName().equals(itemName)) {
                getIngredientsStock().replace(ingredient, valueOf(itemQuantity));
            }
        }
    }


    public static class IngredientChecker implements Runnable, Serializable {
        private final BlockingQueue<Ingredient> ingredientsQueue;

        public IngredientChecker(BlockingQueue<Ingredient> queue) {
            this.ingredientsQueue = queue;
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
                if (ingredientsQueue.remainingCapacity()==0) {
                    System.out.println("Queue is full " + Thread.currentThread().getName() + " is waiting");
                }
                for (Ingredient ingredient : new StockManagement().getIngredientsStock().keySet()) {
                    int quantity = new StockManagement().getIngredientsStock().get(ingredient).intValue();
                    int restockThreshold = ingredient.getRestockThreshold().intValue();

                    if (quantity < restockThreshold) {
                        System.out.println("Ingredient Queue: Adding " + ingredient.getName());
                        ingredientsQueue.put(ingredient);
                    }
                }
                Thread.sleep(180000);
        }
    }

    public static class StockChecker implements Runnable, Serializable {
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
            } catch(NullPointerException ignored) {

            }
        }

        private void checkDishes() throws InterruptedException, NullPointerException {
            while (dishesQueue.remainingCapacity() == 0) {
                System.out.println("Queue is full " + Thread.currentThread().getName() + " is waiting");
                Thread.sleep(10000);
            }

            for (Dish dish : new StockManagement().getDishesStock().keySet()) {

                int quantity = new StockManagement().getDishesStock().get(dish).intValue();
                int restockThreshold = dish.getRestockThreshold().intValue();

                if (quantity < restockThreshold) {
                    System.out.println("Putting " + dish.getName() + " in the dishesQueue");
                    dishesQueue.put(dish);
                    Thread.sleep(1000);
                }
            }

        }
    }

    public  void setDishesStock(Map<Dish, Number> dishes) {
        dishesStock = dishes;
    }

    public void setDishes(List<Dish> dishesList) {
        dishes = dishesList;
    }


    private void writeObject(ObjectOutputStream oos) throws IOException {
        ArrayList<Dish> serialisedDish = new ArrayList<>(getDishes());
        ArrayList<Ingredient> serialisedIngredients = new ArrayList<>(new StockManagement().getIngredients());
        ConcurrentHashMap ingredientNumberHashMap = new ConcurrentHashMap<>(new StockManagement().getIngredientsStock());
        ConcurrentHashMap<Dish,Number> dishNumberHashMap = new ConcurrentHashMap<>(new StockManagement().getDishesStock());

        ArrayList<Object> stockData = new ArrayList<>();
        stockData.add(
                serialisedDish);
        stockData.add(
                serialisedIngredients);
        stockData.add(
                ingredientNumberHashMap);
        stockData.add(
                dishNumberHashMap);

        oos.writeObject(stockData);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ArrayList<Object> deserialisedData = new ArrayList<>();
        deserialisedData = (ArrayList<Object>) ois.readObject();
        setIngredients((ArrayList<Ingredient>) deserialisedData.get(1));
        setIngredientsStock((ConcurrentHashMap<Ingredient,Number>)deserialisedData.get(2));
        setDishes((ArrayList<Dish>) deserialisedData.get(0) );
        setDishesStock((ConcurrentHashMap<Dish, Number>) deserialisedData.get(3));
    }

}
