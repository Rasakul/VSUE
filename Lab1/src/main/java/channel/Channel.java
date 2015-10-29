package channel;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Lukas on 19.10.2015.
 */
public interface Channel extends Closeable {
	public void send(String data) throws IOException;

	public String receive() throws IOException;
}
