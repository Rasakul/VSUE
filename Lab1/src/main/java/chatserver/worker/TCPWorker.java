package chatserver.worker;

import channel.TCPChannel;
import chatserver.Chatserver;
import chatserver.operations.OperationFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Lukas on 16.10.2015.
 */
public class TCPWorker implements Worker {
    private final int ID;

    private final Chatserver chatserver;
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final PrintStream userResponseStream;
    private final OperationFactory operationFactory;
    private final TCPChannel channel;

    private volatile boolean running = true;
    private String clienthost;
    private Integer clientport;

    public TCPWorker(int ID, Chatserver chatserver, ServerSocket serverSocket, Socket clientSocket, PrintStream userResponseStream) {
        this.ID = ID;
        this.chatserver = chatserver;
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.userResponseStream = userResponseStream;

        this.operationFactory = new OperationFactory(chatserver);

        this.channel = new TCPChannel(clientSocket);
    }

    @Override
    public void run() {
        try {
            clienthost = clientSocket.getInetAddress().toString();
            clientport = clientSocket.getPort();

            System.out.println("new clientsocket: " + clienthost + ":" + clientport);

            while (running) {
                String input = channel.receive();

                if (input == null || input.equals("quit")) this.terminate();

                if (running) {
                    System.out.println("[" + clienthost + ":" + clientport + "]" + input);
                    String response = operationFactory.process(ID, input);
                    System.out.println("sending: " + response);
                    channel.send(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                running = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        running = false;
    }

    @Override
    public void terminate() {
        try {
            System.out.println("closing tcp worker");
            running = false;
            channel.terminate();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public TCPChannel getChannel() {
        return channel;
    }
}
