package comp1206.sushi.server;

import comp1206.sushi.Communication.ClientComms;
import comp1206.sushi.Communication.ServerComms;
import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.valueOf;

public class Server implements ServerInterface {

    private static final Logger logger = LogManager.getLogger("Server");
    private static final ArrayList<Order> orders = new ArrayList<Order>();
    private static final ArrayList<User> users = new ArrayList<User>();
    private static final ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
    private static final ArrayList<Drone> drones = new ArrayList<Drone>();
    private static final ArrayList<Staff> staff = new ArrayList<Staff>();
    private static final ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
    private static final ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();
    private static Restaurant restaurant;
    private Lock taskLock = new ReentrantLock();
    private StockManagement stockManagement;
    private ServerComms serverComms;


    public Server() {
        logger.info("Starting up server...");
        try {
            serverComms = new ServerComms(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stockManagement = new StockManagement();
        Postcode restaurantPostcode = new Postcode("SO17 1BJ");
        restaurant = new Restaurant("Mock Restaurant", restaurantPostcode);

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void init() throws IOException {


        loadConfiguration("Configuration.txt");
        BlockingQueue<Dish> serverQueue = new LinkedBlockingQueue<>(10);
        new Thread(new StockChecker(serverQueue), "Stock Checker").start();

        synchronized (taskLock) {
            for (Staff staff : getStaff()) {
                staff.setDishBlockingQueue(serverQueue);
                (new Thread(staff, staff.getName())).start();
            }
        }
    }

    //Restaurant Details
    @Override
    public String getRestaurantName() {
        return restaurant.getName();
    }

    @Override
    public Postcode getRestaurantPostcode() {
        return restaurant.getLocation();
    }

    @Override
    public Restaurant getRestaurant() {
        return restaurant;
    }

    //Dishes
    @Override
    public List<Dish> getDishes() {
        return stockManagement.getDishes();
    }

    @Override
    public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {

        for (Dish existingDishes : getDishStockLevels().keySet()) {
            if (existingDishes.getName().equals(name)) {
                this.notifyUpdate();
                return existingDishes;
            }
        }

        Dish newDish = new Dish(name, description, price, restockThreshold, restockAmount);
        stockManagement.getDishesStock().put(newDish, 0);
        try {
            serverComms.sendMsg(newDish,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.notifyUpdate();
        return newDish;
    }

    public Dish addDish(Dish dishToAdd) {

        for (Dish existingDishes : getDishStockLevels().keySet()) {
            if (existingDishes.getName().equals(dishToAdd.getName())) {
                this.notifyUpdate();
                return existingDishes;
            }
        }
        stockManagement.getDishesStock().put(dishToAdd,0);
        this.notifyUpdate();
        return dishToAdd;
    }

    @Override
    public void removeDish(Dish dish) {
        stockManagement.getDishes().remove(dish);
        this.notifyUpdate();
    }

    @Override
    public Map<Dish, Number> getDishStockLevels() {
        return stockManagement.getDishesStock();
    }

    @Override
    public void setRestockingIngredientsEnabled(boolean enabled) {
        stockManagement.setRestockIngredientsEnabled(enabled);
    }

    @Override
    public void setRestockingDishesEnabled(boolean enabled) {
        stockManagement.setRestockDishesEnabled(enabled);
    }

    @Override
    public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
        if (quantity == valueOf(0)) {
            removeIngredientFromDish(dish, ingredient);
        } else {
            dish.getRecipe().put(ingredient, quantity);
        }
    }

    @Override
    public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
        dish.getRecipe().remove(ingredient);
        this.notifyUpdate();
    }

    @Override
    public Map<Ingredient, Number> getRecipe(Dish dish) {
        return dish.getRecipe();
    }

    @Override
    public Number getRestockThreshold(Dish dish) {
        return dish.getRestockThreshold();
    }

    @Override
    public Number getRestockAmount(Dish dish) {
        return dish.getRestockAmount();
    }

    @Override
    public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
        for (Entry<Ingredient, Number> recipeItem : recipe.entrySet()) {
            addIngredientToDish(dish, recipeItem.getKey(), recipeItem.getValue());
        }
        this.notifyUpdate();
    }

    @Override
    public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
        dish.setRestockThreshold(restockThreshold);
        dish.setRestockAmount(restockAmount);
        this.notifyUpdate();
    }

    @Override
    public void setStock(Dish dish, Number stock) {
        Number oldValue = stockManagement.getDishesStock().get(dish);
        stockManagement.getDishesStock().replace(dish, oldValue.intValue(), stock.intValue());
        this.notifyUpdate();
    }

    //Ingredients
    @Override
    public void setStock(Ingredient ingredient, Number stock) {
        Number oldValue = stockManagement.getDishesStock().get(ingredient);
        stockManagement.getIngredientsStock().replace(ingredient, oldValue.intValue(), stock.intValue());
        this.notifyUpdate();
    }

    @Override
    public List<Ingredient> getIngredients() {
        return stockManagement.getIngredients();
    }

    @Override
    public Ingredient addIngredient(String name, String unit, Supplier supplier,
                                    Number restockThreshold, Number restockAmount, Number weight) {

        for (Ingredient existingIngredient : getIngredientStockLevels().keySet()) {
            if (existingIngredient.getName().equals(name)) {
                this.notifyUpdate();
                return existingIngredient;
            }
        }
        Ingredient mockIngredient = new Ingredient(name, unit, supplier, restockThreshold, restockAmount, weight);
        stockManagement.getIngredientsStock().put(mockIngredient, 0);
        this.notifyUpdate();
        return mockIngredient;
    }

    @Override
    public void removeIngredient(Ingredient ingredient) {
        stockManagement.getIngredientsStock().remove(ingredient);
        this.notifyUpdate();
    }

    @Override
    public Map<Ingredient, Number> getIngredientStockLevels() {
        return stockManagement.getIngredientsStock();
    }

    @Override
    public Number getRestockThreshold(Ingredient ingredient) {
        return ingredient.getRestockThreshold();
    }

    @Override
    public Number getRestockAmount(Ingredient ingredient) {
        return ingredient.getRestockAmount();
    }

    @Override
    public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
        ingredient.setRestockThreshold(restockThreshold);
        ingredient.setRestockAmount(restockAmount);
        this.notifyUpdate();
    }

