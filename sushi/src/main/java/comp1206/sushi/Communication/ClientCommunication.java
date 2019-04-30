package comp1206.sushi.Communication;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import comp1206.sushi.client.Client;
import comp1206.sushi.common.*;

import java.io.IOException;
import java.util.ArrayList;

public class ClientCommunication {
    Client client;
    static com.esotericsoftware.kryonet.Client clientComms;
    static String ip = "localhost";
    static int tcpPort = 6000;
    boolean messageReceived = false;
    Connection connection;


    public ClientCommunication(Client aClient) throws IOException, InterruptedException {
        client = aClient;
        clientComms = new com.esotericsoftware.kryonet.Client();

        clientComms.getKryo().register(Dish.class);
        clientComms.getKryo().register(User.class);
        clientComms.getKryo().register(Order.class);
        clientComms.getKryo().register(ArrayList.class);

        clientComms.start();
        clientComms.connect(5000,ip,tcpPort,6000);

        clientComms.addListener(new ClientListener());

        while(!messageReceived) {
            Thread.sleep(1000);
        }
        System.out.println("The client program is waiting for");


    }

    public void sendMessage(Object obj) {
        connection.sendTCP(obj);
    }

    public class ClientListener extends Listener {
        String name;

        //set the dishes to what has been received
        public void received(Connection c, Object obj) {
            messageReceived = true;
            if(obj instanceof ArrayList) {
                if(((ArrayList) obj).get(0) instanceof Dish) {
                    client.setDishes((ArrayList<Dish>)obj);
                }
            }

        }

        public void connected(Connection c) {
            connection = c;
            System.out.println("Connected to server: "+c.getRemoteAddressTCP().getHostString());
        }

        public void disconnected(Connection connection) {
            System.out.println("The server has disconnected");
        }


    }
}
