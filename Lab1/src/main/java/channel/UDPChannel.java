package channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Created by Lukas on 19.10.2015.
 */
public class UDPChannel implements Channel {
	private static final Logger LOGGER = Logger.getLogger(UDPChannel.class.getName());

	private final DatagramSocket socket;
	private final String         host;
	private final Integer        port;

	public UDPChannel(DatagramSocket socket, String host, Integer port) {
		this.host = host;
		this.port = port;

		this.socket = socket;
	}

	@Override
	public void send(String data) throws IOException {
		LOGGER.fine("sending: " + data);

		byte[] data_byte = data.getBytes();

		DatagramPacket packet_out = new DatagramPacket(data_byte, data_byte.length, InetAddress.getByName(host), port);
		socket.send(packet_out);
	}

	@Override
	public String receive() throws IOException {

		DatagramPacket packet_in = new DatagramPacket(new byte[1024], 1024);
		socket.receive(packet_in);
		String response = new String(packet_in.getData()).split("\u0000")[0];
		LOGGER.fine("receiving: " + response);
		return response;
	}

	@Override
	public void close() {
		//nothing to do
		LOGGER.info("Shutdown UDP channel");
	}
}
