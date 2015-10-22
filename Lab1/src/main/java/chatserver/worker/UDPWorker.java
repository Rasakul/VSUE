package chatserver.worker;

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
	private final DatagramPacket packet_in;
	private final PrintStream    userResponseStream;
	private final DatagramSocket socket;

	private volatile boolean running = true;

	public UDPWorker(Chatserver chatserver, DatagramPacket packet_in, PrintStream userResponseStream,
	                 DatagramSocket socket) {
		this.chatserver = chatserver;
		this.packet_in = packet_in;
		this.userResponseStream = userResponseStream;
		this.socket = socket;
	}

	@Override
	public void run() {
		byte[] data = packet_in.getData();
		String command = new String(data, 0, packet_in.getLength());

		LOGGER.fine(
				"Processing incoming UDP: " + command + " from " + packet_in.getAddress() + ":" + packet_in.getPort());

		try {

			byte[] response = (command.equals("list") ? this.getOnlineUsers() : "unknown command").getBytes();
			DatagramPacket packet_out = new DatagramPacket(response, response.length, packet_in.getAddress(),
			                                               packet_in.getPort());

			socket.send(packet_out);

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
	public void terminate() {
		LOGGER.info("Stopping UDP Worker");
		running = false;
		socket.close();
	}

	@Override
	public boolean isRunning() {
		return false;
	}
}
