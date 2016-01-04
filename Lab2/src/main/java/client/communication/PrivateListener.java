package client.communication;

import channel.*;
import channel.util.DataPacket;
import client.Client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Lukas on 22.10.2015.
 */
public class PrivateListener implements ClientCommunication {
    private static final Logger LOGGER = Logger.getLogger(PrivateListener.class.getName());

    private final Client client;
    private final ServerSocket serverSocket;
    private final PrintStream userResponseStream;

    private volatile boolean running = true;

    public PrivateListener(Client client, ServerSocket serverSocket, PrintStream userResponseStream) {
        this.client = client;
        this.serverSocket = serverSocket;
        this.userResponseStream = userResponseStream;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Private TCP listening activ");
            while (running) {
                Socket clientSocket = serverSocket.accept();
                ObjectChannel channel = new ObjectChannel(clientSocket);
                DataPacket dataPacket = channel.receive();
                String message = dataPacket.getCommand();
                userResponseStream.println(message);

                if (message.equals("!ack")) {
                    channel.close();
                    clientSocket.close();
                } else {
                    dataPacket.setResponse("!ack");
                    channel.send(dataPacket);
                    channel.close();
                    clientSocket.close();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (running) LOGGER.log(Level.SEVERE, "Error on TCP Socket", e);
        } finally {
            try {
                LOGGER.info("Stopping TCP Listener");
                serverSocket.close();
                running = false;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error closing TCP Socket", e);
            }
        }
        running = false;
    }

    @Override
    public void close() {
        try {
            LOGGER.info("closing TCP socket");
            running = false;
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "error closing tcp serverSocket", e);
        }
    }
}
