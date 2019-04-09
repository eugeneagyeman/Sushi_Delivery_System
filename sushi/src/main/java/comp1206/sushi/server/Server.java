package comp1206.sushi.server;

import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.valueOf;

public class Server implements ServerInterface {

    private static final Logger logger = LogManager.getLogger("Server");
    public static ArrayList<Order> orders = new ArrayList<Order>();
    public static ArrayList<User> users = new ArrayList<User>();
    public static ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
    public static Restaurant restaurant;
    public static ArrayList<Drone> drones = new ArrayList<Drone>();
    public static ArrayList<Staff> staff = new ArrayList<Staff>();
    public static ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
    private static ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();

    public Server() {
        logger.info("Starting up server...");
        loadConfiguration("Configuration.txt");

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
    public List<Dish> getDishes() {
        return StockManagement.getDishes();
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
        StockManagement.getDishesStock().putIfAbsent(newDish,0);

        this.notifyUpdate();
        return newDish;
    }

    @Override
    public void removeDish(Dish dish) {
        StockManagement.getDishes().remove(dish);
        this.notifyUpdate();
    }

    @Override
    public Map<Dish, Number> getDishStockLevels() {
        return StockManagement.getDishesStock();
    }

    @Override
    public void setRestockingIngredientsEnabled(boolean enabled) {

    }

    @Override
    public void setRestockingDishesEnabled(boolean enabled) {

    }

    @Override
    public void setStock(Dish dish, Number stock) {
        Number oldValue = StockManagement.getDishesStock().get(dish);
        StockManagement.getDishesStock().replace(dish, oldValue.intValue(), stock.intValue());
        this.notifyUpdate();
    }

    @Override
    public void setStock(Ingredient ingredient, Number stock) {
        Number oldValue = StockManagement.getDishesStock().get(ingredient);
        StockManagement.getIngredientsStock().replace(ingredient, oldValue.intValue(), stock.intValue());
        this.notifyUpdate();
    }

    @Override
    public List<Ingredient> getIngredients() {
        return StockManagement.getIngredients();
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
        StockManagement.getIngredientsStock().putIfAbsent(mockIngredient,0);
        this.notifyUpdate();
        return mockIngredient;
    }

    @Override
    public void removeIngredient(Ingredient ingredient) {
        StockManagement.getIngredientsStock().remove(ingredient);
        this.notifyUpdate();
    }

    @Override
    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    @Override
    public Supplier addSupplier(String name, Postcode postcode) {
        Supplier mock = new Supplier(name, postcode);
        suppliers.add(mock);
        return mock;
    }


    @Override
    public void removeSupplier(Supplier supplier) {
        suppliers.remove(supplier);
        this.notifyUpdate();
    }

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
    public List<Staff> getStaff() {
        return staff;
    }

    @Override
    public Staff addStaff(String name) {
        Staff mock = new Staff(name);
        staff.add(mock);
        return mock;
    }

    @Override
    public void removeStaff(Staff staff) {
        Server.staff.remove(staff);
        this.notifyUpdate();
    }


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
    public Map<Ingredient, Number> getIngredientStockLevels() {
        return StockManagement.getIngredientsStock();
    }

    @Override
    public Number getSupplierDistance(Supplier supplier) {
        return supplier.getDistance();
    }

    @Override
    public Number getDroneSpeed(Drone drone) {
        return drone.getSpeed();
    }

    @Override
    public Number getOrderDistance(Order order) {
        Order mock = order;
        return mock.getDistance();
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
                System.out.println("Existing postcode");
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

    @Override
    public List<User> getUsers() {
        return users;
    }

    @Override
    public void removeUser(User user) {
        users.remove(user);
        this.notifyUpdate();
    }

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
    public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
        for (Entry<Ingredient, Number> recipeItem : recipe.entrySet()) {
            addIngredientToDish(dish, recipeItem.getKey(), recipeItem.getValue());
        }
        this.notifyUpdate();
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
    public String getStaffStatus(Staff staff) {
        Random rand = new Random();
        if (rand.nextBoolean()) {
            return "Idle";
        } else {
            return "Working";
        }
    }

    @Override
    public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
        dish.setRestockThreshold(restockThreshold);
        dish.setRestockAmount(restockAmount);
        this.notifyUpdate();
    }

    @Override
    public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
        ingredient.setRestockThreshold(restockThreshold);
        ingredient.setRestockAmount(restockAmount);
        this.notifyUpdate();
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
    public Number getRestockThreshold(Ingredient ingredient) {
        return ingredient.getRestockThreshold();
    }

    @Override
    public Number getRestockAmount(Ingredient ingredient) {
        return ingredient.getRestockAmount();
    }

    @Override
    public void addUpdateListener(UpdateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void notifyUpdate() {
        listeners.forEach(listener -> listener.updated(new UpdateEvent()));
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


    public User addUser(String name) {
        for (User existingUsers : users) {
            if (existingUsers.getName().equals(name)) return existingUsers;
        }
        return null;
    }

    static class StockManagement {
        private static ArrayList<Dish> dishes;
        private static ArrayList<Ingredient> ingredients;

        private static Map<Ingredient, Number> ingredientsStock = new HashMap<>();
        private static Map<Dish, Number> dishesStock = new HashMap<>();

        public static Map<Ingredient, Number> getIngredientsStock() {
                ingredientsTracker();
                return ingredientsStock;

        }


        public static Map<Dish, Number> getDishesStock() {
                dishesTracker();
                return dishesStock;
        }

        public static void ingredientsTracker() {

            for (Ingredient existingIngredient : getIngredients()) {
          //      System.out.println("Ingredient: " + existingIngredient.getName()
            //            + " Quantity:" + ingredientsStock.computeIfAbsent(existingIngredient,k -> );
            }
        }

        public static void dishesTracker() {

            /*for (Dish existingDish : getDishes()) {
                System.out.println("Dish: " + existingDish.getName()
                        + " Quantity: " + dishesStock.computeIfAbsent(existingDish,k->existingDish.getQuantity()));
            }*/
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

        public static List<Ingredient> getIngredients() {
            ingredients = new ArrayList<Ingredient>(ingredientsStock.keySet());
            return ingredients;
            //return ingredients;
        }

        public static void setIngredients(ArrayList<Ingredient> ingredients) {
            StockManagement.ingredients = ingredients;
        }

        public void restockIngredient(Ingredient ingredient) {
            int restockThreshold = ingredient.getRestockThreshold().intValue();
            int restockAmount = ingredient.getRestockAmount().intValue();

            restockThreshold += restockAmount;

            ingredient.setRestockThreshold(restockThreshold);
        }

        public void restockDish(Dish dish) {
            int restockThreshold = dish.getRestockThreshold().intValue();
            int restockAmount = dish.getRestockAmount().intValue();

            restockThreshold += restockAmount;

            dish.setRestockThreshold(restockThreshold);
        }

        private static void dishIngredientFinder(String itemName, String itemQuantity) {
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

        /*List of ingredients currently in system
        List of dishes currently in system
        Method to track current ingredients
        Method to track current dishes
        Method to restock ingredients
        Method to build dish
        Method to restock dishes.
         */


    /*
            Take a file input
            File Reader
            If no file throw no file exception
            else

            Read the text
            Read Text Line By Line
            split line into respective model and value
            Parse each line with model
            Input the parsed data
            Create the models using parsed data
    */
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

        void postcodeParse(String postcode) throws IOException {
            Pattern postcodePattern = Pattern.compile("^POSTCODE:(.+)$", Pattern.MULTILINE);
            matcher = postcodePattern.matcher(postcode);

            while (matcher.find()) {
                System.out.println("Postcode: " + matcher.group(1));
                addPostcode(matcher.group(1));
            }
        }

        void restaurantParse(String restaurants) throws IOException {
            Pattern restaurantPattern = Pattern.compile("^RESTAURANT:(\\w+\\s*+\\w*+):(.+)$", Pattern.MULTILINE);
            matcher = restaurantPattern.matcher(restaurants);
            while (matcher.find()) {
                System.out.println("Restaurant: " + matcher.group(1));
                System.out.println("Restaurant Postcode: " + matcher.group(2));
                System.out.println();
                restaurant = new Restaurant(matcher.group(1), getPostcodes().get(0));
            }
        }

        void supplierParse(String supplier) throws IOException {
            Pattern supplierPattern = Pattern.compile("^SUPPLIER:(.+):(.+)$", Pattern.MULTILINE);
            matcher = supplierPattern.matcher(supplier);

            while (matcher.find()) {
                String parsedSupplier = matcher.group(1);
                String parsedPostcode = matcher.group(2);

                System.out.println();
                System.out.println("Supplier: " + parsedSupplier);
                System.out.println("Postcode: " + parsedPostcode);
                System.out.println();

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
                        System.out.println("Existing supplier");
                    }
                }

                System.out.println("Ingredient: " + name);
                System.out.println("Units: " + units);
                System.out.println("Supplier: " + supplier);
                System.out.println("Restock Threshold: " + restock_threshold);
                System.out.println("Restock Amount: " + restock_amount);
                System.out.println("Weight: " + weight);
                System.out.println();

                addIngredient(name, units, supplierFromSys, restock_threshold, restock_amount, weight);


            }
        }

        void dishParse(String dishes) {
            Pattern dishPattern = Pattern.compile("^DISH:(\\w.+):(\\w.+):([0-9]+):([0-9]+):(\\d+):((\\d*)\\s\\*\\s(\\w+).*)");
            matcher = dishPattern.matcher(dishes);

            while (matcher.find()) {
                System.out.println("Dish Name: " + matcher.group(1));
                System.out.println("Description: " + matcher.group(2));
                System.out.println("Price: " + matcher.group(3));
                System.out.println("Restock Threshold: " + matcher.group(4));
                System.out.println("Restock Amount: " + matcher.group(5));
                System.out.println("Ingredients: " + matcher.group(6));
                System.out.println();

                String dishName = matcher.group(1);
                String description = matcher.group(2);
                Integer price = valueOf(matcher.group(3));
                Integer restock_threshold = valueOf(matcher.group(4));
                Integer restock_amount = valueOf(matcher.group(5));

                Dish definedDish = addDish(dishName, description, price, restock_threshold, restock_amount);

                Pattern ingredientsDishPattern = Pattern.compile("((\\d+) \\* (\\w+))");
                Matcher ingredientMatcher = ingredientsDishPattern.matcher(matcher.group(6));

                while (ingredientMatcher.find()) {
                    System.out.println("Ingredient: " + ingredientMatcher.group(3));
                    System.out.println("Quantity: " + ingredientMatcher.group(2) + "\n");

                    String parsedIngredient = ingredientMatcher.group(3);
                    Integer parsedIngredientQuantity = valueOf(ingredientMatcher.group(2));

                    Ingredient ingredientFromSys = null;

                    for (Ingredient ingredient : getIngredients()) {
                        if (ingredient.getName().equals(parsedIngredient)) ingredientFromSys = ingredient;
                    }

                    addIngredientToDish(definedDish, ingredientFromSys, parsedIngredientQuantity);
                }

            }
        }

        void userParse(String users) {
            Pattern userPattern = Pattern.compile("^USER:(\\w+):(\\w+):(\\w.+):(\\w.+)", Pattern.MULTILINE);
            matcher = userPattern.matcher(users);

            while (matcher.find()) {
                System.out.println("User: " + matcher.group(1));
                System.out.println("Password: " + matcher.group(2));
                System.out.println("Location: " + matcher.group(3));
                System.out.println("Postcode: " + matcher.group(4));
                System.out.println();

                String username = matcher.group(1);
                String password = matcher.group(2);
                String location = matcher.group(3);
                String postcode = matcher.group(4);

                getUsers().add(new User(username, password, location, addPostcode(postcode)));

            }
        }

        void staffParse(String staff) {
            System.out.println("Staff: " + staff.split(":")[1]);
            System.out.println();
            addStaff(staff.split(":")[1]);

        }

        void droneParse(String drone) {
            System.out.println("Drone: " + drone.split(":")[1]);
            System.out.println();
            addDrone(valueOf(drone.split(":")[1]));
        }

        void orderParse(String order) {
            Pattern orderPattern = Pattern.compile("^ORDER:(\\w+):((\\d) \\* (\\w.+)*+)");
            matcher = orderPattern.matcher(order);


            while (matcher.find()) {
                System.out.println("User: " + matcher.group(1));
                String order_username = matcher.group(1);
                System.out.println();
                Map<Dish, Number> orderbasket = new HashMap<>();

                Order parsedOrder = addOrder(order_username);
                User parsedUser = parsedOrder.getUser();

                Pattern dishOrderPattern = Pattern.compile("((\\d+) \\* (\\w+\\s*\\w*))");
                System.out.println(matcher.group(2));
                Matcher orderMatcher = dishOrderPattern.matcher(matcher.group(2));

                while (orderMatcher.find()) {
                    System.out.println("Dish: " + orderMatcher.group(3));
                    System.out.println("Quantity: " + orderMatcher.group(2));

                    Dish dishToAdd = StockManagement.getDish(orderMatcher.group(3));
                    Integer quantity = valueOf(orderMatcher.group(2));

                    orderbasket.put(dishToAdd, quantity);

                }
                parsedUser.setBasket(orderbasket);
                parsedOrder.addContents(parsedUser.getBasket());
                parsedUser.getOrders().add(parsedOrder);
            }
        }

        void stockParse(String stock) {
            Pattern stockPattern = Pattern.compile("^STOCK:(\\w+\\s*\\w*):(\\d+)", Pattern.MULTILINE);
            matcher = stockPattern.matcher(stock);

            while (matcher.find()) {

                String itemName = matcher.group(1);
                String itemQuantity = matcher.group(2);

                System.out.println("Stock of Item: " + itemName);
                System.out.println("Quantity: " + itemQuantity);

                StockManagement.dishIngredientFinder(itemName, itemQuantity);
            }
        }

    }
}


