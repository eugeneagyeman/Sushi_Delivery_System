package comp1206.sushi.server;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import comp1206.sushi.common.*;

public interface ServerInterface {

	/**
	 * Get the restaurant object
	 * @return restaurant
	 */
    Restaurant getRestaurant();
	
	/** 
	 * Get the name of the restaurant
	 * @return restaurant name
	 */
    String getRestaurantName();
	
	/**
	 * Get the postcode of the restaurant
	 * @return restaurant postcode
	 */
    Postcode getRestaurantPostcode();
	
	//Configuration
	/**
	 * Given a configuration file, populate the module appropriately
	 * @param filename configuration file to load
	 * @throws FileNotFoundException if unable to load given file
	 */
    void loadConfiguration(String filename) throws FileNotFoundException;
	
	//Stock control
	
	/**
	 * Enable or disable automatic restocking of ingredients. 
	 * When restocking is disabled, no restocking by drones of ingredients should take place.
	 * @param enabled set to true to enable restocking of ingredients, or false to disable.
	 */
    void setRestockingIngredientsEnabled(boolean enabled);
	
	/**
	 * Enable or disable automatic restocking of dishes. 
	 * When restocking is disabled, no restocking by staff of dishes should take place.
	 * @param enabled set to true to enable restocking of dishes, or false to disable.
	 */
    void setRestockingDishesEnabled(boolean enabled);
	
	/**
	 * Set the current stock of a given dish directly
	 * @param dish dish to set the stock 
	 * @param stock stock amount
	 */
    void setStock(Dish dish, Number stock);
	
	/**
	 * Set the current stock of a given ingredient directly
	 * @param ingredient ingredient to set the stock
	 * @param stock stock amount
	 */
    void setStock(Ingredient ingredient, Number stock);
	
	//Dishes
	
	/**
	 * Return the list of current dishes available to order
	 * @return list of dishes
	 */
    List<Dish> getDishes();
	
