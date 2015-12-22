package chatserver.listener;

import java.io.Closeable;

/**
 * Interface for the listening on a blocking socket in a multithreading server
 */
public interface Serverlistener extends Runnable, Closeable {

}
