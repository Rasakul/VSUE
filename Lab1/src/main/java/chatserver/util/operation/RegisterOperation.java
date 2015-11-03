package chatserver.util.operation;

import channel.util.DataPacket;
import chatserver.Chatserver;
import chatserver.util.Usermodul;

import java.util.regex.Pattern;

/**
 * Created by Lukas on 22.10.2015.
 */
public class RegisterOperation implements Operation {

	private final Chatserver chatserver;
	private static final Pattern PATTERN = Pattern
			.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	public RegisterOperation(Chatserver chatserver) {
		this.chatserver = chatserver;
	}

	@Override
	public DataPacket process(Integer workerID, DataPacket income) {
		Usermodul usermodul = chatserver.getUsermodul();

		if (usermodul.isLogedin(workerID)) {
			if (income.getArguments().size() == 1) {
				String address = income.getArguments().get(0);
				String[] split = address.split(":");
				if (split[0] != null && PATTERN.matcher(split[0]).matches()) {
					String username = usermodul.getUser(workerID);
					usermodul.registerUser(username, address);
					income.setResponse("Successfully registered address for " + username);

				} else {
					income.setError("invalid host");
				}
			} else {
				income.setError("Invalid command!");
			}
		} else {
			income.setError("Permission denied, user not logged in!");
		}
		return income;
	}
}
