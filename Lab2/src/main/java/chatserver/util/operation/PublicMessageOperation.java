package chatserver.util.operation;

import channel.util.DataPacket;
import chatserver.Chatserver;
import chatserver.util.Usermodul;
import chatserver.worker.TCPWorker;
import chatserver.worker.Worker;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Send a message to all logged in users, if the requesting user is logged in
 */
public class PublicMessageOperation implements Operation {
	private static final Logger LOGGER = Logger.getLogger(PublicMessageOperation.class.getName());

	private final Chatserver chatserver;

	public PublicMessageOperation(Chatserver chatserver) {

		this.chatserver = chatserver;
	}

	@Override
	public DataPacket process(Integer workerID, DataPacket income) {
		Usermodul usermodul = chatserver.getUsermodul();

		if (usermodul.isLoggedIn(workerID)) {
			String message = income.getArguments().get(0);
			String username = usermodul.getUser(workerID);
			message = username + ": " + message;

			for (int ID : usermodul.getLoggedInWorkers()) {
				if (ID != workerID) {
					try {
						Worker worker = chatserver.getConnectionByID(ID);
						income.setResponse(message);
						((TCPWorker) worker).getChannel().send(income);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Error sending public message", e);
					}
				}
			}
			income.setResponse("");
		} else {
			income.setError("Permission denied, user not logged in!");
		}
		return income;
	}
}
