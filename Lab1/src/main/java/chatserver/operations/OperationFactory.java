package chatserver.operations;

import chatserver.Chatserver;

import java.util.HashMap;

/**
 * Created by Lukas on 20.10.2015.
 */
public class OperationFactory {
    private final Chatserver chatserver;
    private HashMap<String, Operation> operationMap;

    public OperationFactory(Chatserver chatserver) {
        this.chatserver = chatserver;

        operationMap = new HashMap<>();
        operationMap.put("login", new LoginOperation(chatserver));
        operationMap.put("logout", new LogoutOperation(chatserver));
        operationMap.put("send", new PublicMessageOperation(chatserver));
    }

    public String process(Integer workerID, String command) {
        String response = "";
        if (command != null && !command.equals("")) {
            Operation operation = operationMap.get(command.split(";")[0]);
            if (operation != null) response = operation.process(workerID, command);
        }
        return response;
    }
}
