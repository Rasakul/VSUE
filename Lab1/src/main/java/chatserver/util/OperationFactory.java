package chatserver.util;

import channel.util.DataPacket;
import channel.util.TCPDataPacket;
import chatserver.Chatserver;
import chatserver.util.operation.*;

import java.util.HashMap;

/**
 * Created by Lukas on 20.10.2015.
 */
public class OperationFactory {
	private final Chatserver                 chatserver;
	private       HashMap<String, Operation> operationMap;

	public OperationFactory(Chatserver chatserver) {
		this.chatserver = chatserver;

		operationMap = new HashMap<>();
		operationMap.put("login", new LoginOperation(chatserver));
		operationMap.put("logout", new LogoutOperation(chatserver));
		operationMap.put("send", new PublicMessageOperation(chatserver));
		operationMap.put("register", new RegisterOperation(chatserver));
		operationMap.put("lookup", new LookupOperation(chatserver));
	}

	public DataPacket process(Integer workerID, DataPacket income) {
		DataPacket response = new TCPDataPacket(null, null);
		if (income.getCommand() != null && !income.getCommand().equals("")) {
			Operation operation = operationMap.get(income.getCommand());
			if (operation != null) response = operation.process(workerID, income);
			else {
				response.setResponse("operation not supported!");
			}
		}
		return response;
	}
}
