package chatserver.util.operation;

import channel.util.DataPacket;

/**
 * Created by Lukas on 20.10.2015.
 */
public interface Operation {
	public DataPacket process(Integer workerID, DataPacket income);
}
