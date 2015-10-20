package chatserver.listener;

import chatserver.Chatserver;
import chatserver.worker.TCPWorker;
import util.Config;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Created by Lukas on 16.10.2015.
 */
public class TCPListener implements Serverlistener {
    private final Chatserver chatserver;
    private final Config server_config;
    private final PrintStream userResponseStream;
    private final ExecutorService executor;

    private volatile boolean running = true;

    private ServerSocket server_socket;

    public TCPListener(Chatserver chatserver, Config server_config, PrintStream userResponseStream, ExecutorService executor) {
        this.chatserver = chatserver;
        this.server_config = server_config;
        this.userResponseStream = userResponseStream;
        this.executor = executor;
    }

    @Override
    public void run() {
        userResponseStream.println("TCP listen");

        try {
            server_socket = new ServerSocket(server_config.getInt("tcp.port"));

            while (running) {
                Socket clientSocket = server_socket.accept();
                int id = Chatserver.OPENCON_COUNTER++;
                TCPWorker worker_tcp = new TCPWorker(id, chatserver, server_socket, clientSocket, userResponseStream);
                chatserver.getOpenConnections().put(id, worker_tcp);
                executor.execute(worker_tcp);
            }
        } catch (IOException e) {
            userResponseStream.println("TCP socket: " + e.getMessage());
        }
    }

    @Override
    public String terminate() {
        String message = "Try to stop TCP \n";
        running = false;
        try {
            server_socket.close();
            message += "TCP successfully stopped \n";
        } catch (IOException e) {
            message += "Error closing TCP: \n" + e.getMessage();
        }
        return message;
    }
}
