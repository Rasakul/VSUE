package chatserver.worker;

import chatserver.Chatserver;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Hashtable;

/**
 * Created by Lukas on 16.10.2015.
 */
public class UDPWorker implements Worker {

    private final Chatserver chatserver;
    private final DatagramPacket packet_in;
    private final PrintStream userResponseStream;
    private final DatagramSocket socket;

    private volatile boolean running = true;

    public UDPWorker(Chatserver chatserver, DatagramPacket packet_in, PrintStream userResponseStream, DatagramSocket socket) {
        this.chatserver = chatserver;
        this.packet_in = packet_in;
        this.userResponseStream = userResponseStream;
        this.socket = socket;


    }

    @Override
    public void run() {
        byte[] data = packet_in.getData();
        String command = new String(data, 0, packet_in.getLength());

        userResponseStream.println("Processing incoming UDP: " + command + " from " + packet_in.getAddress() + ":" + packet_in.getPort());
        try {

            byte[] response = (command.equals("list") ? this.getOnlineUsers() : "unknown command").getBytes();
            System.out.println("sending " + new String(response));
            DatagramPacket packet_out = new DatagramPacket(response,
                    response.length,
                    packet_in.getAddress(),
                    packet_in.getPort());

            socket.send(packet_out);

        } catch (IOException e) {
            e.printStackTrace();
        }
        running = false;
    }

    public String getOnlineUsers() {
        Hashtable<String, Boolean> users;
        synchronized (chatserver.getUsersStatus()) {
            users = new Hashtable<>(chatserver.getUsersStatus());
        }

        String online = "Online users: \n";

        for (String user : users.keySet()) {
            online += users.get(user) ? ("* " + user + "\n") : "";
        }
        return online.substring(0, online.lastIndexOf('\n'));
    }

    @Override
    public void terminate() {
        running = false;
        socket.close();
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
