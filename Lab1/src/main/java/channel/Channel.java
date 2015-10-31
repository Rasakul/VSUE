package channel;

import channel.util.DataPacket;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Lukas on 19.10.2015.
 */
public interface Channel extends Closeable {
	void send(DataPacket data) throws IOException;

	DataPacket receive() throws IOException, ClassNotFoundException;
}
