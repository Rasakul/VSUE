package channel;

import util.Config;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Lukas on 19.10.2015.
 */
public class UDPChannel implements Channel {
    private final DatagramSocket socket;
    private final String host;
    private final Integer port;

    public UDPChannel(DatagramSocket socket, String host, Integer port) {
        this.host = host;
        this.port = port;

        this.socket = socket;
    }

    @Override
    public void send(String data) throws IOException {
        byte[] data_byte = data.getBytes();

        DatagramPacket packet_out = new DatagramPacket(data_byte,
                data_byte.length,
                InetAddress.getByName(host),
                port);
        socket.send(packet_out);
    }

    @Override
    public String receive() throws IOException {

        DatagramPacket packet_in = new DatagramPacket(new byte[1024], 1024);
        socket.receive(packet_in);
        return new String(packet_in.getData());
    }

    @Override
    public String terminate() {

        return null;
    }
}
