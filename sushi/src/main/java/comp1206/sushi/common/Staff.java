package comp1206.sushi.common;

import comp1206.sushi.server.StockManagement;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static comp1206.sushi.server.StockManagement.getDishes;

public class Staff extends Model implements Runnable {

    private String name;
    private String status;
    private Number fatigue;
    private boolean ready = true;
    private BlockingQueue<Dish> dishBlockingQueue;
    private final static Lock dishesLock = new ReentrantLock(true);


    public Staff(String name) {
        this.setName(name);
        this.setFatigue(0);
        this.setStatus("Idle");

    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Number getFatigue() {
        return fatigue;
    }

    public void setFatigue(Number fatigue) {
        this.fatigue = fatigue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        notifyUpdate("status", this.status, status);
        this.status = status;
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
    public static void build(Dish dish) throws InterruptedException {


        int dishQuantity = StockManagement.getDishesStock().get(dish).intValue();
        Map<Ingredient, Number> dishrecipe = dish.getRecipe();

        boolean enoughIngredients = true;
        System.out.println(Thread.currentThread().getName() + " is searching ingredients...\n");
        for (Map.Entry<Ingredient, Number> entry : dishrecipe.entrySet()) {
            Ingredient currentIngredient = entry.getKey();
            Number recipeQuantity = entry.getValue();

            int currentIngredientAmount = StockManagement.getIngredientsStock().get(currentIngredient).intValue();
            System.out.println("Ingredient: " + currentIngredient.getName() + "\tRecipe: " + dish.getName()
                    + "\tRecipe Quantity: " + recipeQuantity + "\t"
                    + "\t Currently in Stock:" + currentIngredientAmount);


            if (recipeQuantity.intValue() >= currentIngredientAmount) enoughIngredients = false;
        }
        Thread.currentThread().sleep(3000);

        if (enoughIngredients) {
            dishrecipe.forEach((key, value) -> {
                StockManagement.getIngredientsStock().replace(key, StockManagement.getIngredientsStock().get(key).intValue() - value.intValue());
            });
            StockManagement.getDishesStock().replace(dish, dishQuantity + 1);
            System.out.println(Thread.currentThread().getName() + " has successfully created: " + dish.getName() + "\n");

            Random rand = new Random();
            int randomBuildTime = rand.nextInt(5000 - 1000) + 1000;
            Thread.currentThread().sleep(randomBuildTime);

        } else {
            for (Ingredient ingredient : dishrecipe.keySet())
                StockManagement.restockIngredient(ingredient);
        }
    }

    public void setDishBlockingQueue(BlockingQueue<Dish> dishBlockingQueue) {
        this.dishBlockingQueue = dishBlockingQueue;
    }

    @Override
    public void run() {

        try {
            while (StockManagement.isRestockDishesEnabled()) {
                if(dishBlockingQueue.peek() == null) {
                    this.setStatus("Idle");
                } else {
                    System.out.println(Thread.currentThread().getName() + " is attempting to build: " + dishBlockingQueue.peek()+"\n");
                    this.setStatus("Building: " + dishBlockingQueue.peek());
                    build(dishBlockingQueue.take());
                }


                }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
        }
    }


}
