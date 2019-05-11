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
    private  Map<Ingredient, Number> ingredientsStock = new ConcurrentHashMap<>();
    private  Map<Dish, Number> dishesStock = new ConcurrentHashMap<>();
    private  List<Dish> dishes;
    private  List<Ingredient> ingredients;

    public StockManagement() {

    }


    public static void setRestockDishesEnabled(boolean restockDishesEnabled) {
        StockManagement.restockDishesEnabled = restockDishesEnabled;
    }

    public boolean isRestockDishesEnabled() {
        return restockDishesEnabled;
    }

    public void setRestockIngredientsEnabled(boolean restockIngredientsEnabled) {
        StockManagement.restockIngredientsEnabled = restockIngredientsEnabled;
    }

    public static boolean isRestockIngredientsEnabled() {
        return restockIngredientsEnabled;
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


        ingredients = new ArrayList<Ingredient>(ingredientsSet);
        return ingredients;
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


    public  void setDishesStock(Map<Dish, Number> dishes) {
        dishesStock = dishes;
    }

    public void setDishes(List<Dish> dishesList) {
        dishes = dishesList;
    }


    private void writeObject(ObjectOutputStream oos) throws IOException {
        ArrayList<Dish> serialisedDish = new ArrayList<>(getDishes());
        ArrayList<Ingredient> serialisedIngredients = (ArrayList<Ingredient>) getIngredients();
        ConcurrentHashMap ingredientNumberHashMap = (ConcurrentHashMap) getIngredientsStock();
        ConcurrentHashMap<Dish,Number> dishNumberHashMap = (ConcurrentHashMap<Dish, Number>) getDishesStock();

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
        ArrayList<Object> deserializedData = new ArrayList<>();
        deserializedData = (ArrayList<Object>) ois.readObject();
        setIngredients((ArrayList<Ingredient>) deserializedData.get(1));
        setIngredientsStock((ConcurrentHashMap<Ingredient,Number>)deserializedData.get(2));
        setDishes((ArrayList<Dish>) deserializedData.get(0) );
        setDishesStock((ConcurrentHashMap<Dish, Number>) deserializedData.get(3));
    }

    private void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

}
