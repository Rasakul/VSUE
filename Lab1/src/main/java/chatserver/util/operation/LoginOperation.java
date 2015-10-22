package chatserver.util.operation;

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
	public String process(Integer workerID, String line) {

		String[] split = line.split(";");

		if (split.length == 3) {
			String username = split[1];
			String password = split[2];
			Usermodul usermodul = chatserver.getUsermodul();

			if (usermodul.checkPassword(username, password)) {

				if (!usermodul.isLogedin(username)) {
					usermodul.loginUser(workerID,username);
				} else {
					return ("Error, already logged in");
				}
				return ("Successfully logged in.");
			} else {
				return ("Wrong username or password.");
			}
		} else {
			return ("need username + password");
		}
	}
}
