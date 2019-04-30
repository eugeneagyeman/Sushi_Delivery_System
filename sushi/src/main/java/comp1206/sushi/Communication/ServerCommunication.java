package comp1206.sushi.Communication;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import comp1206.sushi.common.Dish;
import comp1206.sushi.server.Server;

import java.io.IOException;
import java.util.ArrayList;

import comp1206.sushi.common.*;


public class ServerCommunication  extends Thread{
    ArrayList<ServerListener> list_of_active_clients = new ArrayList<>();
    //ServerSocket serverSocket;
    //Socket clientSocket;
    Server server;
    Connection connection;
    boolean messageReceived;



    /*public ServerCommunication(ServerSocket ss) throws IOException {
        this.serverSocket = ss;
        acceptClients();
    }

    void acceptClients() {
        while(true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Client received found: " + clientSocket);

                DataInputStream testInputStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream testOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                ClientServerLayer receiveMessage = new ClientServerLayer(clientSocket,testInputStream,testOutputStream);
                list_of_active_clients.add(receiveMessage);
                new Thread(receiveMessage).start();



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
    public ServerCommunication(Server aServer) throws IOException {
        System.out.println("Initialising server communications...");
        this.server = aServer;

        com.esotericsoftware.kryonet.Server serverComms = new com.esotericsoftware.kryonet.Server();

        serverComms.getKryo().register(Dish.class);
        serverComms.getKryo().register(Ingredient.class);
        serverComms.getKryo().register(Order.class);
        serverComms.getKryo().register(Postcode.class);
        serverComms.getKryo().register(Restaurant.class);
        serverComms.getKryo().register(User.class);
        serverComms.getKryo().register(java.util.Collections.class);

        serverComms.bind(6000,6000);
        serverComms.start();

        serverComms.addListener(new ServerListener());

        System.out.println("Server comms initialised.");


    }
    public void sendMessage(Object obj) {
        connection.sendTCP(obj);
    }


    class ServerListener extends Listener {
        String name;

        public ServerListener() {
        }

        public void connected(Connection c) {
            name = c.getRemoteAddressTCP().getHostName();
            System.out.println("Connection received from " + name);
            connection = c;
            list_of_active_clients.add(this);
        }

        //If request is dishes return dishes else
        public void received(Connection c, Object obj) {
            if(obj instanceof Order) {
                Order anOrder = (Order) obj;
                server.getOrders().add(anOrder);
            } else if(obj instanceof String) {
                switch((String) obj) {
                    case "Dishes":
                        sendMessage(server.getDishes());
                }
            }
        }
    }
}

/*class ClientServerLayer implements Runnable {
    Scanner scanner = new Scanner(System.in);
    Socket instanceSocket;
    final DataInputStream inputStream;
    final DataOutputStream outputStream;

    ClientServerLayer(Socket socket, DataInputStream instream, DataOutputStream outStream) {
        this.instanceSocket = socket;
        this.inputStream = instream;
        this.outputStream = outStream;
    }
    @Override
    public void run() {
        String receiveMessage;
        while(true)
        {
            try{
                receiveMessage = inputStream.readUTF();
                System.out.println(receiveMessage);
                System.out.println("Sending it off");
                outputStream.writeUTF(receiveMessage+" sent back");
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}*/
