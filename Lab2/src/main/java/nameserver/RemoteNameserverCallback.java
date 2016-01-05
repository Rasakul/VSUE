package nameserver;

import nameserver.exceptions.AlreadyRegisteredException;
import nameserver.exceptions.InvalidDomainException;
import nameserver.helper.DomainResolver;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * This class encapsulates the remote functionality for registering a user address and for looking up for the private
 * address of a user. Furthermore contains this class the opportunity to register nameservers.
 *
 * @author David Molnar
 * @since 04.01.2016
 */
public class RemoteNameserverCallback implements INameserver {

	private static final Logger LOGGER = Logger.getLogger(RemoteNameserverCallback.class.getName());

	private ConcurrentHashMap<String, INameserver> subHosts;
			// references to the underlying nameservers (hosted domain bzw. nameserver object)
	private ConcurrentHashMap<String, String>      users; // registered users for this nameserver

	public RemoteNameserverCallback() {
		subHosts = new ConcurrentHashMap<>();
		users = new ConcurrentHashMap<>();
	}

	@Override
	public void registerNameserver(String domain, INameserver nameserver,
	                               INameserverForChatserver nameserverForChatserver)
			throws RemoteException, AlreadyRegisteredException, InvalidDomainException {
		LOGGER.info("Trying to register nameserver with domain: " + domain);

		DomainResolver domainToRegister = new DomainResolver(domain);

		if (!domainToRegister.isValidDomain()) { // invalid domain name
			throw new InvalidDomainException("Invalid domain name.");
		} else if (domainToRegister.hasSubdomain()) { // domain contains a subdomain, resolution possible
			subHosts.get(domainToRegister.getZone())
			        .registerNameserver(domainToRegister.getSubdomain(), nameserver, nameserverForChatserver);
		} else if (subHosts.keySet()
		                   .contains(domainToRegister.getZone())) { // domain already registered to this nameserver
			throw new AlreadyRegisteredException("Domain already registered");
		} else { // resolution is no longer possible, store domain and nameserver
			subHosts.put(domainToRegister.getZone(), nameserver);
			LOGGER.info("Successfully registered domain: " + domainToRegister.getDomainName());
		}
	}

	@Override
	public void registerUser(String username, String address)
			throws RemoteException, AlreadyRegisteredException, InvalidDomainException {
		LOGGER.info("Trying to register user with username: " + username + ", with address: " + address);

		DomainResolver userdomainToRegister = new DomainResolver(username);

		if (!userdomainToRegister.isValidDomain()) { // invalid userdomain
			throw new InvalidDomainException("Invalid userdomain.");
		} else if (userdomainToRegister.hasSubdomain()) { // userdomain contains a subdomain, resolution possible
			subHosts.get(userdomainToRegister.getZone()).registerUser(userdomainToRegister.getSubdomain(), address);
		} else if (users.keySet().contains(username)) { // user already registered to this nameserver
			throw new AlreadyRegisteredException("User already registered");
		} else { // resolution is no longer possible, store username and address
			users.put(username, address);
			LOGGER.info("Successfully registered user: " + username + ", with address: " + address);
		}
	}

	@Override
	public INameserverForChatserver getNameserver(String zone) throws RemoteException {
		if (subHosts.keySet().contains(zone)) {
			return subHosts.get(zone);
		} else {
			throw new RemoteException(zone + " has not been registered yet.");
		}
	}

	@Override
	public String lookup(String username) throws RemoteException {
		if (users.keySet().contains(username)) {
			return users.get(username);
		} else {
			return username + " has not been registered yet.";
		}
	}

	/**
	 * Returns the list of subdomains referenced in this nameserver
	 *
	 * @return list of subdomains
	 */
	public List<String> getSubHosts() {
		List<String> result = new ArrayList<>();
		result.addAll(subHosts.keySet());

		return result;
	}

	/**
	 * Returns the users (usernam, address)
	 *
	 * @return a map of username and address pairs
	 */
	public HashMap<String, String> getUsers() {
		HashMap<String, String> result = new HashMap<>();
		result.putAll(users);

		return result;
	}
}
