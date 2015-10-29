package chatserver;

import chatserver.listener.Serverlistener;
import chatserver.listener.TCPListener;
import chatserver.listener.UDPListener;
import chatserver.util.Usermodul;
import chatserver.worker.Worker;
import cli.Command;
import cli.Shell;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Chatserver implements IChatserverCli, Runnable {
	private static final Logger LOGGER = Logger.getLogger(Chatserver.class.getName());

	public static int WORKER_COUNTER = 0;

	private String         componentName;
	private Config         server_config;
	private InputStream    userRequestStream;
	private PrintStream    userResponseStream;
	private Serverlistener TCPListener;
	private Serverlistener UDPListener;
	private Shell          shell;
	private Usermodul      usermodul;

	private ExecutorService executor;

	private boolean running = true;

	private Hashtable<Integer, Worker> openConnections;

	/**
	 * @param componentName      the name of the component - represented in the prompt
	 * @param server_config      the configuration to use
	 * @param userRequestStream  the input stream to read user input from
	 * @param userResponseStream the output stream to write the console output to
	 */
	public Chatserver(String componentName, Config server_config, InputStream userRequestStream,
	                  PrintStream userResponseStream) {
		this.componentName = componentName;
		this.server_config = server_config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		openConnections = new Hashtable<>();
		executor = Executors.newCachedThreadPool();
		usermodul = new Usermodul();

		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
	}

	/**
	 * @param args the first argument is the name of the {@link Chatserver}
	 *             component
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		try {
			InputStream inputStream = Chatserver.class.getResourceAsStream("/logging.properties");
			LogManager.getLogManager().readConfiguration(inputStream);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error setting Log-Properties", e);
		}

		Chatserver chatserver = new Chatserver(args[0], new Config("chatserver"), System.in, System.out);
		chatserver.run();
	}

	@Override
	public void run() {
		LOGGER.info("Starting Server " + componentName);

		TCPListener = new TCPListener(this, server_config, userResponseStream, executor);
		UDPListener = new UDPListener(this, server_config, userResponseStream, executor);

		executor.execute(shell);
		executor.execute(TCPListener);
		executor.execute(UDPListener);
	}

	@Override
	@Command
	public String users() throws IOException {
		return usermodul.getUserString();
	}

	@Override
	@Command
	public String exit() throws IOException {
		LOGGER.info("Shutting down " + componentName);
		running = false;
		for (Worker worker : this.openConnections.values()) {
			if (worker.isRunning()) {
				worker.close();
			}
		}
		TCPListener.close();
		UDPListener.close();
		executor.shutdown();
		shell.close();
		return "Stopping server";
	}

	public synchronized void addConnection(Integer ID, Worker worker) {openConnections.put(ID, worker); }

	public synchronized void removeConnection(Integer ID) {openConnections.remove(ID); }

	public synchronized Worker getConnectionByID(Integer ID) {return openConnections.get(ID);}

	public Usermodul getUsermodul() {return usermodul; }
}
