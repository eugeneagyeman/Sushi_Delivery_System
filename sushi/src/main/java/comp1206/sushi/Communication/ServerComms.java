package comp1206.sushi.Communication;

import comp1206.sushi.StockManagement.StockManagement;
import comp1206.sushi.common.*;
import comp1206.sushi.server.Server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;


public class ServerComms extends Thread {
    private ServerSocket serverSocket;


    protected static ObjectOutputStream outputStream;
    protected static ObjectInputStream inputStream;
    static Server server;
    ClientListener clientListener;

    public ServerComms(Server aServer) throws IOException {
        serverSocket = new ServerSocket(3000);
        server = aServer;
        clientListener = new ClientListener();
        this.start();
    }

    public void run() {
        while (true) {
            try {
                System.out.println("Waiting...");
                System.out.println("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");

                Socket serverSocket = this.serverSocket.accept();
                this.outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
                inputStream = new ObjectInputStream(serverSocket.getInputStream());
                clientListener.getActiveClients().add(this);

                for (Dish dishes : server.getDishes()) {
                    outputStream.writeObject(dishes);
                }

                for (Postcode postcode : server.getPostcodes()) {
                    outputStream.writeObject(postcode);
                }

                for (User user : server.getUsers()) {
                    outputStream.writeObject(user);
                }

                clientListener.start();

            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void sendMsg(Object obj, boolean sendToAll) throws IOException {
        outputStream.writeObject(obj);
        if (sendToAll) {
            for (ServerComms connectedClients : clientListener.getActiveClients()) {
                connectedClients.getOutputStream().writeObject(obj);
            }
        }
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public static ObjectInputStream getInputStream() {
        return inputStream;
    }

    protected class ClientListener extends Thread {
        ArrayList<ServerComms> activeClients = new ArrayList<>();

        public ArrayList<ServerComms> getActiveClients() {
            return activeClients;
        }

        public void run() {
            while (true) {
                receiveMsg();
            }
        }

        void receiveMsg() {
            try {
                Object obj = ServerComms.getInputStream().readObject();
                if (obj instanceof Order) {
                    Order receivedOrder = (Order) obj;
                    receivedOrder.setStatus("Received");
                    //send message across to client to update status
                    String notify = String.format("%s:Received", receivedOrder.getOrderID());
                    ServerComms.sendMsg(notify);

                    server.getOrders().add(receivedOrder);
                    server.getOrderQueue().add(receivedOrder);
                } else if (obj instanceof User) {
                    server.getUsers().add((User) obj);
                } else if (obj instanceof String) {
                    parseReceivedMsg((String) obj);
                }
            } catch (EOFException ignored) {

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private static void sendMsg(Object obj) throws IOException {
        ServerComms.outputStream.writeObject(obj);

    }

    private void parseReceivedMsg(String obj) {

        Integer orderID = Integer.valueOf(obj.split(":")[0]);
        String updateString = obj.split(":")[1];

        for (Order order : server.getOrders()) {
            if (order.getOrderID().equals(orderID)) {
                order.setStatus(updateString);
            }
        }

    }


}
