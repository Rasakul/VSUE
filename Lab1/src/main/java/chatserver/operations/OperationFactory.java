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
        operationMap.put("login", new LoginOperation(this));
    }

    public String process(String operation) {
        String response = "";
        if (operation != null && !operation.equals("")) {
            response = operationMap.get(operation.split(" ")[0]).process(operation);
        }
        return response;
    }

    public Chatserver getChatserver() {
        return chatserver;
    }
}
