package channel.util;

/**
 * Created by Lukas on 19.10.2015.
 */
public class UDPDataPacket extends DataPacket {
	private final String address;
	private final int    port;

	public UDPDataPacket(String address, int port) {

		this.address = address;
		this.port = port;
	}
}
