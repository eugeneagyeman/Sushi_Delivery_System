package comp1206.sushi.client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import comp1206.sushi.Communication.ClientComms;
import comp1206.sushi.Communication.ServerComms;
import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client implements ClientInterface {

    private static final Logger logger = LogManager.getLogger("Client");

    public Restaurant restaurant;

    public static ArrayList<User> users = new ArrayList<User>();
    public static ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
    public static ArrayList<Dish> dishes = new ArrayList<Dish>();
    private ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();
    ClientComms comms;
    String id;


    public Client() {
        logger.info("Starting up client...");

        Postcode restaurantPostcode = new Postcode("SO17 1BJ");
        restaurant = new Restaurant("Mock Restaurant", restaurantPostcode);

        /*Postcode postcode1 = new Postcode("SO17 1TJ");
        Postcode postcode2 = new Postcode("SO17 1BX");
        Postcode postcode3 = new Postcode("SO17 2NJ");
        Postcode postcode4 = new Postcode("SO17 1TW");
        Postcode postcode5 = new Postcode("SO17 2LB");

        postcodes.add(postcode1);
        postcodes.add(postcode2);
        postcodes.add(postcode3);
        postcodes.add(postcode4);
        postcodes.add(postcode5);*/

        comms = new ClientComms(this);

        /*Dish dish1 = new Dish("Dish 1", "Dish 1", 1, 1, 10);
        Dish dish2 = new Dish("Dish 2", "Dish 2", 2, 1, 10);
        Dish dish3 = new Dish("Dish 3", "Dish 3", 3, 1, 10);

        dishes.add(dish1);
        dishes.add(dish2);
        dishes.add(dish3);*/




        /*
        Supplier supplier1 = addSupplier("Supplier 1", postcode1);
        Supplier supplier2 = addSupplier("Supplier 2", postcode2);
        Supplier supplier3 = addSupplier("Supplier 3", postcode3);

        Ingredient ingredient1 = addIngredient("Ingredient 1", "grams", supplier1, 1, 5, 1);
        Ingredient ingredient2 = addIngredient("Ingredient 2", "grams", supplier2, 1, 5, 1);
        Ingredient ingredient3 = addIngredient("Ingredient 3", "grams", supplier3, 1, 5, 1);



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

    public static void setUsers(ArrayList<User> users) {
        Client.users = users;
    }

    public static void setPostcodes(ArrayList<Postcode> postcodes) {
        Client.postcodes = postcodes;
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
        User mockUser = new User(username, password, address, postcode);
        //add to server
        users.add(mockUser);
        try {
            comms.sendMsg(mockUser);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mockUser;
    }

    @Override
    public User login(String username, String password) {
        for (User user : users) {
            if (user.getName().equals(username) && user.getPassword().equals(password)) return user;
        }
        return null;
    }

    @Override
    public List<Postcode> getPostcodes() {
        return postcodes;
    }

    public void addPostcode(Postcode postcode) {
        getPostcodes().add(postcode);
        this.notifyUpdate();
    }

    @Override
    public List<Dish> getDishes() {
        return dishes;
    }

    public void addDish(Dish dish) {
        getDishes().add(dish);
        this.notifyUpdate();
    }

    public void setDishes(ArrayList<Dish> dishes) {
        Client.dishes = dishes;
    }


    @Override
    public String getDishDescription(Dish dish) {
        return dish.getDescription();
    }

    @Override
    public Number getDishPrice(Dish dish) {
        return dish.getPrice();
    }

    @Override
    public Map<Dish, Number> getBasket(User user) {
        return user.getBasket();
    }

    @Override
    public Number getBasketCost(User user) {
        Double sum = Double.valueOf(0);

        for (Dish dishInBasket : user.getBasket().keySet()) {
            Number multiply = dishInBasket.getPrice().doubleValue() * user.getBasket().get(dishInBasket).doubleValue();
            sum += multiply.doubleValue();
        }
        return sum;
    }

    @Override
    public void addDishToBasket(User user, Dish dish, Number quantity) {
        user.getBasket().put(dish, quantity);

    }

    @Override
    public void updateDishInBasket(User user, Dish dish, Number quantity) {
        //get old quantity of dish
        Number oldQuantity = user.getBasket().get(dish);

        user.getBasket().replace(dish, oldQuantity, quantity);
        this.notifyUpdate();
        //UpdateEvent updateBasket = new UpdateEvent(user,"Basket",oldQuantity,quantity);
        //this.notifyUpdate(updateBasket);
    }

    @Override
    public Order checkoutBasket(User user) {
        //create a new order
        Order checkoutOrder = new Order(user);

        //add basket and cost to order
        checkoutOrder.setContents(user.getBasket());
        checkoutOrder.setCost(getBasketCost(user));

        //add to list of checkouts
        user.getOrders().add(checkoutOrder);

        //clear basket for next order
        user.getBasket().clear();

        //notify server
        try {
            comms.sendMsg(checkoutOrder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.notifyUpdate();
        return checkoutOrder;
    }

    @Override
    public void clearBasket(User user) {
        user.getBasket().clear();
        this.notifyUpdate();
    }

    @Override
    public List<Order> getOrders(User user) {
        return user.getOrders();
    }

    @Override
    public boolean isOrderComplete(Order order) {
        return false;
    }

    @Override
    public String getOrderStatus(Order order) {
        return order.getStatus();
    }

    @Override
    public Number getOrderCost(Order order) {
        return order.getCost();
    }

    @Override
    public void cancelOrder(Order order) {
        order.setStatus("Cancelled");

        //int index = order.getUser().getOrders().indexOf(order);
        //order.getUser().getOrders().remove(index);

        this.notifyUpdate();

    }

    @Override
    public void addUpdateListener(UpdateListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void notifyUpdate() {
        this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
    }

    public List<User> getUsers() {
        return this.users;
    }

//	public void notifyUpdate(UpdateEvent updateEvent){
//		this.listeners.forEach(listener -> listener.updated(updateEvent));
//	}



}