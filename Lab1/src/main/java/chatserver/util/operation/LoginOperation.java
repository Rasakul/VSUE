package chatserver.util.operation;

import channel.util.DataPacket;
import chatserver.Chatserver;
import chatserver.util.Usermodul;

/**
 * Created by Lukas on 20.10.2015.
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

				if (!usermodul.isLogedin(username)) {
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
