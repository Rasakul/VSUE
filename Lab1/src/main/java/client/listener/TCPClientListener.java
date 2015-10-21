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
    private final TCPChannel channel;

    private volatile boolean running = true;

    public TCPClientListener(Socket socket, PrintStream userResponseStream) {
        this.socket = socket;
        this.userResponseStream = userResponseStream;

        this.channel = new TCPChannel(socket);
    }

    @Override
    public void run() {
        System.out.println("listening on TCP");
        try {
            while (running){
                String response = channel.receive();
                if(response != null) userResponseStream.println(response);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "error communicate with tcp socket", e);
            userResponseStream.println("Error, server not reachable!");
            running = false;
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
