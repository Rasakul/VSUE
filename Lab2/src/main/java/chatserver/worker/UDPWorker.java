package chatserver.worker;

import channel.SimpleUDPChannel;
import channel.util.DataPacket;
import channel.util.UDPDataPacket;
import chatserver.Chatserver;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Process the incoming UDP socket of the corresponding listener and manage and communicate with it
 */
public class UDPWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(UDPWorker.class.getName());

	private final Chatserver       chatserver;
	private final UDPDataPacket    udp_dataPacket;
	private final DatagramSocket   socket;
	private final PrintStream      userResponseStream;
	private final SimpleUDPChannel channel;


	private volatile boolean running = true;

	public UDPWorker(Chatserver chatserver, DataPacket dataPacket, PrintStream userResponseStream,
	                 DatagramSocket socket) {
		this.chatserver = chatserver;
		this.udp_dataPacket = (UDPDataPacket) dataPacket;
		this.userResponseStream = userResponseStream;
		this.socket = socket;
		this.channel = new SimpleUDPChannel(socket, udp_dataPacket.getHost(), udp_dataPacket.getPort());
	}

	@Override
	public void run() {

		LOGGER.fine(
				"Processing incoming UDP: " + udp_dataPacket.getCommand() + " from " + udp_dataPacket.getHost() + ":" +
				udp_dataPacket.getPort());

		try {

			switch (udp_dataPacket.getCommand()) {
				case "list":
					udp_dataPacket.setResponse(this.getOnlineUsers());
					break;
				default:
					udp_dataPacket.setError("unknown command");
			}
			channel.send(udp_dataPacket);

		} catch (IOException e) {
			if (running) LOGGER.log(Level.SEVERE, "Error on TCP Socket", e);
			socket.close();
		}
		running = false;
	}

	public String getOnlineUsers() {
		return chatserver.getUsermodul().getOnlineUsers();
	}

	@Override
	public void close() {
		LOGGER.info("Stopping UDP Worker");
		running = false;
		channel.close();
		if (socket != null) socket.close();
	}

	@Override
	public boolean isRunning() {
		return false;
	}
}
