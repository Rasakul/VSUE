package chatserver.operations;

import chatserver.Chatserver;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Lukas on 20.10.2015.
 */
public class LoginOperation implements Operation {

    private final Chatserver chatserver;

    public LoginOperation(Chatserver chatserver) {

        this.chatserver = chatserver;
    }

    @Override
    public String process(Integer workerID, String line) {

        String[] split = line.split(";");

        if (split.length == 3) {
            String username = split[1];
            String passoword = split[2];

            HashMap<String, String> passwords = chatserver.getPasswords();

            if (passwords.containsKey(username) && Objects.equals(passwords.get(username), passoword)) {

                synchronized (chatserver.getUsersStatus()) {
                    if(!chatserver.getUsersStatus().get(username)){
                        chatserver.getUsersStatus().put(username, true);
                        synchronized (chatserver.getUserConnections_tcp()){
                            chatserver.getUserConnections_tcp().put(workerID,username);
                        }
                    } else {
                        return ("Error, already logged in");
                    }
                }
                return ("Successfully logged in.");
            } else {
                return ("Wrong username or password.");
            }
        } else {
            return ("need username + password");
        }
    }
}
