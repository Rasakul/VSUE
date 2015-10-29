package chatserver.worker;

import java.io.Closeable;

/**
 * Created by Lukas on 16.10.2015.
 */
public interface Worker extends Runnable, Closeable {

	public boolean isRunning();
}
