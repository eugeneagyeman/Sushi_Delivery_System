package comp1206.sushi.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import comp1206.sushi.common.*;
import comp1206.sushi.server.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client implements ClientInterface {

    private static final Logger logger = LogManager.getLogger("Client");

    public Restaurant restaurant;
	private ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();
	public static ArrayList<User> users = new ArrayList<User>();



	public Client() {
        logger.info("Starting up client...");

        /*Postcode restaurantPostcode = new Postcode("SO17 1BJ");
        restaurant = new Restaurant("Mock Restaurant", restaurantPostcode);

        Postcode postcode1 = addPostcode("SO17 1TJ");
        Postcode postcode2 = addPostcode("SO17 1BX");
        Postcode postcode3 = addPostcode("SO17 2NJ");
        Postcode postcode4 = addPostcode("SO17 1TW");
        Postcode postcode5 = addPostcode("SO17 2LB");

        Supplier supplier1 = addSupplier("Supplier 1", postcode1);
        Supplier supplier2 = addSupplier("Supplier 2", postcode2);
        Supplier supplier3 = addSupplier("Supplier 3", postcode3);

        Ingredient ingredient1 = addIngredient("Ingredient 1", "grams", supplier1, 1, 5, 1);
        Ingredient ingredient2 = addIngredient("Ingredient 2", "grams", supplier2, 1, 5, 1);
        Ingredient ingredient3 = addIngredient("Ingredient 3", "grams", supplier3, 1, 5, 1);

        Dish dish1 = addDish("Dish 1", "Dish 1", 1, 1, 10);
        Dish dish2 = addDish("Dish 2", "Dish 2", 2, 1, 10);
        Dish dish3 = addDish("Dish 3", "Dish 3", 3, 1, 10);

        orders.add(new Order());

        addIngredientToDish(dish1, ingredient1, 1);
        addIngredientToDish(dish1, ingredient2, 2);
        addIngredientToDish(dish2, ingredient2, 3);
        addIngredientToDish(dish2, ingredient3, 1);
        addIngredientToDish(dish3, ingredient1, 2);
        addIngredientToDish(dish3, ingredient3, 1);

        addStaff("Staff 1");
        addStaff("Staff 2");
        addStaff("Staff 3");

        addDrone(1);
        addDrone(2);
        addDrone(3);*/

	}
	
	@Override
	public Restaurant getRestaurant() {
		return this.restaurant;
	}
	
	@Override
	public String getRestaurantName() {
		return this.restaurant.getName();
	}

	@Override
	public Postcode getRestaurantPostcode() {
		return this.restaurant.getLocation();
	}
	
	@Override
	public User register(String username, String password, String address, Postcode postcode) {
		User mockUser = new User(username,password,address,postcode);
		//add to server
		this.users.add(mockUser);
		//update ui
		return mockUser;
	}

	@Override
	public User login(String username, String password) {
		for(User user: users) {
			if(user.getName().equals(username) && user.getPassword().equals(password)) return user;
		}
		return null;
	}

	@Override
	public List<Postcode> getPostcodes() {
		return Server.postcodes;
	}

	@Override
	public List<Dish> getDishes() {
		return Server.dishes;
	}

	@Override
	public String getDishDescription(Dish dish) {
		return dish.getDescription();
	}

	@Override
	public Number getDishPrice(Dish dish) {
		// TODO Auto-generated method stub
		return dish.getPrice();
	}

	@Override
	public Map<Dish, Number> getBasket(User user) {
		// TODO Auto-generated method stub
		return user.getBasket();
	}

	@Override
	public Number getBasketCost(User user) {
		Number sum = 0;

		for(Dish dishInBasket: user.getBasket().keySet()) {
			Number multiply = dishInBasket.getPrice().intValue() * user.getBasket().get(dishInBasket).intValue();
			//sum += multiply;
		}
		return null;
	}

	@Override
	public void addDishToBasket(User user, Dish dish, Number quantity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateDishInBasket(User user, Dish dish, Number quantity) {
		// TODO Auto-generated method stub

	}

	@Override
	public Order checkoutBasket(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearBasket(User user) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Order> getOrders(User user) {
		// TODO Get Orders from list belonging to users which is added to by the server.
		// user.getOrders
		return null;
	}

	@Override
	public boolean isOrderComplete(Order order) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getOrderStatus(Order order) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number getOrderCost(Order order) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cancelOrder(Order order) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
    	this.listeners.add(listener);
	}

	@Override
	public void notifyUpdate() {
		this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
	}

}
