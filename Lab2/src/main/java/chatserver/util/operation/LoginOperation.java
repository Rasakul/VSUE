package chatserver.util.operation;

import channel.util.DataPacket;
import chatserver.Chatserver;
import chatserver.util.Usermodul;

/**
 * Validate the DataPacket arguments, check the password and log in the user, if the user is not already logged in
 */
public class LoginOperation implements Operation {

	private final Chatserver chatserver;

	public LoginOperation(Chatserver chatserver) {

		this.chatserver = chatserver;
	}

	@Override
	public DataPacket process(Integer workerID, DataPacket income) {

		if (income.getArguments().size() == 2) {
			String username = income.getArguments().get(0);
			String password = income.getArguments().get(1);
			Usermodul usermodul = chatserver.getUsermodul();

			if (usermodul.checkPassword(username, password)) {

				if (!usermodul.isLoggedIn(username)) {
					usermodul.loginUser(workerID, username);
					income.setResponse("Successfully logged in.");
				} else {
					income.setError("Error, already logged in");
				}
			} else {
				income.setError("Wrong username or password.");
			}
		} else {
			income.setError("need username + password");
		}
		return income;
	}
}
