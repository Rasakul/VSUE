package channel.util;

import java.util.List;

/**
 * Abstract DTO class for the communication over a TCP socket
 */
public class TCPDataPacket extends DataPacket {
	public TCPDataPacket(String command, List<String> arguments) {
		super(command, arguments);
	}

	public TCPDataPacket(String response) {
		super(response);
	}
}
