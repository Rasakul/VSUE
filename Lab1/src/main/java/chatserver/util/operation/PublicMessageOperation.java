package chatserver.util.operation;

import chatserver.Chatserver;
import chatserver.util.Usermodul;
import chatserver.worker.TCPWorker;
import chatserver.worker.Worker;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created by Lukas on 21.10.2015.
 */
public class PublicMessageOperation implements Operation {
	private static final Logger LOGGER = Logger.getLogger(PublicMessageOperation.class.getName());

	private final Chatserver chatserver;

	public PublicMessageOperation(Chatserver chatserver) {

		this.chatserver = chatserver;
	}

	@Override
	public String process(Integer workerID, String line) {
		Usermodul usermodul = chatserver.getUsermodul();
		String message = line.replaceFirst(Pattern.quote("send;"), "");

		if (usermodul.isLogedin(workerID)) {

			String username = usermodul.getUser(workerID);
			message = "public:" + username + ": " + message;

			for (int ID : usermodul.getLoggedinWorkers()) {
				if (ID != workerID) {
					Worker worker = chatserver.getConnectionByID(ID);
					try {
						((TCPWorker) worker).getChannel().send(message);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Error sending public message", e);
					}
				}
			}
		} else {
			message = "Permission denied, user not logged in!";
		}
		return message;
	}
}
