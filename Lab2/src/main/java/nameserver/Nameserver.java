package nameserver;

import cli.Command;
import cli.Shell;
import nameserver.exceptions.AlreadyRegisteredException;
import nameserver.exceptions.InvalidDomainException;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Please note that this class is not needed for Lab 1, but will later be used
 * in Lab 2. Hence, you do not have to implement it for the first submission.
 */
public class Nameserver implements INameserverCli, Runnable {

	private String      componentName;
	private Config      config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;

	private static final Logger LOGGER = Logger.getLogger(Nameserver.class.getName());

	private Registry registry;
	private RemoteNameserverCallback remoteNamserverCallback;
	private INameserver rootNameserver;

	private Shell shell;

	/**
	 * @param componentName      the name of the component - represented in the prompt
	 * @param config             the configuration to use
	 * @param userRequestStream  the input stream to read user input from
	 * @param userResponseStream the output stream to write the console output to
	 */
	public Nameserver(String componentName, Config config, InputStream userRequestStream,
	                  PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		// TODO
		shell = new Shell(this.componentName,userRequestStream,userResponseStream);
		shell.register(this);
		new Thread(shell).start();

		rootNameserver = null;
	}

	/**
	 * @param args the first argument is the name of the {@link Nameserver}
	 *             component
	 */
	public static void main(String[] args) {
		Nameserver nameserver = new Nameserver(args[0], new Config(args[0]), System.in, System.out);
		// TODO: start the nameserver
		nameserver.run();
	}


	@Override
	public void run() {
		// TODO
		if (config.listKeys().contains("domain")){ // non-root nameserver
			try {
				registry = LocateRegistry.getRegistry(config.getString("registry.host"),config.getInt("registry.port"));

				rootNameserver = (INameserver) registry.lookup(config.getString("root_id"));

				remoteNamserverCallback = new RemoteNameserverCallback();

				INameserver remoteObject = (INameserver) UnicastRemoteObject.exportObject(remoteNamserverCallback, 0);

				rootNameserver.registerNameserver(config.getString("domain"),remoteObject,remoteObject);
			} catch (RemoteException e) {
				LOGGER.log(Level.SEVERE, "Error while registering domain: " + config.getString("domain"), e);
			} catch (NotBoundException e) {
				LOGGER.log(Level.SEVERE, "Error while registering domain: " + config.getString("domain"), e);
			} catch (AlreadyRegisteredException e) {
				LOGGER.log(Level.SEVERE, "Error while registering domain: " + config.getString("domain"), e);
			} catch (InvalidDomainException e) {
				LOGGER.log(Level.SEVERE, "Error while registering domain: " + config.getString("domain"), e);
			}

			LOGGER.info("Nameserver for hosting " + config.getString("domain") + " is created.");

		} else { // root nameserver
			try {
				registry = LocateRegistry.createRegistry(config.getInt("registry.port"));

				remoteNamserverCallback = new RemoteNameserverCallback();

				INameserver remoteObject = (INameserver) UnicastRemoteObject.exportObject(remoteNamserverCallback, 0);

				registry.bind(config.getString("root_id"),remoteObject);

			} catch (RemoteException e) {
				LOGGER.log(Level.SEVERE, "Error while registering root domain.", e);
			} catch (AlreadyBoundException e) {
				LOGGER.log(Level.SEVERE, "Error while registering root domain.", e);
			}

			LOGGER.info("Root nameserver created.");
		}

	}

	@Command
	@Override
	public String nameservers() throws IOException {
		// TODO Auto-generated method stub
		String result = "";

		List<String> subHosts = remoteNamserverCallback.getSubHosts();
		Collections.sort(subHosts);

		for (String subHost: subHosts){
			int index = subHosts.indexOf(subHost) + 1;
			result +=  index + ". " + subHost + "\n";
		}

		return result;
	}

	@Command
	@Override
	public String addresses() throws IOException {
		// TODO Auto-generated method stub
		String result = "";

		HashMap<String,String> users = remoteNamserverCallback.getUsers();
		List<String> userNames = new ArrayList<>(users.keySet());

		Collections.sort(userNames);

		for (String userName: userNames){
			int index = userNames.indexOf(userName) + 1;
			result += index + ". " + userName + " " + users.get(userName) + "\n";

		}

		return result;
	}

	@Command
	@Override
	public String exit() throws IOException {

		shell.close();

		try {
			// unexport the previously exported remote object
			UnicastRemoteObject.unexportObject(remoteNamserverCallback, true);
		} catch (NoSuchObjectException e) {
			LOGGER.log(Level.SEVERE, "Error while unexporting object.", e);
		}

		if (rootNameserver == null){
			try {
				// unbind the remote object so that a client can't find it anymore
				registry.unbind(config.getString("root_id"));
			} catch (NotBoundException e) {
				LOGGER.log(Level.SEVERE, "Error while unbinding object.", e);
			}
		}

		return "";
	}

}
