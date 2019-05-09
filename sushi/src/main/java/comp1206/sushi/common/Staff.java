package comp1206.sushi.common;

import comp1206.sushi.StockManagement.StockManagement;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Staff extends Model implements Runnable, Serializable {
    public static final long serialVersionUID = 1755448731277423394L;
    private final static Lock ingredientsLock = new ReentrantLock(true);
    private String name;
    private String status;
    private Number fatigue;
    private boolean ready = true;
    private BlockingQueue<Dish> dishBlockingQueue;


    public Staff(String name) {
        this.setName(name);
        this.setFatigue(0);
        this.setStatus("Idle");
    }

    public Staff() {

    }

    //check stock level of dishes
    //done using value of quantity.
    //if there are enough ingredients
    //checked using recipe against ingredients map.
    //check all ingredients in recipe are in the system
    //check quantity is below or equal to current quantity
    //if so then deduct recipe amount from quantity amount.
    //if not then do other dishes, or if not then wait
    //build dish and update key
    public void build(Dish dish) throws InterruptedException {
        //synchronized (dish) {
        Map<Ingredient, Number> dishrecipe = dish.getRecipe();
        boolean enoughIngredients = true;


        ingredientsLock.lock();

        Map<Ingredient, Number> ingredientsStock = StockManagement.getIngredientsStock();
        for (Map.Entry<Ingredient, Number> entry : dishrecipe.entrySet()) {

            Ingredient currentIngredient = entry.getKey();
            Number recipeQuantity = entry.getValue();
            int currentIngredientAmount = ingredientsStock.get(currentIngredient).intValue();

            System.out.println("Ingredient: " + currentIngredient.getName() + "\tRecipe: " + dish.getName()
                    + "\tRecipe Quantity: " + recipeQuantity + "\t"
                    + "\t Currently in Stock:" + currentIngredientAmount);

            if (recipeQuantity.intValue() >= currentIngredientAmount) {
                enoughIngredients = false;
            } else {
                dishrecipe.forEach((key, value) -> {
                    ingredientsStock.replace(key, ingredientsStock.get(key).intValue() - value.intValue());
                });
            }
        }

        ingredientsLock.unlock();
        if (enoughIngredients) {


            int randomBuildTime = ThreadLocalRandom.current().nextInt(60000 - 20000) + 20000;
            Thread.sleep(randomBuildTime);

            Map<Dish, Number> dishesStock = StockManagement.getDishesStock();
            int dishQuantity = dishesStock.get(dish).intValue();
            dishesStock.replace(dish, dishQuantity + 1);

            System.out.println(Thread.currentThread().getName() + " has successfully created: " + dish.getName() + "\n");
        } else {
            System.out.println("There are not enough ingredients...\n");
        }
        //}
    }

    public String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public Number getFatigue() {
        return fatigue;
    }

    public synchronized void setFatigue(Number fatigue) {
        this.fatigue = fatigue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        notifyUpdate("status", this.status, status);
        this.status = status;
    }

    public void setDishBlockingQueue(BlockingQueue<Dish> dishBlockingQueue) {
        this.dishBlockingQueue = dishBlockingQueue;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(300);
            while (StockManagement.isRestockDishesEnabled()) {
                if (dishBlockingQueue.isEmpty()) {
                    setStatus("Idle");
                    //wait();
                } else {
                    Dish toBuild = dishBlockingQueue.take();
                    System.out.println(Thread.currentThread().getName() + " is attempting to build: " + toBuild.getName() + "\n");
                    this.setStatus("Building: " + toBuild.getName());
                    build(toBuild);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NullPointerException uninitialised) {

        }
    }
}
