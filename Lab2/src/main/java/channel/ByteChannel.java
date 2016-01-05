package channel;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/**
 * Interface for abstraction of the byte communication with a socket, using the Decorator Pattern
 */
public abstract class ByteChannel implements Closeable {
	private final Socket      socket;
	private       ByteChannel byteChannel;

	public ByteChannel(Socket socket, ByteChannel byteChannel) {
		this.socket = socket;
		this.byteChannel = byteChannel;
	}

	/**
	 * Sending a byte array over the byteChannel
	 *
	 * @param bytes array for sending
	 *
	 * @throws IOException if an error occurs during the communication with the Socket
	 */
	public abstract void send(byte[] bytes) throws IOException;

	/**
	 * Receiving a byte array over the byteChannel The method is a blocking I/O operation
	 *
	 * @return the received bytes
	 *
	 * @throws IOException if an error occurs during the communication with the Socket
	 */
	public abstract byte[] receive() throws IOException;

	public Socket getSocket() {
		return socket;
	}

	public ByteChannel getByteChannel() {
		return byteChannel;
	}
}
