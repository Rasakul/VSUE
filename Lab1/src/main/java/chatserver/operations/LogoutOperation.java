package chatserver.operations;

import chatserver.Chatserver;

/**
 * Created by Lukas on 21.10.2015.
 */
public class LogoutOperation implements Operation {

    private final Chatserver chatserver;

    public LogoutOperation(Chatserver chatserver) {

        this.chatserver = chatserver;
    }

    @Override
    public String process(Integer workerID, String line) {

        String[] split = line.split(";");

        if (split.length == 2) {
            String username = split[1];

            synchronized (chatserver.getUsersStatus()) {
                if (chatserver.getUsersStatus().get(username)) {
                    chatserver.getUsersStatus().put(username, false);
                    synchronized (chatserver.getUserConnections_tcp()){
                        chatserver.getUserConnections_tcp().remove(workerID);
                    }
                } else {
                    return ("Error, not logged in");
                }
            }
            return ("Successfully logged out.");
        } else {
            return ("Unknown username.");
        }
    }
}
