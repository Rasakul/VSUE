package nameserver;

import nameserver.exceptions.AlreadyRegisteredException;
import nameserver.exceptions.InvalidDomainException;
import nameserver.helper.Domain;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by David on 04.01.2016.
 */
public class RemoteNameserverCallback implements INameserver {

    private static final Logger LOGGER = Logger.getLogger(Nameserver.class.getName());

    private ConcurrentHashMap<String, INameserver> subHosts;
    private ConcurrentHashMap<String,String> users;

    public RemoteNameserverCallback() {
        subHosts = new ConcurrentHashMap<>();
        users = new ConcurrentHashMap<>();
    }

    @Override
    public void registerNameserver(String domain, INameserver nameserver, INameserverForChatserver nameserverForChatserver) throws RemoteException, AlreadyRegisteredException, InvalidDomainException {
        LOGGER.info("Registering nameserver with domain: " + domain);

        Domain domainToRegister = new Domain(domain);



        if (domainToRegister.hasSubdomain()){
            subHosts.get(domainToRegister.getZone()).registerNameserver(domainToRegister.getSubdomain(),nameserver,nameserverForChatserver);
        } else if (subHosts.keySet().contains(domainToRegister.getZone())){
            throw new AlreadyRegisteredException("Domain already registered");
        }else {
            subHosts.put(domainToRegister.getZone(),nameserver);
        }
    }

    @Override
    public void registerUser(String username, String address) throws RemoteException, AlreadyRegisteredException, InvalidDomainException {
        LOGGER.info("Registering user with username: " + username + " and address: " + address);

        Domain domainToRegister = new Domain(username);



        if (domainToRegister.hasSubdomain()){
            subHosts.get(domainToRegister.getZone()).registerUser(domainToRegister.getSubdomain(), address);
        } else if (users.keySet().contains(username)){
            throw new AlreadyRegisteredException("Domain already registered");
        }else {
            users.put(username,address);
        }
    }

    @Override
    public INameserverForChatserver getNameserver(String zone) throws RemoteException {
        if (subHosts.keySet().contains(zone)){
            return subHosts.get(zone);
        } else {
            throw new RemoteException(zone + " has not been registered yet.");
        }
    }

    @Override
    public String lookup(String username) throws RemoteException {
        if (users.keySet().contains(username)){
            return users.get(username);
        } else {
            throw new RemoteException(username + " has not been registered yet.");
        }
    }

    public List<String> getSubHosts(){
        List<String> result = new ArrayList<>();
        result.addAll(subHosts.keySet());

        return result;
    }

    public HashMap<String, String> getUsers(){
        HashMap<String,String> result = new HashMap<>();
        result.putAll(users);

        return result;
    }
}
