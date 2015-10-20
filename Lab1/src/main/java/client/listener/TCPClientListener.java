package client.listener;

import channel.TCPChannel;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Lukas on 16.10.2015.
 */
public class TCPClientListener implements ClientListener {
    private static final Logger LOGGER = Logger.getLogger(TCPClientListener.class.getName());

    private final Socket socket;
    private final PrintStream userResponseStream;
    private final String message;
    private final ArrayList<String> args;
    private final TCPChannel channel;

    private volatile boolean running;

    public TCPClientListener(Socket socket, PrintStream userResponseStream, String message, ArrayList<String> args) {
        this.socket = socket;
        this.userResponseStream = userResponseStream;
        this.message = message;
        this.args = args;

        this.channel = new TCPChannel(socket);
    }

    @Override
    public void run() {
        String message_out = message;

        if (args != null && args.size() > 0) {
            for (String arg : args) {
                message_out += " " + arg;
            }
        }

        try {
            channel.send(message_out);
            userResponseStream.println(channel.receive());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "error communicate with tcp socket", e);
            userResponseStream.println("Error, server not reachable!");
        }
    }

    @Override
    public String terminate() {
        LOGGER.info("closing TCP socket");
        try {
            running = false;
            channel.terminate();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "error closing tcp socket", e);
        }
        return null;
    }
}
