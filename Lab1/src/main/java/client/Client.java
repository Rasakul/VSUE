package client;

import channel.Channel;
import channel.TCPChannel;
import channel.UDPChannel;
import cli.Command;
import cli.Shell;
import client.communication.PrivateListener;
import client.communication.PublicListener;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.ServerException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Client implements IClientCli, Runnable {
	private static final Logger LOGGER         = Logger.getLogger(Client.class.getName());
	public static        int    WORKER_COUNTER = 0;

	private final ExecutorService executor;
	private final String          componentName;
	private final String          host;
	private final Integer         port_tcp;
	private final Integer         port_udp;
	private       String          lastLookupAdress;
	private       boolean         lookupPerfomed;
	private boolean lookupError = false;
	private Channel         channel_tcp;
	private Channel         channel_udp;
	private Shell           shell;
	private PublicListener  activListener;
	private PrivateListener privateListener;
	private Socket          socket_tcp;
	private DatagramSocket  socket_udp;
	private String username = "";
	private volatile boolean loggedIn;

	private Config      config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private String lastMg = "No message received!";

	/**
	 * @param componentName      the name of the component - represented in the prompt
	 * @param config             the configuration to use
	 * @param userRequestStream  the input stream to read user input from
	 * @param userResponseStream the output stream to write the console output to
	 */
	public Client(String componentName, Config config, InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		this.executor = Executors.newCachedThreadPool();

		this.host = config.getString("chatserver.host");
		this.port_udp = config.getInt("chatserver.udp.port");
		this.port_tcp = config.getInt("chatserver.tcp.port");

		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
	}

	/**
	 * @param args the first argument is the name of the {@link Client} component
	 */
	public static void main(String[] args) {
		try {
			InputStream inputStream = Client.class.getResourceAsStream("/logging.properties");
			LogManager.getLogManager().readConfiguration(inputStream);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error setting Log-Properties", e);
		}

		Client client = new Client(args[0], new Config("client"), System.in, System.out);
		client.run();
	}

	@Override
	public void run() {

		try {
			LOGGER.info("starting client " + componentName);

			socket_tcp = new Socket(host, port_tcp);
			socket_udp = new DatagramSocket();

			this.activListener = new PublicListener(this, socket_tcp, userResponseStream);
			this.channel_tcp = new TCPChannel(socket_tcp);
			this.channel_udp = new UDPChannel(socket_udp, host, port_udp);

			executor.execute(shell);
			executor.execute(activListener);

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "error openen sockets", e);
			userResponseStream.println("Error, server not reachable!");
			this.exit();
		}
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		this.username = username;
		this.loggedIn = true;
		channel_tcp.send("login;" + username + ";" + password);
		return null;
	}

	@Override
	@Command
	public String logout() throws IOException {
		loggedIn = false;
		channel_tcp.send("logout;" + username);
		return null;
	}

	@Override
	@Command
	public String send(String message) throws IOException {
		channel_tcp.send("send;" + message);
		return null;
	}

	@Override
	@Command
	public String list() throws IOException {
		channel_udp.send("list");
		return channel_udp.receive();
	}

	@Override
	@Command
	public String msg(String username, String message) throws IOException {

		this.lookup(username);
		while (!lookupPerfomed) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ignored) {

			}
		}
		if (!lookupError) {
			String[] split = lastLookupAdress.split(":");
			if (split.length == 2) {
				String host = split[0];
				String port = split[1];
				Socket socket = new Socket(host, Integer.parseInt(port));
				TCPChannel channel = new TCPChannel(socket);
				channel.send("[private] " + username + ": " + message);
			}
		}
		lookupPerfomed = false;
		lookupError = false;
		return null;
	}

	@Override
	@Command
	public String lookup(String username) throws IOException {
		channel_tcp.send("lookup;" + username);
		return null;
	}

	@Override
	@Command
	public String register(String privateAddress) throws IOException {
		String[] split = privateAddress.split(":");
		if (loggedIn) {
			if (split.length == 2) {
				String port = split[1];

				try {

					ServerSocket privateSocket = new ServerSocket(Integer.parseInt(port));
					privateListener = new PrivateListener(this, privateSocket, userResponseStream, executor);
					executor.execute(privateListener);

					channel_tcp.send("register;" + privateAddress);

				} catch (ServerException e) {
					userResponseStream.println("Error with address: " + e.getMessage());
				}
			} else {
				LOGGER.log(Level.SEVERE, "wrong format: " + privateAddress);
				return "wrong format! need IP:Port";
			}
		} else {
			return "Permission denied, user not logged in!";
		}
		return null;
	}

	@Override
	@Command
	public String lastMsg() throws IOException {
		return this.lastMg;
	}

	@Override
	@Command
	public String exit() {
		LOGGER.info("shutting down client");
		try {
			if (loggedIn) this.logout();
			channel_tcp.send("quit");

			activListener.terminate();
			if (privateListener != null) privateListener.terminate();
			channel_udp.terminate();
			channel_tcp.terminate();
			socket_tcp.close();
			socket_udp.close();
			shell.close();
			executor.shutdown();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error closing TCP Socket", e);
		}

		return "shutdown complete";
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setLastLookupAdress(String lastLookupAdress) {
		lookupPerfomed = true;
		this.lastLookupAdress = lastLookupAdress;
	}

	public void setLastMsg(String lastMg) {
		this.lastMg = lastMg;
	}

	public void setLookupError(boolean lookupError) {
		lookupPerfomed = true;
		this.lookupError = lookupError;
	}
}