	/**
	 * Add a new dish to the system
	 * @param name name of dish
	 * @param description description of dish
	 * @param price price of dish
	 * @param restockThreshold minimum threshold to reach before restocking
	 * @param restockAmount amount to restock by
	 * @return newly created dish
	 */
    Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount);
	
	/**
	 * Remove a dish from the system
	 * @param dish dish to remove
	 * @throws UnableToDeleteException if unable to delete a dish
	 */
    void removeDish(Dish dish) throws UnableToDeleteException;
	
	/**
	 * Add an ingredient to a dish with the given quantity. If the ingredient exists already, update with the newly given values.
	 * @param dish dish to edit the recipe of
	 * @param ingredient ingredient to add/update
	 * @param quantity quantity to set. Should update and replace, not add to.
	 */
    void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity);
	
	/**
	 * Remove an ingredient from a dish
	 * @param dish dish to edit the recipe of
	 * @param ingredient ingredient to completely remove
	 */
    void removeIngredientFromDish(Dish dish, Ingredient ingredient);
	
	/**
	 * Set the recipe for a given dish to the suppliest map of ingredients and quantity numbers
	 * @param dish dish to modify the recipe of
	 * @param recipe map of ingredients and quantity numbers to update
	 */
    void setRecipe(Dish dish, Map<Ingredient, Number> recipe);
	
	/**
	 * Set restocking levels for the given dish
	 * @param dish dish to modify the restocking levels of
	 * @param restockThreshold new amount at which to restock
	 * @param restockAmount new amount to restock by when threshold is reached
	 */
    void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount);
	
	/**
	 * Get the restock threshold for a given dish
	 * @param dish dish to query restock threshold of
	 * @return the restock threshold
	 */
    Number getRestockThreshold(Dish dish);
	
	/**
	 * Get the restock amount for a given dish
	 * @param dish dish to query restock amount of
	 * @return the restock amount, which will be restocked on reaching threshold
	 */
    Number getRestockAmount(Dish dish);
	
	/**
	 * Get the recipe of a given dish, by returning a map of ingredients to quantity amounts
	 * @param dish dish to query the recipe of
	 * @return the mapping of ingredients to quantity amounts
	 */
    Map<Ingredient,Number> getRecipe(Dish dish);
	
	/**
	 * Get the current stock levels of dishes
	 * @return map of dish to quantity numbers
	 */
    Map<Dish,Number> getDishStockLevels();
	
	//Ingredients
	
	/**
	 * Get the current list of ingredients in the system
	 * @return a list of all current ingredients
	 */
    List<Ingredient> getIngredients();
	
	/**
	 * Add a new ingredient
	 * @param name name
	 * @param unit unit
	 * @param supplier supplier
	 * @param restockThreshold when amount reaches restockThreshold restock
	 * @param restockAmount when threshold is reached, restock with this amount
	 * @param weight weight of the ingredient
	 * @return new ingredient
	 */
    Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold, Number restockAmount, Number weight);
	
	/**
	 * Remove the given ingredient
	 * @param ingredient ingredient to remove
	 * @throws UnableToDeleteException if unable to remove the ingredient
	 */
    void removeIngredient(Ingredient ingredient) throws UnableToDeleteException;
	
	/**
	 * Set the restock levels of the given ingredient
	 * @param ingredient ingredient to modify the restocking levels of
	 * @param restockThreshold new amount at which to restock
	 * @param restockAmount new amount to restock by when threshold is reached
	 */
    void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount);
	
	/**
	 * Get the restock threshold for a given ingredient
	 * @param ingredient ingredient to query restock threshold of
	 * @return the restock threshold
	 */
    Number getRestockThreshold(Ingredient ingredient);
	
	/**
	 * Get the restock amount for a given ingredient
	 * @param ingredient ingredient to query restock amount of
	 * @return the restock amount, which will be restocked on reaching threshold
	 */
    Number getRestockAmount(Ingredient ingredient);
	
	/**
	 * Get the current stock levels of ingredients
	 * @return map of ingredient to quantity numbers
	 */
    Map<Ingredient,Number> getIngredientStockLevels();
	
	//Suppliers
	
	/**
	 * Get a list of all current suppliers
	 * @return list of suppliers
	 */
    List<Supplier> getSuppliers();
	
	/**
	 * Add a new supplier
	 * @param name name of supplier
	 * @param location postcode of supplier
	 * @return newly created supplier
	 */
    Supplier addSupplier(String name, Postcode location);
	
	/**
	 * Remove the given supplier
	 * @param supplier supplier to remove
	 * @throws UnableToDeleteException if unable to remove this supplier
	 */
    void removeSupplier(Supplier supplier) throws UnableToDeleteException;
	
	/**
	 * Get the distance of a given supplier
	 * @param supplier supplier to query
	 * @return distance from the restaurant
	 */
    Number getSupplierDistance(Supplier supplier);
	
	//Drones
	
	/**
	 * Get a list of all the drones in the system
	 * @return list of drones
	 */
    List<Drone> getDrones();
	
	/**
	 * Add a new drone to the system with the given speed
	 * @param speed speed of drone
	 * @return the newly created drone
	 */
    Drone addDrone(Number speed);
	
	/**
	 * Remove a given drone from the system
	 * @param drone drone to remove
	 * @throws UnableToDeleteException if unable to remove the drone
	 */
    void removeDrone(Drone drone) throws UnableToDeleteException;
	
	/**
	 * Get the speed of a given drone
	 * @param drone drone to query
	 * @return the speed of the drone
	 */
    Number getDroneSpeed(Drone drone);
	
	/**
	 * Get the source of a drone journey - it's previous or current location
	 * @param drone source postcode
	 * @return source postcode, where drone is or is travelling from
	 */
    Postcode getDroneSource(Drone drone);
	
	/**
	 * Get the destination of a drone journey
	 * @param drone destination postcode
	 * @return destination postcode - where drone is heading to, or null if not in motion
	 */
    Postcode getDroneDestination(Drone drone);
	
	/**
	 * Get the current progress as a percentage of 0 to 100 of a given drone
	 * @param drone drone to check progress of
	 * @return null if not travelling, or a number 0-100 of progress towards destination
	 */
    Number getDroneProgress(Drone drone);
	
	/**
	 * Get the text status of a given drone and what it is currently doing
	 * @param drone drone to query
	 * @return a text description of the current status of the drone
	 */
    String getDroneStatus(Drone drone);
	
	//Staff
	
	/**
	 * Get the current staff in the system
	 * @return list of staff
	 */
    List<Staff> getStaff();
	
	/**
	 * Add a new staff member to the system with the given name
	 * @param name name of staff member
	 * @return newly created staff member
	 */
    Staff addStaff(String name);
	
	/**
	 * Remove a staff member from the system
	 * @param staff staff member to remove
	 * @throws UnableToDeleteException if unable to remove staff
	 */
    void removeStaff(Staff staff) throws UnableToDeleteException;
	
	/**
	 * Get the current status of a given staff member
	 * @param staff member to query
	 * @return a text description of the current status of the staff member
	 */
    String getStaffStatus(Staff staff);
	
	//Orders
	
	/**
	 * Get a list of all the orders in the system
	 * @return list of orders
	 */
    List<Order> getOrders();
	
	/**
	 * Remove an order from the system
	 * @param order order to remove
	 * @throws UnableToDeleteException if unable to remove the order
	 */
    void removeOrder(Order order) throws UnableToDeleteException;
	
	/**
	 * Get the distance of an order based on the delivery location
	 * @param order order to query
	 * @return the distance of the order
	 */
    Number getOrderDistance(Order order);
	
	/**
	 * Return whether an order is complete
	 * @param order order to query
	 * @return true if the order is complete, false otherwise
	 */
    boolean isOrderComplete(Order order);
	
	/**
	 * Get the text status of a given order
	 * @param order order to query
	 * @return a text status of the given order
	 */
    String getOrderStatus(Order order);
	
	/**
	 * Get the current cost of an order
	 * @param order order to query
	 * @return the cost of the order
	 */
    Number getOrderCost(Order order);
	
	//Postcodes
	
	/**
	 * Get all postcodes in the system
	 * @return a list of all postcodes
	 */
    List<Postcode> getPostcodes();
	
	/**
	 * Add a new postcode to the system
	 * @param code postcode string representation
	 * @return added postcode
	 */
    Postcode addPostcode(String code);
	
	/**
	 * Remove a postcode from the system
	 * @param postcode postcode to remove
	 * @throws UnableToDeleteException if unable to remove the postcode
	 */
    void removePostcode(Postcode postcode) throws UnableToDeleteException;
	
	//Users
	
	/**
	 * Get all users in the system
	 * @return list of all users
	 */
    List<User> getUsers();
	
	/**
	 * Remove a user from the system
	 * @param user to remove
	 * @throws UnableToDeleteException if unable to remove the user
	 */
    void removeUser(User user) throws UnableToDeleteException;

	//Listeners
	
	/**
	 * Add a new update listener to the client. This should be notified when any model changes occur that require the UI to update.
	 * @param listener An update listener to be informed of all model changes.
	 */
    void addUpdateListener(UpdateListener listener);
	
	/**
	 * Notify all listeners of a model update. This is primarily used to update the UI after changes have been made.
	 */
    void notifyUpdate();
	
	//Exceptions
	
	//Unable to delete exception
	
	/**
	 * Throw an unable to delete exception with a given message if a deletion cannot occur
	 *
	 */
    class UnableToDeleteException extends Exception {
	    public UnableToDeleteException(String message) {
	        super(message);
	    }
	}
		
}


