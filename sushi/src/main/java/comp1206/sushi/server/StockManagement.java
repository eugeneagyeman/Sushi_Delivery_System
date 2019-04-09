package comp1206.sushi.server;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.valueOf;

public class StockManagement {
        private static ArrayList<Dish> dishes;
        private static ArrayList<Ingredient> ingredients;

        private static Map<Ingredient, Number> ingredientsStock = new HashMap<>();
        private static Map<Dish, Number> dishesStock = new HashMap<>();

        public static Map<Ingredient, Number> getIngredientsStock() {
            //ingredientsTracker();
            return ingredientsStock;

        }


        public static Map<Dish, Number> getDishesStock() {
            //dishesTracker();
            return dishesStock;
        }

        public static void ingredientsTracker() {

            for (Ingredient existingIngredient : getIngredients()) {
                System.out.println("Ingredient: " + existingIngredient.getName()
                        + " Quantity:" + ingredientsStock.get(existingIngredient) );
            }
        }

        public static void dishesTracker() {

            for (Dish existingDish : getDishes()) {
                System.out.println("Dish: " + existingDish.getName()
                        + " Quantity: " + dishesStock.get(existingDish));
            }
        }

        public static ArrayList<Dish> getDishes() {
            dishes = new ArrayList<Dish>(dishesStock.keySet());
            return dishes;
        }

        public static Dish getDish(String name) {
            for (Dish dishes : getDishes()) {
                if (dishes.getName().equals(name)) {
                    return dishes;
                }
            }
            return null;
        }

        public static void build(Dish dish) {
            //check stock level of dishes
            //done using value of quantity.
            //if there are enough ingredients
            //checked using recipe against ingredients map.
            //check all ingredients in recipe are in the system
            //check quantity is below or equal to current quantity
            //if so then deduct recipe amount from quantity amount.
            //if not then do other dishes, or if not then wait
            //build dish and update key

            int dishQuantity = dishesStock.get(dish).intValue();
            Map<Ingredient,Number> dishrecipe = dish.getRecipe();

            boolean enoughIngredients = true;
            for (Map.Entry<Ingredient, Number> entry : dishrecipe.entrySet()) {
                Ingredient currentIngredient = entry.getKey();
                Number recipeQuantity = entry.getValue();

                int currentIngredientAmount = ingredientsStock.get(currentIngredient).intValue();

                System.out.println("Ingredient: " + currentIngredient.getName() + "\tRecipe: " + dish.getName()
                        + "\tRecipe Quantity: " + recipeQuantity + "\t"
                        + "\t Currently in Stock:" + currentIngredientAmount);

                if (recipeQuantity.intValue() >= currentIngredientAmount) enoughIngredients = false;
            }

            if(enoughIngredients) {
                dishrecipe.forEach((key,value) -> {
                    ingredientsStock.replace(key,ingredientsStock.get(key).intValue()-value.intValue());
                });
                dishesStock.replace(dish,dishQuantity+1);
            } else {
                for(Ingredient ingredient: dishrecipe.keySet())
                restockIngredient(ingredient);
            }


        }

        public static List<Ingredient> getIngredients() {
            ingredients = new ArrayList<Ingredient>(ingredientsStock.keySet());
            return ingredients;
        }

        public static void setIngredients(ArrayList<Ingredient> ingredients) {
            StockManagement.ingredients = ingredients;
        }

        public static void restockIngredient(Ingredient ingredient) {
            int restockThreshold = ingredient.getRestockThreshold().intValue();
            int restockAmount = ingredient.getRestockAmount().intValue();
            int quantity = ingredientsStock.get(ingredient).intValue();

            if(quantity < restockThreshold) {
                ingredientsStock.replace(ingredient,quantity+=restockAmount);
            }
        }

        public static void restockDish(Dish dish) {
            int restockThreshold = dish.getRestockThreshold().intValue();
            int quantity = dishesStock.get(dish).intValue();
            int restockAmount = dish.getRestockAmount().intValue();

            if(quantity <= restockThreshold) {
                dishesStock.replace(dish,quantity+=restockAmount);
            }
        }

        static void dishIngredientFinder(String itemName, String itemQuantity) {
            for (Dish dish : StockManagement.getDishes()) {
                if (dish.getName().equals(itemName)) {
                    StockManagement.getDishesStock().replace(dish,valueOf(itemQuantity));
                }
            }

            for (Ingredient ingredient : StockManagement.getIngredients()) {
                if (ingredient.getName().equals(itemName)) {
                    StockManagement.getIngredientsStock().replace(ingredient,valueOf(itemQuantity));
                }
            }
        }
    }
