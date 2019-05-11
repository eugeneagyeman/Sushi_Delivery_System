package comp1206.sushi.common;

import comp1206.sushi.common.StockManagement.StockManagement;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Staff extends Model implements Runnable, Serializable {
    public static final long serialVersionUID = 1755448731277423394L;
    private final static Lock ingredientsLock = new ReentrantLock(true);
    private final static Lock dishesLock = new ReentrantLock(true);
    private String name;
    private String status;
    private Number fatigue;
    private BlockingQueue<Dish> dishBlockingQueue;
    private StockManagement stockManagement;
    private volatile boolean exit = false;


    public Staff(String name) {
        this.setName(name);
        this.setFatigue(0);
        this.setStatus("Idle");
    }

    public Staff() {

    }

    public void build(Dish dish) throws InterruptedException {
        Map<Ingredient, Number> dishrecipe = dish.getRecipe();
        boolean enoughIngredients = true;
        Number restockAmount = dish.getRestockAmount().intValue();


        ingredientsLock.lock();
        Map<Ingredient, Number> ingredientsStock = stockManagement.getIngredientsStock();
        for (Map.Entry<Ingredient, Number> entry : dishrecipe.entrySet()) {

            Ingredient currentIngredient = entry.getKey();
            Number recipeQuantity = entry.getValue();
            Number currentIngredientAmount = ingredientsStock.get(currentIngredient).intValue();

            /*System.out.println("Ingredient: " + currentIngredient.getName() + "\tRecipe: " + dish.getName()
                    + "\tRecipe Quantity: " + recipeQuantity + "\t"
                    + "\t Currently in Stock:" + currentIngredientAmount);*/

            if (recipeQuantity.intValue() * restockAmount.intValue() > currentIngredientAmount.intValue()) {
                enoughIngredients = false;
            } else {
                dishrecipe.forEach((key, value) -> {
                    ingredientsStock.replace(key, ingredientsStock.get(key).intValue() - (value.intValue()*restockAmount.intValue()));
                });
            }
        }

        ingredientsLock.unlock();
        if (enoughIngredients) {

            //int randomBuildTime = ThreadLocalRandom.current().nextInt(10000);
            int randomBuildTime = ThreadLocalRandom.current().nextInt(60000 - 20000) + 20000;
            Thread.sleep(randomBuildTime);

            dishesLock.lock();
            Map<Dish, Number> dishesStock = stockManagement.getDishesStock();

            Number dishQuantity = dishesStock.get(dish).intValue();
            dishesStock.replace(dish, dishQuantity.intValue() + restockAmount.intValue());

            System.out.println(Thread.currentThread().getName() + " has successfully created: " + dish.getName() + "\n");
            this.setFatigue(fatigue.intValue()+ThreadLocalRandom.current().nextInt(10));
            dishesLock.unlock();
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
        while(!exit){
        try {
            Thread.sleep(100);
            System.out.println(Thread.currentThread().getName()+" has started");
            if(this.getFatigue().intValue()>=100) {
                this.setStatus("Not at work due to fatigue");
                Thread.sleep(180000);
                this.setFatigue(0);
                this.setStatus("Idle");
            }
            while (stockManagement.isRestockDishesEnabled()) {
                if (dishBlockingQueue.isEmpty()) {
                    setStatus("Idle");
                } else {
                    Dish toBuild = dishBlockingQueue.take();
                    System.out.println(Thread.currentThread().getName() + " is attempting to build: " + toBuild.getName());
                    this.setStatus("Building: " + toBuild.getName());
                    build(toBuild);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NullPointerException uninitialised) {
            uninitialised.printStackTrace();
        }}
    }

    public void stop() {
        this.exit=true;
    }

    public void start() {
        this.exit=false;
        new Thread(this,this.getName()).start();
    }


    public StockManagement getStockManagement() {
        return stockManagement;
    }

    public void setStockManagement(StockManagement stockManagement) {
        this.stockManagement = stockManagement;
    }
}