    //Suppliers
    @Override
    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    @Override
    public Supplier addSupplier(String name, Postcode postcode) {
        Supplier new_supplier = new Supplier(name, postcode);
        suppliers.add(new_supplier);
        return new_supplier;
    }


    @Override
    public void removeSupplier(Supplier supplier) {
        suppliers.remove(supplier);
        this.notifyUpdate();
    }

    @Override
    public Number getSupplierDistance(Supplier supplier) {
        return supplier.getDistance();
    }

    //Drones
    @Override
    public List<Drone> getDrones() {
        return drones;
    }

    @Override
    public Drone addDrone(Number speed) {
        Drone mock = new Drone(speed);
        drones.add(mock);
        return mock;
    }

    @Override
    public void removeDrone(Drone drone) {
        drones.remove(drone);
        this.notifyUpdate();
    }

    @Override
    public Number getDroneSpeed(Drone drone) {
        return drone.getSpeed();
    }

    @Override
    public String getDroneStatus(Drone drone) {
        Random rand = new Random();
        if (rand.nextBoolean()) {
            return "Idle";
        } else {
            return "Flying";
        }
    }

    @Override
    public Postcode getDroneSource(Drone drone) {
        return drone.getSource();
    }

    @Override
    public Postcode getDroneDestination(Drone drone) {
        return drone.getDestination();
    }

    @Override
    public Number getDroneProgress(Drone drone) {
        return drone.getProgress();
    }

    //Staff
    @Override
    public List<Staff> getStaff() {
        return staff;
    }

