package comp1206.sushi.Communication;

import comp1206.sushi.client.Client;
import comp1206.sushi.common.Dish;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import comp1206.sushi.common.*;


public class ClientComms extends Thread {
    String serverName;
    int port;
    ObjectInputStream clientInputStream;
    ObjectOutputStream clientOutputStream;
    Client client;

    public ClientComms(Client aClient) {
        serverName = "localhost";
        port = 3000;
        this.client = aClient;



        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket clientSocket = new Socket(serverName, port);

            System.out.println("Just connected to " + clientSocket.getRemoteSocketAddress());
            clientOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            clientInputStream = new ObjectInputStream(clientSocket.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
        this.start();
    }

    public void sendMsg(Object obj) throws IOException {
        clientOutputStream.writeObject(obj);
    }

    public void run() {
        while(true) {
            try {


                Object obj = clientInputStream.readObject();
                if(obj instanceof Dish) {
                    client.addDish((Dish) obj);
                } else if(obj instanceof Postcode) {
                    client.addPostcode((Postcode) obj);
                } else if(obj instanceof User) {
                    client.getUsers().add((User) obj);
                } else if(obj instanceof String) {
                    //Find order and update status
                    parseReceivedMsg((String) obj);
                }



            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseReceivedMsg(String obj) {

        Integer orderID = Integer.valueOf(obj.split(":")[0]);
        String updateString = obj.split(":")[1];
        for(User user: client.getUsers()) {
            for(Order order:user.getOrders()) {
                if(order.getOrderID().equals(orderID)) {
                    order.setStatus(updateString);
                }
            }
        }
    }


}