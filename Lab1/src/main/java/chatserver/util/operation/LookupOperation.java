package chatserver.util.operation;

import channel.util.DataPacket;
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
	public DataPacket process(Integer workerID, DataPacket income) {

		Usermodul usermodul = chatserver.getUsermodul();
		if (usermodul.isLogedin(workerID)) {
			String username;

			if (income.getArguments().size() == 1) {
				username = income.getArguments().get(0);
				if (usermodul.isRegisterd(username)) {
					income.setResponse("lookup:" + usermodul.getAdress(username));
				} else {
					income.setResponse("lookuperror:User not registered!");
				}
			} else {
				income.setResponse("lookuperror:Invalid command!");
			}
		} else {
			income.setResponse("lookuperror:Permission denied, user not logged in!");
		}
		return income;
	}
}
