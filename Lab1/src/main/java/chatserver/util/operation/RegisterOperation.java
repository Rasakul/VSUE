package chatserver.util.operation;

import channel.util.DataPacket;
import chatserver.Chatserver;
import chatserver.util.Usermodul;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Lukas on 22.10.2015.
 */
public class RegisterOperation implements Operation {

	private final Chatserver chatserver;

	public RegisterOperation(Chatserver chatserver) {
		this.chatserver = chatserver;
	}

	@Override
	public DataPacket process(Integer workerID, DataPacket income) {
		Usermodul usermodul = chatserver.getUsermodul();

		if (usermodul.isLogedin(workerID)) {
			if (income.getArguments().size() == 1) {
				try {
					String address = income.getArguments().get(0);
					InetAddress.getByName(address);
					String username = usermodul.getUser(workerID);
					usermodul.registerUser(username, address);
					income.setResponse("Successfully registered address for " + username);
				} catch (UnknownHostException e) {
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
