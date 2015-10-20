package channel.util;

/**
 * Created by Lukas on 19.10.2015.
 */
public class TCPDataPacket extends DataPacket {

    public TCPDataPacket(String address, Integer port) {

        this.address = address;
        this.port = port;
    }
}
