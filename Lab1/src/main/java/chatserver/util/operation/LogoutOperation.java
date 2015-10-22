package chatserver.util.operation;

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
	public String process(Integer workerID, String line) {

		String[] split = line.split(";");

		if (split.length == 2) {
			String username = split[1];
			Usermodul usermodul = chatserver.getUsermodul();

			if (usermodul.isLogedin(username)) {
				usermodul.logoutUser(workerID);
			} else {
				return ("Error, not logged in");
			}
			return ("Successfully logged out.");
		} else {
			return ("Unknown username.");
		}
	}
}
