package channel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@link ByteChannel} interface for abstraction the byte communication over a TCP socket using no
 * further encryption
 */
public class SimpleTCPChannel extends ByteChannel {
	private static final Logger LOGGER = Logger.getLogger(SimpleTCPChannel.class.getName());

	public SimpleTCPChannel(Socket socket) {
		super(socket, null);
	}


	@Override
	public void send(byte[] data) throws IOException {
		DataOutputStream dOut = new DataOutputStream(getSocket().getOutputStream());

		dOut.writeInt(data.length); // write length of the message
		LOGGER.log(Level.FINE, "send " + data.length + " bytes");
		dOut.write(data);           // write the message
	}

	@Override
	public byte[] receive() throws IOException {
		DataInputStream dIn = new DataInputStream(getSocket().getInputStream());

		int length = dIn.readInt();                    // read length of incoming message
		LOGGER.log(Level.FINE, "receive " + length + " bytes");
		if (length > 0) {
			byte[] receive = new byte[length];
			dIn.readFully(receive, 0, receive.length); // read the message
			return receive;
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		//nothing to do
		LOGGER.info("Shutdown TCP channel");
	}
}
