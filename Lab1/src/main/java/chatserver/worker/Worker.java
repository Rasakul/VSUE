package chatserver.worker;

/**
 * Created by Lukas on 16.10.2015.
 */
public interface Worker extends Runnable{
    public void terminate();

    public boolean isRunning();
}
