package channel;

import channel.util.DataPacket;
import security.Base64Util;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Class for the communication of Objects over a TCP socket Uses a {@link ByteChannel} for the underlying communication
 */
public class ObjectChannel implements Closeable {
	private static final Logger LOGGER = Logger.getLogger(ObjectChannel.class.getName());

	private ByteChannel byteChannel;

	public ObjectChannel(Socket socket) {
		this.byteChannel = new AESChannel(socket);
	}

	/**
	 * Sending a {@link DataPacket} over the byteChannel
	 *
	 * @param data packet for sending
	 *
	 * @throws IOException if an error occurs during the communication with the Socket
	 */
	public void send(DataPacket datagramPacket) throws IOException {
		LOGGER.fine("sending: " + datagramPacket);
		byteChannel.send(Base64Util.convertToBytes(datagramPacket));
	}

	/**
	 * Receiving a {@link DataPacket} over the byteChannel The method is a blocking I/O operation
	 *
	 * @return the received datapacket
	 *
	 * @throws IOException            if an error occurs during the communication with the Socket
	 * @throws ClassNotFoundException if an error occurs during casting to DataPacket
	 */
	public DataPacket receive() throws IOException, ClassNotFoundException {
		DataPacket dataPacket = (DataPacket) Base64Util.convertFromBytes(byteChannel.receive());
		LOGGER.fine("receive: " + dataPacket);
		return dataPacket;
	}

	@Override
	public void close() throws IOException {
		byteChannel.close();
	}

	public ByteChannel getByteChannel() {
		return byteChannel;
	}

	public void setByteChannel(ByteChannel byteChannel) {
		this.byteChannel = byteChannel;
	}
}
