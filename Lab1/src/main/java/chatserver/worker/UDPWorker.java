package chatserver.worker;

import channel.UDPChannel;
import channel.util.DataPacket;
import channel.util.UDPDataPacket;
import chatserver.Chatserver;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Lukas on 16.10.2015.
 */
public class UDPWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(UDPWorker.class.getName());

	private final Chatserver     chatserver;
	private final UDPDataPacket udp_dataPacket;
	private final DatagramSocket socket;
	private final PrintStream    userResponseStream;
	private final UDPChannel     channel;


	private volatile boolean running = true;

	public UDPWorker(Chatserver chatserver, DataPacket dataPacket, PrintStream userResponseStream,
	                 DatagramSocket socket) {
		this.chatserver = chatserver;
		this.udp_dataPacket = (UDPDataPacket) dataPacket;
		this.userResponseStream = userResponseStream;
		this.socket = socket;
		this.channel = new UDPChannel(socket, udp_dataPacket.getHost(), udp_dataPacket.getPort());
	}

	@Override
	public void run() {
		String command = udp_dataPacket.getCommand();

		LOGGER.fine(
				"Processing incoming UDP: " + command + " from " + udp_dataPacket.getHost() + ":" + udp_dataPacket.getPort());

		try {

			String response = (command.equals("list") ? this.getOnlineUsers() : "unknown command");
			UDPDataPacket dataPacket = new UDPDataPacket(response);
			channel.send(dataPacket);

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
		socket.close();
	}

	@Override
	public boolean isRunning() {
		return false;
	}
}
