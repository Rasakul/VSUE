package chatserver.util.operation;

import channel.util.DataPacket;
import chatserver.Chatserver;
import chatserver.util.Usermodul;

import java.rmi.RemoteException;

/**
 * respond the registered address of an user, if the arguments are valid, the user is logged and the requested user is registered
 */
public class LookupOperation implements Operation {
	private final Chatserver chatserver;

	public LookupOperation(Chatserver chatserver) {

		this.chatserver = chatserver;
	}

	@Override
	public DataPacket process(Integer workerID, DataPacket income) {

		Usermodul usermodul = chatserver.getUsermodul();
		if (usermodul.isLoggedIn(workerID)) {
			String username;

			if (income.getArguments().size() == 1) {
				username = income.getArguments().get(0);
				//if (usermodul.isRegisterd(username)) {
					try {
						income.setResponse(chatserver.getRootNameserver().lookup(username));
					} catch (RemoteException e) {
						income.setError(e.getMessage());
					}
				//} else {
					//income.setError("User not registered!");
				//}
			} else {
				income.setError("Invalid command!");
			}
		} else {
			income.setError("Permission denied, user not logged in!");
		}
		return income;
	}
}
