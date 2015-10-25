package chatserver.worker;

import channel.TCPChannel;
import chatserver.Chatserver;
import chatserver.util.OperationFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Lukas on 16.10.2015.
 */
public class TCPWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(TCPWorker.class.getName());

	private final int ID;

	private final Chatserver       chatserver;
	private final Socket           clientSocket;
	private final PrintStream      userResponseStream;
	private final OperationFactory operationFactory;
	private final TCPChannel       channel;

	private volatile boolean running = true;
	private String  clienthost;
	private Integer clientport;

	public TCPWorker(int ID, Chatserver chatserver, Socket clientSocket, PrintStream userResponseStream) {
		this.ID = ID;
		this.chatserver = chatserver;
		this.clientSocket = clientSocket;
		this.userResponseStream = userResponseStream;

		this.operationFactory = new OperationFactory(chatserver);

		this.channel = new TCPChannel(clientSocket);
	}

	@Override
	public void run() {
		try {

			clienthost = clientSocket.getInetAddress().toString();
			clientport = clientSocket.getPort();

			LOGGER.info("New TCP Worker with ID " + ID + "with client " + clienthost + ":" + clientport);

			while (running) {
				String input = channel.receive();

				if (input == null || input.equals("quit")) this.terminate();

				if (running) {
					LOGGER.fine("Worker " + ID + ": " + input);
					String response = operationFactory.process(ID, input);
					channel.send(response);
				}
			}
		} catch (IOException e) {
			if (running) LOGGER.log(Level.SEVERE, "Error on TCP Socket", e);
		} finally {
			try {
				LOGGER.info("Stopping TCP Worker " + ID);
				clientSocket.close();
				running = false;
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Error closing TCP Socket", e);
			}
		}
		running = false;
	}

	@Override
	public void terminate() {
		try {
			LOGGER.info("Stopping TCP Worker " + ID);
			running = false;
			channel.terminate();
			clientSocket.close();
			chatserver.removeConnection(ID);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error closing TCP Socket", e);
		}
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	public TCPChannel getChannel() {
		return channel;
	}
}
