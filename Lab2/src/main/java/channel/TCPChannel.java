package channel;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Implementation of the {@link Channel} interface for abstraction the communication over a TCP socket
 */
public class TCPChannel implements Channel {
	private static final Logger LOGGER = Logger.getLogger(TCPChannel.class.getName());

	private final Base64Channel base64Channel;

	public TCPChannel(Socket socket) {
		base64Channel = new Base64Channel(socket);
	}


	@Override
	public void send(Object data) throws IOException {
		LOGGER.fine("sending: " + data);
		//		ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
		//		outputStream.writeObject(data);
		base64Channel.send(data);
	}

	@Override
	public Object receive() throws IOException, ClassNotFoundException {
		//		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		//		DataPacket response = (TCPDataPacket) inStream.readObject();
		Object response = base64Channel.receive();
		LOGGER.fine("receiving: " + response);
		return response;
	}

	@Override
	public void close() throws IOException {
		//nothing to do
		LOGGER.info("Shutdown TCP channel");
	}
}
