package chatserver.util;

import channel.util.DataPacket;
import chatserver.Chatserver;
import chatserver.util.operation.*;

import java.util.HashMap;

/**
 * Factory for all available operations
 */
public class OperationFactory {
	private final Chatserver                 chatserver;
	private final HashMap<String, Operation> operationMap;

	public OperationFactory(Chatserver chatserver) {
		this.chatserver = chatserver;

		operationMap = new HashMap<>();
		operationMap.put("login", new LoginOperation(chatserver));
		operationMap.put("logout", new LogoutOperation(chatserver));
		operationMap.put("send", new PublicMessageOperation(chatserver));
		operationMap.put("register", new RegisterOperation(chatserver));
		operationMap.put("lookup", new LookupOperation(chatserver));
	}

	/**
	 * choose the corresponding operation to the {@link DataPacket} and process it
	 *
	 * @param workerID ID of the worker who request the operation
	 * @param income   incoming datapaket to process
	 *
	 * @return a datapacket with filled in response or error message
	 */
	public DataPacket process(Integer workerID, DataPacket income) {

		if (income.getCommand() != null && !income.getCommand().equals("")) {
			Operation operation = operationMap.get(income.getCommand());
			if (operation != null) income = operation.process(workerID, income);
			else income.setError("operation not supported!");

		}
		return income;
	}
}
