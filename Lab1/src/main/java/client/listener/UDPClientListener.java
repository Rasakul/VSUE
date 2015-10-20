package client.listener;

import channel.UDPChannel;
import util.Config;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Lukas on 16.10.2015.
 */
public class UDPClientListener implements ClientListener {
    private static final Logger LOGGER = Logger.getLogger(UDPClientListener.class.getName());

    private final DatagramSocket socket_udp;
    private final PrintStream userResponseStream;
    private final String message;
    private final ArrayList<String> args;
    private final Config config;
    private final UDPChannel channel;

    public UDPClientListener(DatagramSocket socket_udp,
                             PrintStream userResponseStream,
                             String message,
                             ArrayList<String> args,
                             Config config) {

        this.socket_udp = socket_udp;
        this.userResponseStream = userResponseStream;
        this.message = message;
        this.args = args;
        this.config = config;
        this.channel = new UDPChannel(socket_udp, config.getString("chatserver.host"), config.getInt("chatserver.udp.port"));
    }

    @Override
    public void run() {
        String message_out = message;
        if (args != null && args.size() > 0) {
            message_out += args.toString();
        }
        try {
            channel.send(message_out);
            userResponseStream.println(channel.receive());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "error communicate with udp socket", e);
            userResponseStream.println("Error, server not reachable!");
        }
    }

    @Override
    public String terminate() {
        LOGGER.info("closing udp socket");
        channel.terminate();
        socket_udp.close();
        return null;
    }
}
