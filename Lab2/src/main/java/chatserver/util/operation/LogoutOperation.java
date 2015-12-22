package chatserver.util.operation;

import channel.util.DataPacket;
import chatserver.Chatserver;
import chatserver.util.Usermodul;

/**
 * Validate the DataPacket arguments and log out the user, if the user is logged in
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

			if (usermodul.isLoggedIn(username)) {
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
