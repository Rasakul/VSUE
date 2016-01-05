package chatserver.util.operation;

import channel.util.DataPacket;
import chatserver.Chatserver;
import chatserver.util.Usermodul;
import nameserver.INameserverForChatserver;
import nameserver.helper.DomainResolver;

import java.rmi.RemoteException;

/**
 * respond the registered address of an user, if the arguments are valid, the user is logged and the requested user is registered
 */
public class LookupOperation implements Operation {
	private final Chatserver chatserver;

	public LookupOperation(Chatserver chatserver) {

		this.chatserver = chatserver;
	}

	@Override
	public DataPacket process(Integer workerID, DataPacket income) {

		Usermodul usermodul = chatserver.getUsermodul();
		if (usermodul.isLoggedIn(workerID)) {
			String username;

			if (income.getArguments().size() == 1) {
				username = income.getArguments().get(0);

				// looking up for a private user address iteratively
				try {
					DomainResolver usernameDomain = new DomainResolver(username);

					INameserverForChatserver currentNameserver = chatserver.getRootNameserver();

					//going through the nameservers until user userdomain is resolved and address has been found
					while (usernameDomain.hasSubdomain()){
						currentNameserver = currentNameserver.getNameserver(usernameDomain.getZone());
						usernameDomain = new DomainResolver(usernameDomain.getSubdomain());
					}

					income.setResponse(currentNameserver.lookup(usernameDomain.getDomainName()));

				} catch (RemoteException e) {
					income.setError(e.getMessage());
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
