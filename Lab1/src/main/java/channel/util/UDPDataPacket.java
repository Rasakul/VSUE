package channel.util;

import java.util.List;

/**
 * Created by Lukas on 19.10.2015.
 */
public class UDPDataPacket extends DataPacket {

	private String host;
	private Integer port;

	public UDPDataPacket(String command, List<String> arguments) {
		super(command, arguments);
	}

	public UDPDataPacket(String response) {
		super(response);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
}
