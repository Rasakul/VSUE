package chatserver.worker;

import channel.TCPChannel;
import channel.util.DataPacket;
import channel.util.TCPDataPacket;
import chatserver.Chatserver;
import chatserver.util.OperationFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Process the incoming TCP socket of the corresponding listener and manage and communicate with it
 */
public class TCPWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(TCPWorker.class.getName());

	private final int ID;

	private final Chatserver       chatserver;
	private final Socket           clientSocket;
	private final PrintStream      userResponseStream;
	private final OperationFactory operationFactory;
	private final TCPChannel       channel;

	private volatile boolean running    = true;
	private          boolean clientquit = false;

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

			LOGGER.info("New TCP Worker with ID " + ID + "with client " + clientSocket.getInetAddress() + ":" +
			            clientSocket.getPort());

			while (running) {
				DataPacket dataPacket = (DataPacket) channel.receive();
				String input = dataPacket.getCommand();

				if (input == null || input.equals("quit")) {
					clientquit = true;
					this.close();
				}

				if (running) {
					LOGGER.fine("Worker " + ID + ": " + input);
					channel.send(operationFactory.process(ID, dataPacket));
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			if (running && !(e instanceof SocketException)) LOGGER.log(Level.SEVERE, "Error on TCP Socket", e);
		} finally {
			try {
				LOGGER.info("Stopping TCP Worker " + ID);
				chatserver.removeConnection(ID);
				chatserver.getUsermodul().logoutUser(ID);
				clientSocket.close();
				running = false;
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Error closing TCP Socket", e);
			}
		}
		running = false;
	}

	@Override
	public void close() {
		try {
			LOGGER.info("Stopping TCP Worker " + ID);
			running = false;
			if (!clientquit) channel.send(new TCPDataPacket("serverend", new ArrayList<String>()));
			channel.close();
			if (clientSocket != null) clientSocket.close();
			chatserver.getUsermodul().logoutUser(ID);
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
