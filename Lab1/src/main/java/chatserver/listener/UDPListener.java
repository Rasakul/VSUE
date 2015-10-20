package chatserver.listener;

import chatserver.Chatserver;
import chatserver.worker.Worker;
import chatserver.worker.UDPWorker;
import util.Config;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;

/**
 * Created by Lukas on 16.10.2015.
 */
public class UDPListener implements Serverlistener {
    private final Chatserver chatserver;
    private final Config server_config;
    private final PrintStream userResponseStream;
    private final ExecutorService executor;

    private volatile boolean running = true;

    DatagramSocket socket;

    public UDPListener(Chatserver chatserver, Config server_config, PrintStream userResponseStream, ExecutorService executor) {
        this.chatserver = chatserver;
        this.server_config = server_config;
        this.userResponseStream = userResponseStream;
        this.executor = executor;
    }

    @Override
    public void run() {
        System.out.println("UDP listen");

        try {
            socket = new DatagramSocket(server_config.getInt("udp.port"));

            while (running) {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                socket.receive(packet);
                Worker worker = new UDPWorker(chatserver, packet, userResponseStream, socket);
                executor.execute(worker);
            }

        } catch (IOException e) {
            userResponseStream.println("UDP socket: " + e.getMessage());
        }

    }

    @Override
    public String terminate() {
        String message = "Try to stop UDP \n";
        running = false;
        socket.close();
        message += "UDP successfully stopped \n";
        return message;
    }
}
