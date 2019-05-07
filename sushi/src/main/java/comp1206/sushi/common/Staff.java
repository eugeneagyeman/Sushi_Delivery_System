package comp1206.sushi.common;

import comp1206.sushi.server.StockManagement;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Staff extends Model implements Runnable, Serializable {
    public static final long serialVersionUID = 1755448731277423394L;
    private final static Lock dishesLock = new ReentrantLock(true);
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
    //TODO: Check if there is enough ingredients for restockAmount
    //TODO: Implement Fatigue Property
    public synchronized void build(Dish dish) throws InterruptedException, UnableToBuildException {

        Map<Ingredient, Number> dishrecipe = dish.getRecipe();
        boolean enoughIngredients = true;

        Map<Ingredient, Number> ingredientsStock = StockManagement.getIngredientsStock();
        for (Map.Entry<Ingredient, Number> entry : dishrecipe.entrySet()) {
            Ingredient currentIngredient = entry.getKey();
            Number recipeQuantity = entry.getValue();
            int currentIngredientAmount = ingredientsStock.get(currentIngredient).intValue();

            System.out.println("Ingredient: " + currentIngredient.getName() + "\tRecipe: " + dish.getName()
                    + "\tRecipe Quantity: " + recipeQuantity + "\t"
                    + "\t Currently in Stock:" + currentIngredientAmount);

            if (currentIngredientAmount >= recipeQuantity.intValue()) {
                enoughIngredients = false;
            }
        }

        if (enoughIngredients) {
            setStatus("Building: "+dish.getName());
            int randomBuildTime = ThreadLocalRandom.current().nextInt(60000 - 20000) + 20000;
            Thread.sleep(randomBuildTime);

            dishrecipe.forEach((key, value) -> {
                ingredientsStock.replace(key, ingredientsStock.get(key).intValue() - value.intValue());
            });

            Map<Dish, Number> dishesStock = StockManagement.getDishesStock();
            int dishQuantity = dishesStock.get(dish).intValue();
            dishesStock.replace(dish, dishQuantity + 1);

            System.out.println(Thread.currentThread().getName() + " has successfully created: " + dish.getName() + "\n");
            notifyUpdate();
        } else {
            System.out.println("There are not enough ingredients...\n");
            throw new UnableToBuildException("Not enough ingredients. Wait till restock");
            //TODO: To be replaced by the drone implementation. Notify drones to check?
            /*for (Ingredient ingredient : dishrecipe.keySet())
                StockManagement.restockIngredient(ingredient);*/
        }
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

    public synchronized void setStatus(String status) {
        notifyUpdate("status", this.status, status);
        this.status = status;


    }

    public synchronized void setDishBlockingQueue(BlockingQueue<Dish> dishBlockingQueue) {
        this.dishBlockingQueue = dishBlockingQueue;
    }

    @Override
    public synchronized void run() {
        Dish dishDebug = null;

        try {
            while (StockManagement.isRestockDishesEnabled()) {
                if (dishBlockingQueue.peek() != null) {
                    dishDebug = dishBlockingQueue.poll(10, TimeUnit.SECONDS);
                    System.out.println(Thread.currentThread().getName() + " is attempting to build: " + dishDebug.getName());
                    setStatus("Attempting to build: " + dishDebug.getName());


                    build(dishDebug);

                } else {
                    this.setStatus("Idle");
                    wait(3000);
                }
            }


        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (NullPointerException queueNotInitialised) {
            System.out.println("Empty queue...");

        } catch (UnableToBuildException e) {
            dishBlockingQueue.add(dishDebug);
        } catch(NoSuchElementException ignored) {

        }



    }

    private class UnableToBuildException extends Exception {
        private static final long serialVersionUID = 5387102460657662640L;

        UnableToBuildException(String message) {
            super(message);
        }
    }
}
