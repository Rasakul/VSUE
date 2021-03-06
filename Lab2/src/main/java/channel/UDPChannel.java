package channel;

import channel.util.DataPacket;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by lukas on 04.01.2016.
 */
public interface UDPChannel extends Closeable {
	/**
	 * Sending a {@link DataPacket} over the channel
	 *
	 * @param data packet for sending
	 *
	 * @throws IOException if an error occurs during the communication with the Socket
	 */
	void send(Object data) throws IOException;

	/**
	 * Receiving a {@link DataPacket} over the channel The method is a blocking I/O operation
	 *
	 * @return the received datapacket
	 *
	 * @throws IOException            if an error occurs during the communication with the Socket
	 * @throws ClassNotFoundException if the wrong type of {@link DataPacket} was received
	 */
	Object receive() throws IOException, ClassNotFoundException;
}
