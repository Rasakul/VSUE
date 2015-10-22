package chatserver.util.operation;

import chatserver.Chatserver;

/**
 * Created by Lukas on 21.10.2015.
 */
public class LastMsgOperation implements Operation {


	private final Chatserver chatserver;

	public LastMsgOperation(Chatserver chatserver) {

		this.chatserver = chatserver;
	}

	@Override
	public String process(Integer workerID, String line) {
		if (chatserver.getUsermodul().isLogedin(workerID)) {
			return chatserver.getLastMsg();
		} else {
			return "Permission denied, user not logged in!";
		}
	}
}
