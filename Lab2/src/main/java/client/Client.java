package client;

import channel.Base64Channel;
import channel.ObjectChannel;
import channel.SimpleUDPChannel;
import channel.UDPChannel;
import channel.util.TCPDataPacket;
import channel.util.UDPDataPacket;
import cli.Command;
import cli.Shell;
import client.communication.IntegrityChecker;
import client.communication.PrivateListener;
import client.communication.PublicListener;
import client.communication.UDPListener;
import client.security.ClientAuthenticator;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Client implements IClientCli, Runnable {
	private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

	private final String              componentName;
	private final String              host;
	private final Integer             port_tcp;
	private final Integer             port_udp;
	private       IntegrityChecker    integrityChecker;
	private       ClientAuthenticator authenticator;
	private       String              lastLookupAdress;
	private volatile boolean lookupPerfomed  = false;
	private volatile boolean registerSuccess = false;
	private volatile boolean registerError   = false;
	private volatile boolean loggedIn        = false;
	private volatile boolean serverdown      = false;
	private volatile boolean lookupError     = false;
	private ObjectChannel   channel_tcp;
	private UDPChannel      channel_udp;
	private Shell           shell;
	private PublicListener  activListener;
	private PrivateListener privateListener;
	private UDPListener     udpListener;
	private Socket          socket_tcp;
	private DatagramSocket  socket_udp;
	private String username = "";

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
		this.userResponseStream = userResponseStream;

		this.host = config.getString("chatserver.host");
		this.port_udp = config.getInt("chatserver.udp.port");
		this.port_tcp = config.getInt("chatserver.tcp.port");

		this.authenticator = new ClientAuthenticator(userResponseStream, config.getString("keys.dir"),
		                                             config.getString("chatserver.key"));

		try {
//			Random random = new Random();
//			if (random.nextInt() % 2 == 0) {
//				LOGGER.info("using correct hmac, counter: " + COUNTER);
//				this.integrityChecker = new IntegrityChecker(config.getString("hmac.key"));
//			} else {
//				LOGGER.info("using wrong hmac, counter: " + COUNTER);
//				this.integrityChecker = new IntegrityChecker("keys/wrongHmac.key");
//			}
			this.integrityChecker = new IntegrityChecker(config.getString("hmac.key"));
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
			exit();
		}

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
			Thread thread = new Thread(shell);
			thread.start();

			this.setupUDPServerConnection();

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "error open socket", e);
			userResponseStream.println("Error, server not reachable!");
			this.exit();
		}
	}

	private void setupTCPSocket() throws IOException {
		LOGGER.info("set up TCP socket to server");
		socket_tcp = new Socket(host, port_tcp);
	}

	private void setupTCPServerConnection(ObjectChannel objectChannel) throws IOException {
		LOGGER.info("set up TCP connection to server");
		this.activListener = new PublicListener(this, objectChannel, userResponseStream);
		this.channel_tcp = objectChannel;
		Thread thread = new Thread(activListener);
		thread.start();
	}

	private void setupUDPServerConnection() throws IOException {
		LOGGER.info("set up UDP connection to server");
		socket_udp = new DatagramSocket();
		this.udpListener = new UDPListener(this, socket_udp, userResponseStream, host, port_udp);
		this.channel_udp = new SimpleUDPChannel(socket_udp, host, port_udp);
		Thread thread = new Thread(udpListener);
		thread.start();
	}

	private void closeTCPServerConnection() throws IOException {
		LOGGER.info("close TCP connection to server");
		if (channel_tcp != null && !serverdown && loggedIn) {
			LOGGER.info("sending quit");
			channel_tcp.send(new TCPDataPacket("quit", new ArrayList<String>()));
		}
		if (activListener != null) activListener.close();
		if (channel_tcp != null) channel_tcp.close();
		if (socket_tcp != null) socket_tcp.close();
	}

	private void closeUDPServerConnection() throws IOException {
		LOGGER.info("close UDP connection to server");
		if (channel_udp != null) channel_udp.close();
		if (udpListener != null) udpListener.close();
		if (socket_udp != null) socket_udp.close();
	}

	@Deprecated
	@Override
	public String login(String username, String password) throws IOException {
		if (!loggedIn && !serverdown) {
			try {
				this.setupTCPSocket();
				this.setupTCPServerConnection(new ObjectChannel(socket_tcp));

				this.username = username;
				ArrayList<String> args = new ArrayList<>();
				args.add(username);
				args.add(password);
				channel_tcp.send(new TCPDataPacket("login", args));
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "problem with socket", e);
				return "Error, server not reachable!";
			}
		}
		return null;
	}

	@Override
	@Command
	public String logout() throws IOException {
		if (loggedIn && !serverdown) {
			ArrayList<String> args = new ArrayList<>();
			args.add(username);
			channel_tcp.send(new TCPDataPacket("logout", args));
			while (loggedIn && !serverdown) {

			}
			this.closeTCPServerConnection();
		} else {
			return "please log in first";
		}
		return null;
	}

	@Override
	@Command
	public String send(String message) throws IOException {
		if (loggedIn && !serverdown) {
			ArrayList<String> args = new ArrayList<>();
			args.add(message);
			channel_tcp.send(new TCPDataPacket("send", args));
		} else {
			return "please log in first";
		}
		return null;
	}

	@Override
	@Command
	public String list() throws IOException {
		channel_udp.send(new UDPDataPacket("list", new ArrayList<String>()));
		return null;
	}

	@Override
	@Command
	public String msg(String username, String message) throws IOException {
		if (loggedIn && !serverdown) {
			this.lookup(username);
			while (!lookupPerfomed && !serverdown) {
				//do nothing
			}
			if (!lookupError) {
				String[] split = lastLookupAdress.split(":");
				if (split.length == 2) {

					try {
						String host = split[0];
						String port = split[1];
						Socket socket = new Socket(host, Integer.parseInt(port));
						Base64Channel channel = new Base64Channel(socket);

						message = "!msg [private] " + username + ": " + message;
						message = integrityChecker.sign(message);
						channel.send(message.getBytes(StandardCharsets.UTF_8));
						LOGGER.info("send: " + message);

						byte[] bytes = channel.receive();
						String response = new String(bytes, StandardCharsets.UTF_8);
						LOGGER.info("received: " + response);
						split = response.split(" ");
						String text = response.replace(split[0], "");
						String command=split[1];

						if (integrityChecker.check(response)) {
							userResponseStream.println(text);
						} else {
							userResponseStream.println("the client answer was compromised");
							if (!command.equals("!tampered")) {
								String tempered = "!tampered " + text;
								tempered = integrityChecker.sign(tempered);
								LOGGER.info("send: " + tempered);
								channel.send(tempered.getBytes(StandardCharsets.UTF_8));
							}
						}
						registerSuccess = false;
						channel.close();
						socket.close();
					} catch (IOException e) {
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
		if (loggedIn && !serverdown) {
			ArrayList<String> args = new ArrayList<>();
			args.add(username);
			channel_tcp.send(new TCPDataPacket("lookup", args));
		} else {
			return "please log in first";
		}
		return lookupError ? "lookup error" : lastLookupAdress;
	}

	@Override
	@Command
	public String register(String privateAddress) throws IOException {
		if (!registerSuccess) {
			if (loggedIn && !serverdown) {
				String[] split = privateAddress.split(":");
				if (split.length == 2) {
					String port = split[1];

					try {
						ArrayList<String> args = new ArrayList<>();
						args.add(privateAddress);
						channel_tcp.send(new TCPDataPacket("register", args));

						while (!registerError && !registerSuccess && !serverdown) {

						}
						if (registerSuccess) {
							ServerSocket privateSocket = new ServerSocket(Integer.parseInt(port));
							privateListener =
									new PrivateListener(this, privateSocket, userResponseStream, integrityChecker);
							Thread thread = new Thread(privateListener);
							thread.start();
						}
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
		} else {
			return "already registered!";
		}
		return null;
	}

	@Override
	@Command
	public String lastMsg() throws IOException {
		if (loggedIn && !serverdown) {
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
			if (loggedIn && !serverdown) this.logout();
			if (privateListener != null) privateListener.close();
			this.closeUDPServerConnection();
			if (shell != null) shell.close();
		} catch (IOException ignored) {
		}

		return "shutdown complete";
	}

	@Override
	@Command
	public String authenticate(String username) throws IOException {
		if (!loggedIn && !serverdown) {
			try {
				this.username = username;
				this.setupTCPSocket();

			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "problem with socket", e);
				closeTCPServerConnection();
				return "Error, server not reachable!";
			}

			ObjectChannel objectChannel = authenticator.authenticate(socket_tcp, username);

			if (objectChannel != null && !authenticator.hasError()) {
				this.setupTCPServerConnection(objectChannel);
				setLoggedIn(true);
			} else {
				closeTCPServerConnection();
			}
		}
		return null;
	}

	public void setLastLookupAdress(String lastLookupAdress) {
		lookupPerfomed = true;
		this.lastLookupAdress = lastLookupAdress;
	}

	public void setLastMsg(String lastMg) {
		this.lastMg = lastMg;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public void setLookupError(boolean lookupError) {
		lookupPerfomed = true;
		this.lookupError = lookupError;
	}

	public void setRegisterSuccess(boolean registerSuccess) {
		this.registerSuccess = registerSuccess;
	}

	public void setServerdown(boolean serverdown) {
		this.serverdown = serverdown;
	}

	public void setRegisterError(boolean registerError) {
		this.registerError = registerError;
	}
}
