package chatserver.util.operation;

/**
 * Created by Lukas on 20.10.2015.
 */
public interface Operation {
	public String process(Integer workerID, String line);
}
