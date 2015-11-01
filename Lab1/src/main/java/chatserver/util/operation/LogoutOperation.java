package chatserver.util.operation;

import channel.util.DataPacket;
import chatserver.Chatserver;
import chatserver.util.Usermodul;

/**
 * Created by Lukas on 21.10.2015.
 */
public class LogoutOperation implements Operation {

	private final Chatserver chatserver;

	public LogoutOperation(Chatserver chatserver) {

		this.chatserver = chatserver;
	}

	@Override
	public DataPacket process(Integer workerID, DataPacket income) {

		if (income.getArguments().size() == 1) {
			String username = income.getArguments().get(0);
			Usermodul usermodul = chatserver.getUsermodul();

			if (usermodul.isLogedin(username)) {
				usermodul.logoutUser(workerID);
				income.setResponse("Successfully logged out.");
			} else {
				income.setError("Error, not logged in");
			}
		} else {
			income.setError("Unknown username.");
		}
		return income;
	}
}
