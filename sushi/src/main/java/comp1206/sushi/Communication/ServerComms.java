package comp1206.sushi.Communication;

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
    protected ObjectOutputStream outputStream;
    protected static ObjectInputStream inputStream;

    Server server;
    ClientListener clientListener;

    public ServerComms(Server aServer) throws IOException {
        serverSocket = new ServerSocket(3000);
        this.server = aServer;
        this.clientListener = new ClientListener();
        this.start();
    }

    public void run() {
        this.clientListener.setRecevingMessages(false);
        this.clientListener.start();
        while (true) {
            try {
                System.out.println("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");

                Socket serverSocket = this.serverSocket.accept();
                this.outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
                this.inputStream = new ObjectInputStream(serverSocket.getInputStream());
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
                this.clientListener.setRecevingMessages(true);



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

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public void close() throws IOException {
        clientListener.setRecevingMessages(false);
        outputStream.close();
        inputStream.close();
        serverSocket.close();


    }

    protected class ClientListener extends Thread {
        ArrayList<ServerComms> activeClients = new ArrayList<>();
        boolean recevingMessages = true;

        ClientListener() {

        }


        public void setRecevingMessages(boolean isAlive) {
            recevingMessages = isAlive;
        }


        public ArrayList<ServerComms> getActiveClients() {
            return activeClients;
        }

        public void run() {
            while (recevingMessages) {
                try {
                    receiveMsg();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        void receiveMsg() throws IOException, ClassNotFoundException {
            try {
                Object obj = ServerComms.inputStream.readObject();
                if (obj instanceof Order) {
                    server.getOrders().add((Order) obj);
                } else if (obj instanceof User) {
                    server.getUsers().add((User) obj);
                }
            } catch (EOFException ignored) {

            }
        }

    }


}