    @Override
    public Staff addStaff(String name) {
        Staff mock = new Staff(name);
        staff.add(mock);
        new Thread(mock,mock.getName()).start();
        return mock;
    }

    @Override
    public void removeStaff(Staff staff) {
        Server.staff.remove(staff);
        this.notifyUpdate();
    }

    @Override
    public String getStaffStatus(Staff staff) {
        return staff.getStatus();
    }

    //Orders
    @Override
    public List<Order> getOrders() {
        return orders;
    }

    public Order addOrder(String user) {
        for (Order existingOrder : getOrders()) {
            if (existingOrder.getUser().getName().equals(user)) {
                this.notifyUpdate();
                return existingOrder;
            }
        }

        Order mockOrder = new Order(addUser(user));
        orders.add(mockOrder);
        this.notifyUpdate();
        return mockOrder;
    }

    @Override
    public void removeOrder(Order order) {
        orders.remove(order);
        this.notifyUpdate();
    }

    @Override
    public Number getOrderCost(Order order) {
        Random random = new Random();
        return random.nextInt(100);
    }

    @Override
    public Number getOrderDistance(Order order) {
        return order.getDistance();
    }

    @Override
    public boolean isOrderComplete(Order order) {
        return true;
    }

    @Override
    public String getOrderStatus(Order order) {
        Random rand = new Random();
        if (rand.nextBoolean()) {
            return "Complete";
        } else {
            return "Pending";
        }
    }

    //Postcodes
    @Override
    public List<Postcode> getPostcodes() {
        return postcodes;
    }

    @Override
    public Postcode addPostcode(String code) {
        //get current postcodes
        //if matches exisiting one return that one else add it in

        for (Postcode existingPostcode : postcodes) {
            if (existingPostcode.getName().equals(code)) {
                this.notifyUpdate();
                //System.out.println("Existing postcode");
                return existingPostcode;
            }
        }

        Postcode mock = new Postcode(code);
        postcodes.add(mock);
        this.notifyUpdate();
        return mock;


    }

    @Override
    public void removePostcode(Postcode postcode) throws UnableToDeleteException {
        if (!postcodes.contains(postcode)) throw new UnableToDeleteException("Postcode does not exist");
        else {
            postcodes.remove(postcode);
        }
        this.notifyUpdate();
    }

    //Users
    @Override
    public List<User> getUsers() {
        return users;
    }

    @Override
    public void removeUser(User user) {
        users.remove(user);
        this.notifyUpdate();
    }

    private User addUser(String name) {
        for (User existingUsers : users) {
            if (existingUsers.getName().equals(name)) return existingUsers;
        }
        return null;
    }

    //Configuration
    @Override
    public void loadConfiguration(String filename) {
        System.out.println("Loaded configuration: " + filename);
        try {
            new Configuration(filename);

        } catch (IOException e) {
            System.err.println("File not found");
        }

    }

