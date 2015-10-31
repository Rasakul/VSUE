package client;

import channel.Channel;
import channel.TCPChannel;
import channel.UDPChannel;
import channel.util.DataPacket;
import channel.util.TCPDataPacket;
import channel.util.UDPDataPacket;
import cli.Command;
import cli.Shell;
import client.communication.PrivateListener;
import client.communication.PublicListener;
import client.communication.UDPListener;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Client implements IClientCli, Runnable {
	private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

	private final    ExecutorService executor;
	private final    String          componentName;
	private final    String          host;
	private final    Integer         port_tcp;
	private final    Integer         port_udp;
	private          String          lastLookupAdress;
	private volatile boolean         lookupPerfomed;
	private volatile boolean         privateMsgSuccess;
	private boolean lookupError = false;
	private Channel         channel_tcp;
	private Channel         channel_udp;
	private Shell           shell;
	private PublicListener  activListener;
	private PrivateListener privateListener;
	private UDPListener     udpListener;
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
			executor.execute(shell);

			this.setupUDPServerConnection();

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "error open socket", e);
			userResponseStream.println("Error, server not reachable!");
			this.exit();
		}
	}

	private void setupTCPServerConnection() throws IOException {
		LOGGER.info("set up TCP connection to server");
		socket_tcp = new Socket(host, port_tcp);
		this.activListener = new PublicListener(this, socket_tcp, userResponseStream);
		this.channel_tcp = new TCPChannel(socket_tcp);
		executor.execute(activListener);
	}

	private void setupUDPServerConnection() throws IOException{
		LOGGER.info("set up UDP connection to server");
		socket_udp = new DatagramSocket();
		this.udpListener = new UDPListener(this, socket_udp, userResponseStream, host, port_udp);
		this.channel_udp = new UDPChannel(socket_udp, host, port_udp);
		executor.execute(udpListener);
	}

	private void closeTCPServerConnection() throws IOException {
		LOGGER.info("close TCP connection to server");
		if (channel_tcp != null) {
			ArrayList<String> args = new ArrayList<>();
			UDPDataPacket dataPacket = new UDPDataPacket("quit", args);
			channel_tcp.send(dataPacket);
		}
		if (activListener != null) activListener.close();
		if (channel_tcp != null) channel_tcp.close();
		if (socket_tcp != null) socket_tcp.close();
	}

	private void closeUDPServerConnection() throws IOException {
		LOGGER.info("close UDP connection to server");
		if (udpListener != null) udpListener.close();
		if (channel_udp != null) channel_udp.close();
		if (socket_udp != null) socket_udp.close();
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		if (!loggedIn) {

			this.setupTCPServerConnection();

			this.username = username;
			this.loggedIn = true;
			ArrayList<String> args = new ArrayList<>();
			args.add(username);
			args.add(password);
			TCPDataPacket dataPacket = new TCPDataPacket("login", args);
			channel_tcp.send(dataPacket);
		}
		return null;
	}

	@Override
	@Command
	public String logout() throws IOException {
		if (loggedIn) {
			loggedIn = false;
			ArrayList<String> args = new ArrayList<>();
			args.add(username);
			TCPDataPacket dataPacket = new TCPDataPacket("logout", args);
			channel_tcp.send(dataPacket);
			this.closeTCPServerConnection();
		} else {
			return "please log in first";
		}
		return null;
	}

	@Override
	@Command
	public String send(String message) throws IOException {
		if (loggedIn) {
			ArrayList<String> args = new ArrayList<>();
			args.add(message);
			TCPDataPacket dataPacket = new TCPDataPacket("send", args);
			channel_tcp.send(dataPacket);
		} else {
			return "please log in first";
		}
		return null;
	}

	@Override
	@Command
	public String list() throws IOException {
		ArrayList<String> args = new ArrayList<>();
		UDPDataPacket dataPacket = new UDPDataPacket("list", args);
		channel_udp.send(dataPacket);
		return null;
	}

	@Override
	@Command
	public String msg(String username, String message) throws IOException {
		if (loggedIn) {
			this.lookup(username);
			while (!lookupPerfomed) {
				//do nothing
			}
			if (!lookupError) {
				String[] split = lastLookupAdress.split(":");
				if (split.length == 2) {

					try {
						String host = split[0];
						String port = split[1];
						Socket socket = new Socket(host, Integer.parseInt(port));
						TCPChannel channel = new TCPChannel(socket);
						ArrayList<String> args = new ArrayList<>();
						DataPacket dataPacket = new TCPDataPacket("[private] " + username + ": " + message, args);
						channel.send(dataPacket);
						dataPacket = channel.receive();
						String response = dataPacket.getResponse();
						userResponseStream.println(response);
						privateMsgSuccess = false;
						channel.close();
						socket.close();
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			lookupPerfomed = false;
			lookupError = false;
		} else {
			return "please log in first";
		}
		return null;
	}

	@Override
	@Command
	public String lookup(String username) throws IOException {
		if (loggedIn) {
			ArrayList<String> args = new ArrayList<>();
			args.add(username);
			UDPDataPacket dataPacket = new UDPDataPacket("lookup", args);
			channel_tcp.send(dataPacket);
		} else {
			return "please log in first";
		}
		return lookupError ? "lookup error" : lastLookupAdress;
	}

	@Override
	@Command
	public String register(String privateAddress) throws IOException {
		if (loggedIn) {
			String[] split = privateAddress.split(":");
			if (split.length == 2) {
				String port = split[1];

				try {
					ServerSocket privateSocket = new ServerSocket(Integer.parseInt(port));
					privateListener = new PrivateListener(this, privateSocket, userResponseStream, executor);
					executor.execute(privateListener);

					ArrayList<String> args = new ArrayList<>();
					args.add(privateAddress);
					UDPDataPacket dataPacket = new UDPDataPacket("register", args);
					channel_tcp.send(dataPacket);

				} catch (IOException e) {
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
		if (loggedIn) {
			return this.lastMg;
		} else {
			return "please log in first";
		}
	}

	@Override
	@Command
	public String exit() {
		LOGGER.info("shutting down client");
		try {
			if (loggedIn) this.logout();
			if (privateListener != null) privateListener.close();
			this.closeUDPServerConnection();
			if (shell != null) shell.close();

			executor.shutdown();
		} catch (IOException ignored) {
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

	public void setPrivateMsgSuccess(boolean privateMsgSuccess) {
		this.privateMsgSuccess = privateMsgSuccess;
	}
}
