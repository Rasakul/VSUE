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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Please note that this class is not needed for Lab 1, but will later be used in Lab 2. Hence, you do not have to
 * implement it for the first submission.
 */
public class Nameserver implements INameserverCli, Runnable {

	private static final Logger LOGGER = Logger.getLogger(Nameserver.class.getName());
	private String      componentName;
	private Config      config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private Registry                 registry;
	private RemoteNameserverCallback remoteNamserverCallback;
	private INameserver              rootNameserver;

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

		shell = new Shell(this.componentName, userRequestStream, userResponseStream);
		shell.register(this);
		new Thread(shell).start();

		rootNameserver = null;
	}

	/**
	 * @param args the first argument is the name of the {@link Nameserver} component
	 */
	public static void main(String[] args) {
		Nameserver nameserver = new Nameserver(args[0], new Config(args[0]), System.in, System.out);

		nameserver.run();
	}


	@Override
	public void run() {
		if (!config.listKeys()
		           .contains(
				           "domain")) { // root nameserver, because its properties does not contain the domain property
			try {
				LOGGER.info("Creating registry");
				registry = LocateRegistry.createRegistry(
						config.getInt("registry.port")); // only root nameserver creates the registry

				remoteNamserverCallback = new RemoteNameserverCallback(); // callback class for remote functionality

				LOGGER.info("Creating remote object");
				INameserver remoteObject = (INameserver) UnicastRemoteObject.exportObject(remoteNamserverCallback,
				                                                                          0); // create a remote object of the server object

				LOGGER.info("Binding remote object");
				registry.bind(config.getString("root_id"),
				              remoteObject); //bind the obtained remote object on specified binding name in the registry

			} catch (RemoteException e) {
				LOGGER.log(Level.SEVERE, "Error while registering root domain.", e);
				closeNameserver();
			} catch (AlreadyBoundException e) {
				LOGGER.log(Level.SEVERE, "Error while registering root domain.", e);
				closeNameserver();
			}

			LOGGER.info("Root nameserver created.");


		} else { // not the root namesever
			try {
				LOGGER.info("Getting registry");
				registry = LocateRegistry.getRegistry(config.getString("registry.host"),
				                                      config.getInt("registry.port")); // getting registry

				LOGGER.info("Obtaining root nameserver from registry");
				rootNameserver = (INameserver) registry.lookup(
						config.getString("root_id")); // set root nameserver obtained from the registry

				remoteNamserverCallback = new RemoteNameserverCallback(); // callback class for remote functionality

				LOGGER.info("Creating remote object for nameserver");
				INameserver remoteObject = (INameserver) UnicastRemoteObject.exportObject(remoteNamserverCallback,
				                                                                          0); // create a remote object of the server object

				LOGGER.info("Register nameserver");
				rootNameserver.registerNameserver(config.getString("domain"), remoteObject,
				                                  remoteObject); // register nameserver recursively beginning at the root nameserver
			} catch (RemoteException e) {
				LOGGER.log(Level.SEVERE, "Error while registering domain: " + config.getString("domain"), e);
				closeNameserver();
			} catch (NotBoundException e) {
				LOGGER.log(Level.SEVERE, "Error while registering domain: " + config.getString("domain"), e);
				closeNameserver();
			} catch (AlreadyRegisteredException e) {
				LOGGER.log(Level.SEVERE, "Error while registering domain: " + config.getString("domain"), e);
				closeNameserver();
			} catch (InvalidDomainException e) {
				LOGGER.log(Level.SEVERE, "Error while registering domain: " + config.getString("domain"), e);
				closeNameserver();
			}

			LOGGER.info("Nameserver for hosting " + config.getString("domain") + " is created.");
		}

	}

	@Command
	@Override
	public String nameservers() throws IOException {
		String result = "";

		List<String> subHosts = remoteNamserverCallback.getSubHosts();
		Collections.sort(subHosts);

		for (String subHost : subHosts) {
			int index = subHosts.indexOf(subHost) + 1;
			result += index + ". " + subHost + "\n";
		}

		return result;
	}

	@Command
	@Override
	public String addresses() throws IOException {
		String result = "";

		HashMap<String, String> users = remoteNamserverCallback.getUsers();
		List<String> userNames = new ArrayList<>(users.keySet());

		Collections.sort(userNames);

		for (String userName : userNames) {
			int index = userNames.indexOf(userName) + 1;
			result += index + ". " + userName + " " + users.get(userName) + "\n";
		}

		return result;
	}

	public void closeNameserver(){
		try {
			userResponseStream.println("Please shut down this nameserver");
			this.exit();

		} catch (IOException e) {
			LOGGER.info(e.getMessage());
		}
	}

	@Command
	@Override
	public String exit() throws IOException {

		LOGGER.info("Closing nameserver");
		shell.close();

		try {
			// unexport the previously exported remote object
			UnicastRemoteObject.unexportObject(remoteNamserverCallback, true);
		} catch (NoSuchObjectException e) {
			LOGGER.log(Level.SEVERE, "Error while unexporting object.", e);
		}

		if (rootNameserver == null) { // root nameserver
			try {
				// unbind the remote object so that a client can't find it anymore
				registry.unbind(config.getString("root_id"));
			} catch (NotBoundException e) {
				LOGGER.log(Level.SEVERE, "Error while unbinding object.", e);
			}
		}

		return "Nameserver closed";
	}
}
