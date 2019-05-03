package comp1206.sushi.Communication;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import comp1206.sushi.client.Client;
import comp1206.sushi.common.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ClientCommunication {
    Client client;
    com.esotericsoftware.kryonet.Client clientComms;
    static String ip = "localhost";
    static int tcpPort = 6000;
    boolean messageReceived = false;

    public ClientCommunication(Client aClient) throws IOException, InterruptedException {
        System.out.println("Initialising server communications...");
        this.client = aClient;
        this.clientComms = new com.esotericsoftware.kryonet.Client(22528,10480);


        this.clientComms.getKryo().register(Dish.class);
        this.clientComms.getKryo().register(Ingredient.class);
        this.clientComms.getKryo().register(Order.class);
        this.clientComms.getKryo().register(Postcode.class);
        this.clientComms.getKryo().register(Restaurant.class);
        this.clientComms.getKryo().register(User.class);
        this.clientComms.getKryo().register(Supplier.class);
        this.clientComms.getKryo().register(ConcurrentHashMap.class);
        this.clientComms.getKryo().register(ArrayList.class);
        this.clientComms.getKryo().register(Collections.class);
        this.clientComms.getKryo().register(HashMap.class);


        this.clientComms.start();
        this.clientComms.connect(5000,ip,tcpPort);

        this.clientComms.addListener(new ServerClientListener());


    }

    public void sendMessage(Object obj) {
        clientComms.sendTCP(obj);
    }

    public class ServerClientListener extends Listener {
        String name;

        //set the dishes to what has been received
        public void received(Connection c, Object obj) {
            messageReceived = true;
            if(obj instanceof ArrayList) {
                ArrayList objects = (ArrayList) obj;
                if(((ArrayList) obj).get(0) instanceof Dish) {
                    client.setDishes((ArrayList<Dish>)obj);
                } else if(objects.get(0) instanceof Postcode)
                {
                    client.setPostcodes((ArrayList<Postcode>) objects);
                } else if(objects.get(0) instanceof User) {
                    client.setUsers((ArrayList<User>) objects);
                }
            } else if(obj instanceof User) {
                client.login(((User) obj).getName(),((User) obj).getPassword());
            } else if(obj instanceof Dish) {
                Dish dishToAdd = (Dish) obj;
                client.getDishes().add(dishToAdd);
                client.notifyUpdate();
            }

        }

        public void connected(Connection c) {
            System.out.println("Connected to server: "+c.getRemoteAddressTCP().getHostString());
        }

        public void disconnected(Connection connection) {
            System.out.println("The server has disconnected");
        }


    }
}
