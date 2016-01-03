package channel;

import org.bouncycastle.util.encoders.Base64;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lukas on 03.01.2016.
 */
public class Base64Channel implements Channel {
	private static final Logger LOGGER = Logger.getLogger(TCPChannel.class.getName());
	private final Socket socket;

	public Base64Channel(Socket socket) {

		this.socket = socket;
	}

	@Override
	public void send(Object data) throws IOException {
		byte[] message = convertToBytes(data);
		message = encodeBase64(message);

		DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

		dOut.writeInt(message.length); // write length of the message
		LOGGER.log(Level.FINE, "send " + message.length + " bytes");
		dOut.write(message);           // write the message
	}

	@Override
	public Object receive() throws IOException, ClassNotFoundException {
		DataInputStream dIn = new DataInputStream(socket.getInputStream());

		int length = dIn.readInt();                    // read length of incoming message
		LOGGER.log(Level.FINE, "receive " + length + " bytes");
		if (length > 0) {
			byte[] message = new byte[length];
			dIn.readFully(message, 0, message.length); // read the message

			message = decodeBase64(message);
			return convertFromBytes(message);
		}
		return null;
	}

	@Override
	public void close() throws IOException {

	}

	private Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		     ObjectInput in = new ObjectInputStream(bis)) {
			return in.readObject();
		}
	}

	private byte[] convertToBytes(Object object) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
		     ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(object);
			return bos.toByteArray();
		}
	}

	private byte[] encodeBase64(byte[] bytes) {
		LOGGER.log(Level.FINE, "encodeBase64");
		return Base64.encode(bytes);
	}

	private byte[] decodeBase64(byte[] bytes) {
		LOGGER.log(Level.FINE, "decodeBase64");
		return Base64.decode(bytes);
	}
}
