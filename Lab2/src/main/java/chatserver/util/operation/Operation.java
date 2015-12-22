package chatserver.util.operation;

import channel.util.DataPacket;

/**
 * Created by Lukas on 20.10.2015.
 */
public interface Operation {

	/**
	 * process an {@link DataPacket}
	 *
	 * @param workerID ID of the worker who request the operation
	 * @param income   incoming datapaket to process
	 *
	 * @return a datapacket with filled in response or error message
	 */
	public DataPacket process(Integer workerID, DataPacket income);
}
