package comp1206.sushi.common;

import comp1206.sushi.client.Client;
import comp1206.sushi.server.Server;

import java.io.*;
import java.util.Locale;

public class DataPersistence{
    FileOutputStream fos;
    ObjectOutputStream out;

    FileInputStream fis = null;
    ObjectInputStream in = null;

    Server server;

    public DataPersistence(Server server) throws IOException {

        this.server = server;
        fos = new FileOutputStream("ServerDataPersistence");
        out = new ObjectOutputStream(fos);
        out.writeObject(server);

        fis = new FileInputStream("ServerDataPersistence");
        in = new ObjectInputStream(fis);
    }

    public void writeState(Server server) {
        try {
            out.writeObject(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readState(String objectToPersist) {

        try {
            Server readServer = (Server) in.readObject();
            server = readServer;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }



}
