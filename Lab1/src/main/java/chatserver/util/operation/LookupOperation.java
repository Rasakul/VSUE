package chatserver.util.operation;

import chatserver.Chatserver;
import chatserver.util.Usermodul;

/**
 * Created by Lukas on 22.10.2015.
 */
public class LookupOperation implements Operation {
	private final Chatserver chatserver;

	public LookupOperation(Chatserver chatserver) {

		this.chatserver = chatserver;
	}

	@Override
	public String process(Integer workerID, String line) {
		String response;
		Usermodul usermodul = chatserver.getUsermodul();
		if (usermodul.isLogedin(workerID)) {
			String username;

			String[] split = line.split(";");
			if (split.length == 2) {
				username = split[1];
				if (usermodul.isRegisterd(username)) {
					response = "lookup:" + usermodul.getAdress(username);
				} else {
					response = "User not registered!";
				}
			} else {
				response = "Invalid command!";
			}
		} else {
			response = "Permission denied, user not logged in!";
		}
		return response;
	}
}
