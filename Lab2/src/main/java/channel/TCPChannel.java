package channel;

import channel.util.DataPacket;
import channel.util.TCPDataPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Implementation of the {@link Channel} interface for abstraction the communication over a TCP socket
 */
public class TCPChannel implements Channel {
	private static final Logger LOGGER = Logger.getLogger(TCPChannel.class.getName());

	private final Socket socket;

	public TCPChannel(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void send(DataPacket data) throws IOException {
		LOGGER.fine("sending: " + data);
		ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
		outputStream.writeObject(data);
	}

	@Override
	public DataPacket receive() throws IOException, ClassNotFoundException {
		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		DataPacket response = (TCPDataPacket) inStream.readObject();
		LOGGER.fine("receiving: " + response);
		return response;
	}

	@Override
	public void close() throws IOException {
		//nothing to do
		LOGGER.info("Shutdown TCP channel");
	}
}
