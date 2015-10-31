package channel.util;

import java.util.List;

/**
 * Created by Lukas on 19.10.2015.
 */
public class TCPDataPacket extends DataPacket {

	public TCPDataPacket(String command, List<String> arguments) {
		super(command, arguments);
	}

	public TCPDataPacket(String response) {
		super(response);
	}
}