    @Override
    public void addUpdateListener(UpdateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void notifyUpdate() {
        listeners.forEach(listener -> listener.updated(new UpdateEvent()));
    }


    class Configuration {
        private Scanner sc;
        private Matcher matcher;

        Configuration(String filename) throws IOException {
            sc = new Scanner(new FileReader(filename));
            parse();
        }

        void parse() throws IOException {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                switch (line.split(":")[0]) {

                    case "POSTCODE":
                        postcodeParse(line);
                        break;

                    case "RESTAURANT":
                        restaurantParse(line);
                        break;

                    case "SUPPLIER":
                        supplierParse(line);
                        break;

                    case "INGREDIENT":
                        ingredientParse(line);
                        break;

                    case "DISH":
                        dishParse(line);
                        break;

                    case "USER":
                        userParse(line);
                        break;

                    case "ORDER":
                        orderParse(line);
                        break;

                    case "STOCK":
                        stockParse(line);
                        break;

                    case "STAFF":
                        staffParse(line);
                        break;

                    case "DRONE":
                        droneParse(line);
                        break;

                    default:
                        break;
                }
            }
        }

        void postcodeParse(String postcode) {
            Pattern postcodePattern = Pattern.compile("^POSTCODE:(.+)$", Pattern.MULTILINE);
            matcher = postcodePattern.matcher(postcode);

            while (matcher.find()) {
                //System.out.println("Postcode: " + matcher.group(1));
                addPostcode(matcher.group(1));
            }
        }

        void restaurantParse(String restaurants) {
            Pattern restaurantPattern = Pattern.compile("^RESTAURANT:(\\w+\\s*+\\w*+):(.+)$", Pattern.MULTILINE);
            matcher = restaurantPattern.matcher(restaurants);
            while (matcher.find()) {
                /*System.out.println("Restaurant: " + matcher.group(1));
                System.out.println("Restaurant Postcode: " + matcher.group(2));
                System.out.println();*/
                restaurant = new Restaurant(matcher.group(1), getPostcodes().get(0));
            }
        }

        void supplierParse(String supplier) {
            Pattern supplierPattern = Pattern.compile("^SUPPLIER:(.+):(.+)$", Pattern.MULTILINE);
            matcher = supplierPattern.matcher(supplier);

            while (matcher.find()) {
                String parsedSupplier = matcher.group(1);
                String parsedPostcode = matcher.group(2);

                /*System.out.println();
                System.out.println("Supplier: " + parsedSupplier);
                System.out.println("Postcode: " + parsedPostcode);
                System.out.println();*/

                addSupplier(parsedSupplier, addPostcode(parsedPostcode));

            }
        }

        void ingredientParse(String ingredients) {
            Pattern ingredientPattern = Pattern.compile("^INGREDIENT:(\\w+):(\\w+):(\\w.+):([0-9]+):([0-9]+):([0-9]+)");
            matcher = ingredientPattern.matcher(ingredients);

            while (matcher.find()) {
                String name = matcher.group(1);
                String units = matcher.group(2);
                String supplier = matcher.group(3);
                Integer restock_threshold = valueOf(matcher.group(4));
                Integer restock_amount = valueOf(matcher.group(5));
                Integer weight = valueOf(matcher.group(6));

                Supplier supplierFromSys = null;
                for (Supplier existingSuppliers : getSuppliers()) {
                    if (existingSuppliers.getName().equals(supplier)) {
                        supplierFromSys = existingSuppliers;
                        //System.out.println("Existing supplier");
                    }
                }

                /*System.out.println("Ingredient: " + name);
                System.out.println("Units: " + units);
                System.out.println("Supplier: " + supplier);
                System.out.println("Restock Threshold: " + restock_threshold);
                System.out.println("Restock Amount: " + restock_amount);
                System.out.println("Weight: " + weight);
                System.out.println();*/

                addIngredient(name, units, supplierFromSys, restock_threshold, restock_amount, weight);


            }
        }

        void dishParse(String dishes) {
            Pattern dishPattern = Pattern.compile("^DISH:(\\w.+):(\\w.+):([0-9]+):([0-9]+):(\\d+):((\\d*)\\s\\*\\s(\\w+).*)");
            matcher = dishPattern.matcher(dishes);

            while (matcher.find()) {
                /*System.out.println("Dish Name: " + matcher.group(1));
                System.out.println("Description: " + matcher.group(2));
                System.out.println("Price: " + matcher.group(3));
                System.out.println("Restock Threshold: " + matcher.group(4));
                System.out.println("Restock Amount: " + matcher.group(5));
                System.out.println("Ingredients: " + matcher.group(6));
                System.out.println();*/

                String dishName = matcher.group(1);
                String description = matcher.group(2);
                Integer price = valueOf(matcher.group(3));
                Integer restock_threshold = valueOf(matcher.group(4));
                Integer restock_amount = valueOf(matcher.group(5));

                Dish builtDish = new Dish(dishName,description,price,restock_threshold,restock_amount);
                addDish(builtDish);

                Pattern ingredientsDishPattern = Pattern.compile("((\\d+) \\* (\\w+))");
                Matcher ingredientMatcher = ingredientsDishPattern.matcher(matcher.group(6));

                while (ingredientMatcher.find()) {
                   /* System.out.println("Ingredient: " + ingredientMatcher.group(3));
                    System.out.println("Quantity: " + ingredientMatcher.group(2) + "\n");*/

                    String parsedIngredient = ingredientMatcher.group(3);
                    Integer parsedIngredientQuantity = valueOf(ingredientMatcher.group(2));

                    Ingredient ingredientFromSys = null;

                    for (Ingredient ingredient : getIngredients()) {
                        if (ingredient.getName().equals(parsedIngredient)) ingredientFromSys = ingredient;
                    }

                    addIngredientToDish(builtDish, ingredientFromSys, parsedIngredientQuantity);
                }

            }
        }

        void userParse(String users) {
            Pattern userPattern = Pattern.compile("^USER:(\\w+):(\\w+):(\\w.+):(\\w.+)", Pattern.MULTILINE);
            matcher = userPattern.matcher(users);

            while (matcher.find()) {
                /*System.out.println("User: " + matcher.group(1));
                System.out.println("Password: " + matcher.group(2));
                System.out.println("Location: " + matcher.group(3));
                System.out.println("Postcode: " + matcher.group(4));
                System.out.println();*/

                String username = matcher.group(1);
                String password = matcher.group(2);
                String location = matcher.group(3);
                String postcode = matcher.group(4);

                getUsers().add(new User(username, password, location, addPostcode(postcode)));

            }
        }

        void staffParse(String staff) {
            /*System.out.println("Staff: " + staff.split(":")[1]);
            System.out.println();*/
            addStaff(staff.split(":")[1]);

        }

        void droneParse(String drone) {
            /*System.out.println("Drone: " + drone.split(":")[1]);
            System.out.println();*/
            addDrone(valueOf(drone.split(":")[1]));
        }

        void orderParse(String order) {
            Pattern orderPattern = Pattern.compile("^ORDER:(\\w+):((\\d) \\* (\\w.+)*+)");
            matcher = orderPattern.matcher(order);


            while (matcher.find()) {
                //System.out.println("User: " + matcher.group(1));
                String order_username = matcher.group(1);
                //System.out.println();
                Map<Dish, Number> orderbasket = new HashMap<>();

                Order parsedOrder = addOrder(order_username);
                User parsedUser = parsedOrder.getUser();

                Pattern dishOrderPattern = Pattern.compile("((\\d+) \\* (\\w+\\s*\\w*))");
                //System.out.println(matcher.group(2));
                Matcher orderMatcher = dishOrderPattern.matcher(matcher.group(2));

                while (orderMatcher.find()) {
                    //System.out.println("Dish: " + orderMatcher.group(3));
                    //System.out.println("Quantity: " + orderMatcher.group(2));

                    Dish dishToAdd = stockManagement.getDish(orderMatcher.group(3));
                    Integer quantity = valueOf(orderMatcher.group(2));

                    orderbasket.put(dishToAdd, quantity);

                }
                parsedUser.setBasket(orderbasket);
                parsedOrder.setContents(parsedUser.getBasket());
                parsedUser.getOrders().add(parsedOrder);
            }
        }

        void stockParse(String stock) {
            Pattern stockPattern = Pattern.compile("^STOCK:(\\w+\\s*\\w*):(\\d+)", Pattern.MULTILINE);
            matcher = stockPattern.matcher(stock);

            while (matcher.find()) {

                String itemName = matcher.group(1);
                String itemQuantity = matcher.group(2);

                //System.out.println("Stock of Item: " + itemName);
                //System.out.println("Quantity: " + itemQuantity);

                stockManagement.dishIngredientFinder(itemName, itemQuantity);
            }
        }
    }
}