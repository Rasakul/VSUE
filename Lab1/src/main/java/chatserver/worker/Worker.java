package chatserver.worker;

import java.io.Closeable;

/**
 * Process the incoming socket of the corresponding listener and manage and communicate with it
 */
public interface Worker extends Runnable, Closeable {

	public boolean isRunning();
}
