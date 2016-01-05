package channel;

import security.Base64Util;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Implementation of the BytChannel abstract class using Base64 Encoding
 */
public class Base64Channel extends ByteChannel {
	private static final Logger LOGGER = Logger.getLogger(Base64Channel.class.getName());

	public Base64Channel(Socket socket) {
		super(socket, new SimpleTCPChannel(socket));
	}

	@Override
	public void send(byte[] data) throws IOException {
		data = Base64Util.encodeBase64(data);
		getByteChannel().send(data);
	}

	@Override
	public byte[] receive() throws IOException {
		byte[] receive = getByteChannel().receive();
		receive = Base64Util.decodeBase64(receive);
		return receive;
	}

	@Override
	public void close() throws IOException {
		getByteChannel().close();
	}
}
