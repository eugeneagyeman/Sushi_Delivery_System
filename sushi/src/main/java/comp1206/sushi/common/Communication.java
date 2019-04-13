package comp1206.sushi.common;

import java.io.IOException;

public interface Communication {
     void sendMessage() throws IOException, ClassNotFoundException;
     void receiveMessage() throws IOException, ClassNotFoundException;
     void sendMessage(Object object) throws IOException;

     void sendCmdMessage();
     void receiveCmdMessage();
}



