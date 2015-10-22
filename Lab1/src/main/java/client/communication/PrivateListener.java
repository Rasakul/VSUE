package client.communication;

import channel.TCPChannel;
import chatserver.Chatserver;
import chatserver.worker.TCPWorker;
import client.Client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created by Lukas on 22.10.2015.
 */
public class PrivateListener implements ClientCommunication {
	private static final Logger LOGGER = Logger.getLogger(PrivateListener.class.getName());

	private final Client       client;
	private final ServerSocket serverSocket;
	private final PrintStream  userResponseStream;
	private final ExecutorService executor;

	private volatile boolean running = true;

	public PrivateListener(Client client, ServerSocket serverSocket, PrintStream userResponseStream,
	                       ExecutorService executor) {
		this.client = client;
		this.serverSocket = serverSocket;
		this.userResponseStream = userResponseStream;
		this.executor = executor;
	}

	@Override
	public void run() {
		try {
			LOGGER.info("Private TCP listening activ");
			while (running) {
				Socket clientSocket = serverSocket.accept();
				TCPChannel channel = new TCPChannel(clientSocket);
				String message = channel.receive().replaceFirst(Pattern.quote("msg;"), "");
				userResponseStream.println(message);
				channel.send("!ack");
				channel.terminate();
				clientSocket.close();
			}
		} catch (IOException e) {
			if (running) LOGGER.log(Level.SEVERE, "Error on TCP Socket", e);
		} finally {
			try {
				LOGGER.info("Stopping TCP Listener");
				serverSocket.close();
				running = false;
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Error closing TCP Socket", e);
			}
		}
		running = false;
	}

	@Override
	public String terminate() {
		try {
			LOGGER.info("closing TCP socket");
			running = false;
			serverSocket.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "error closing tcp serverSocket", e);
		}
		return null;
	}
}