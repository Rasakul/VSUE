package chatserver.operations;

import chatserver.Chatserver;
import chatserver.worker.TCPWorker;
import chatserver.worker.Worker;

import java.io.IOException;
import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * Created by Lukas on 21.10.2015.
 */
public class PublicMessageOperation implements Operation {

    private final Chatserver chatserver;
    private String lastMsg;

    public PublicMessageOperation(Chatserver chatserver) {

        this.chatserver = chatserver;
    }

    @Override
    public String process(Integer workerID, String line) {
        String message = line.replaceFirst(Pattern.quote("sned;"), "");

        Hashtable<Integer, String> userConnections;
        synchronized (chatserver.getUserConnections_tcp()) {
            userConnections = chatserver.getUserConnections_tcp();
        }

        String username = userConnections.get(workerID);
        message = username + ": " + message;
        lastMsg = message;

        for (int ID : userConnections.keySet()) {
            synchronized (chatserver.getOpenConnections()) {
                Worker worker = chatserver.getOpenConnections().get(ID);
                try {
                    ((TCPWorker) worker).getChannel().send(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public String getLastMsg() {
        return lastMsg;
    }
}
