package chatserver.util.operation;

import chatserver.Chatserver;
import chatserver.util.Usermodul;

/**
 * Created by Lukas on 22.10.2015.
 */
public class RegisterOperation implements Operation {

	private final Chatserver chatserver;

	public RegisterOperation(Chatserver chatserver) {
		this.chatserver = chatserver;
	}

	@Override
	public String process(Integer workerID, String line) {
		Usermodul usermodul = chatserver.getUsermodul();
		String response;
		String username;
		String[] split = line.split(";");
		if (usermodul.isLogedin(workerID)) {
			if (split.length == 2) {
				String address = split[1];
				username = usermodul.getUser(workerID);
				usermodul.registerUser(username, address);
				response = "Successfully registered address for " + username;
			} else {
				response = "Invalid command!";
			}
		} else {
			response = "Permission denied, user not logged in!";
		}
		return response;
	}
}
