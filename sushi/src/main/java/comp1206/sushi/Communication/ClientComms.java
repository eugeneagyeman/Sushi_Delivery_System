package comp1206.sushi.Communication;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import comp1206.sushi.client.Client;
import comp1206.sushi.common.Dish;
import comp1206.sushi.server.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import comp1206.sushi.common.*;
import comp1206.sushi.server.StockManagement;


public class ClientComms extends Thread {
    String serverName;
    int port;
    ObjectInputStream clientInputStream;
    ObjectOutputStream clientOutputStream;
    Client client;
    Socket clientSocket;
    boolean recevingMessages = true;


    public ClientComms(Client aClient) {
        serverName = "localhost";
        port = 3000;
        this.client = aClient;



        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            clientSocket = new Socket(serverName, port);

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

    public void setRecevingMessages(boolean isAlive) {
        recevingMessages = isAlive;
    }

    public void run() {
        while(recevingMessages) {
            try {


                Object obj = clientInputStream.readObject();
                if(obj instanceof Dish) {
                    client.addDish((Dish) obj);
                } else if(obj instanceof Postcode) {
                    client.addPostcode((Postcode) obj);
                } else if(obj instanceof User) {
                    client.getUsers().add((User) obj);
                }



            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() throws IOException {
            setRecevingMessages(false);
            clientOutputStream.close();
            clientInputStream.close();
            clientSocket.close();


    }




}