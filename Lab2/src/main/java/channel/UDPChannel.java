package channel;

import channel.util.DataPacket;
import channel.util.UDPDataPacket;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Implementation of the {@link Channel} interface for abstraction the communication over a UDP socket
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
	public void send(Object dataPacket) throws IOException {
		LOGGER.fine("sending: " + dataPacket);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(outputStream);
		os.writeObject(dataPacket);
		byte[] data = outputStream.toByteArray();

		DatagramPacket packet_out = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
		socket.send(packet_out);
	}

	@Override
	public Object receive() throws IOException, ClassNotFoundException {

		DatagramPacket packet_in = new DatagramPacket(new byte[1024], 1024);
		socket.receive(packet_in);
		byte[] data = packet_in.getData();

		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		UDPDataPacket dataPacket = (UDPDataPacket) is.readObject();

		dataPacket.setPort(packet_in.getPort());
		dataPacket.setHost(packet_in.getAddress().getHostAddress());
		LOGGER.fine("receiving: " + dataPacket);
		return dataPacket;
	}

	@Override
	public void close() {
		//nothing to do
		LOGGER.info("Shutdown UDP channel");
	}
}
