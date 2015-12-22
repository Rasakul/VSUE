package chatserver.listener;

import chatserver.Chatserver;
import chatserver.worker.TCPWorker;
import util.Config;

import java.io.IOException;
import java.io.PrintStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for the listening on a blocking TCP socket in a multithreading server
 */
public class TCPListener implements Serverlistener {
	private static final Logger LOGGER = Logger.getLogger(TCPListener.class.getName());

	private final Chatserver      chatserver;
	private final Config          server_config;
	private final PrintStream     userResponseStream;
	private final ExecutorService executor;

	private volatile boolean running = true;

	private ServerSocket server_socket;

	public TCPListener(Chatserver chatserver, Config server_config, PrintStream userResponseStream,
	                   ExecutorService executor) {
		this.chatserver = chatserver;
		this.server_config = server_config;
		this.userResponseStream = userResponseStream;
		this.executor = executor;
	}

	@Override
	public void run() {
		try {
			server_socket = new ServerSocket(server_config.getInt("tcp.port"));
			LOGGER.info("TCP is UP!");

			while (running) {
				Socket clientSocket = server_socket.accept();
				int id = Chatserver.WORKER_COUNTER++;
				TCPWorker worker_tcp = new TCPWorker(id, chatserver, clientSocket, userResponseStream);
				chatserver.addConnection(id, worker_tcp);
				executor.execute(worker_tcp);
			}
		} catch (BindException e) {
			userResponseStream.println("Error, another server use my ports! Please shut down");
		} catch (IOException e) {
			if (running) LOGGER.log(Level.SEVERE, "Error on TCP Socket", e);
		} finally {
			try {
				LOGGER.info("Stopping TCP Listener");
				if (server_socket != null) server_socket.close();
				running = false;
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Error closing TCP Socket", e);
			}
		}
		running = false;
	}

	public void close() {
		try {
			LOGGER.info("Stopping TCP Listener");
			running = false;
			if (server_socket != null) server_socket.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error closing TCP Socket", e);
		}
	}
}
