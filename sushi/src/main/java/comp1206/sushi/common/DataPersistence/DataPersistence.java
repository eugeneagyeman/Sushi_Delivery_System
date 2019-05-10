package comp1206.sushi.common.DataPersistence;

import comp1206.sushi.client.Client;
import comp1206.sushi.common.*;
import comp1206.sushi.common.StockManagement.StockManagement;
import comp1206.sushi.server.Server;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

public class DataPersistence {
    FileOutputStream fos;
    ObjectOutputStream out;




    FileInputStream fis = null;
    ObjectInputStream in = null;

    private volatile Server server;
    Timer timer;

    public DataPersistence(Server server) throws IOException {

        this.server = server;
        this.timer = new Timer();
        fos = new FileOutputStream("ServerDataPersistence",false);
        out = new ObjectOutputStream(fos);

        timer.scheduleAtFixedRate(new Refresh(),0,30000);


    }

    public void writeState() {
        ArrayList<Object> data = new ArrayList<>();

        try {
            data.add(
                    server.getOrders());
            data.add(
                    server.getUsers());
            data.add(
                    server.getPostcodes());
            data.add(
                    server.getDrones());
            data.add(
                    server.getStaff());
            data.add(
                    server.getSuppliers());
            data.add(
                    server.getRestaurant());
            data.add(
                    server.getStockManagement());
            data.add(
                    server.getOrderQueue());
            data.add(
                    server.getIngredientQueue());
            data.add(
                    server.getDishQueue());

            out.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Refresh extends TimerTask {

        @Override
        public void run() {
            writeState();
        }
    }

    public void readState(String objectToPersist) throws IOException {
        fis = new FileInputStream(objectToPersist);
        in = new ObjectInputStream(fis);
        ArrayList<Object> deserialisedData = new ArrayList<>();

        try {
            deserialisedData = (ArrayList<Object>) in.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        server.setOrders((ArrayList<Order>) deserialisedData.get(0));
        server.setUsers((ArrayList<User>) deserialisedData.get(1));
        server.setPostcodes((ArrayList<Postcode>) deserialisedData.get(2));

        server.setDrones((ArrayList<Drone>) deserialisedData.get(3));
        server.setStaff((ArrayList<Staff>) deserialisedData.get(4));
        server.setSuppliers((ArrayList<Supplier>) deserialisedData.get(5));
        server.setRestaurant((Restaurant) deserialisedData.get(6));
        server.setStockManagement((StockManagement) deserialisedData.get(7));
        server.setOrderQueue((BlockingQueue<Order>) deserialisedData.get(8));
        server.setIngredientQueue((BlockingQueue<Ingredient>) deserialisedData.get(9));
        server.setDishQueue((BlockingQueue<Dish>) deserialisedData.get(10));
        server.notifyUpdate();
    }

    public void close() throws IOException {
        fos.close();
        out.close();
        fis.close();
        in.close();
    }


}
