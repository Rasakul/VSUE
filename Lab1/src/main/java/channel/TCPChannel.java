package channel;

import channel.util.DataPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by Lukas on 19.10.2015.
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
		DataPacket response = (DataPacket) inStream.readObject();
		LOGGER.fine("receiving: " + response);
		return response;
	}

	@Override
	public void close() throws IOException {
		//nothing to do
		LOGGER.info("Shutdown TCP channel");
	}
}
