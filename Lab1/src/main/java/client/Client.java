package client;

import client.listener.ClientListener;
import client.listener.TCPClientListener;
import client.listener.UDPClientListener;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements IClientCli, Runnable {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ExecutorService executor;

    private boolean running = true;

    private String componentName;
    private Config config;
    private InputStream userRequestStream;
    private PrintStream userResponseStream;

    private Socket socket_tcp;
    private DatagramSocket socket_udp;

    private TCPClientListener tcpClientListener_all;

    private ArrayList<ClientListener> activListener;

    /**
     * @param componentName      the name of the component - represented in the prompt
     * @param config             the configuration to use
     * @param userRequestStream  the input stream to read user input from
     * @param userResponseStream the output stream to write the console output to
     */
    public Client(String componentName, Config config,
                  InputStream userRequestStream, PrintStream userResponseStream) {
        this.componentName = componentName;
        this.config = config;
        this.userRequestStream = userRequestStream;
        this.userResponseStream = userResponseStream;

        this.executor = Executors.newCachedThreadPool();
        this.activListener = new ArrayList<>();

        try {
            socket_tcp = new Socket(config.getString("chatserver.host"), config.getInt("chatserver.tcp.port"));
            socket_udp = new DatagramSocket();


            //tcpClientListener_all = new TCPClientListener(socket_tcp,
            //        userResponseStream,
            //        "all",
            //        null);
            //executor.execute(tcpClientListener_all);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "error openen sockets", e);
            userResponseStream.println("Error, server not reachable!");
            this.exit();
        }
    }

    @Override
    public void run() {

        LOGGER.info("starting client " + componentName);

        Scanner scanner = new Scanner(userRequestStream);

        while (running) {
            String input = scanner.nextLine();

            try {
                if (input.contains("!login")) {
                    String[] input_data = input.split(" ");
                    if (input_data.length == 3) {
                        this.login(input_data[1], input_data[2]);
                    } else {
                        userResponseStream.println("need username & password!");
                    }
                } else if (input.contains("!logout")) {
                    this.logout();
                } else if (input.contains("!send")) {
                    String[] input_data = input.split(" ");
                    if (input_data.length == 2) {
                        this.send(input_data[1]);
                    } else {
                        userResponseStream.println("need message!");
                    }
                } else if (input.contains("!register")) {
                    String[] input_data = input.split(" ");
                    if (input_data.length == 2) {
                        this.register(input_data[1]);
                    } else {
                        userResponseStream.println("need IP:Port!");
                    }
                } else if (input.contains("!lookup")) {
                    String[] input_data = input.split(" ");
                    if (input_data.length == 2) {
                        this.lookup(input_data[1]);
                    } else {
                        userResponseStream.println("need username!");
                    }
                } else if (input.contains("!msg")) {
                    String[] input_data = input.split(" ");
                    if (input_data.length == 3) {
                        this.msg(input_data[1], input_data[2]);
                    } else {
                        userResponseStream.println("need receiver & message!");
                    }
                } else if (input.contains("!list")) {
                    this.list();
                } else if (input.contains("!lastMsg")) {
                    this.lastMsg();
                } else if (input.contains("!exit")) {
                    this.exit();
                } else {
                    userResponseStream.println("unknown command");
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "error processing user input", e);
            }
        }
    }

    @Override
    public String login(String username, String password) throws IOException {

        TCPClientListener tcpClientListener = new TCPClientListener(socket_tcp,
                userResponseStream,
                "login",
                new ArrayList<>(Arrays.asList(username, password)));
        activListener.add(tcpClientListener);
        executor.execute(tcpClientListener);
        return null;
    }

    @Override
    public String logout() throws IOException {

        TCPClientListener tcpClientListener = new TCPClientListener(socket_tcp,
                userResponseStream,
                "logout",
                null);
        activListener.add(tcpClientListener);
        executor.execute(tcpClientListener);
        return null;
    }

    @Override
    public String send(String message) throws IOException {

        TCPClientListener tcpClientListener = new TCPClientListener(socket_tcp,
                userResponseStream,
                "send",
                new ArrayList<>(Collections.singletonList(message)));
        activListener.add(tcpClientListener);
        executor.execute(tcpClientListener);
        return null;
    }

    @Override
    public String list() throws IOException {

        UDPClientListener udpClientListener = new UDPClientListener(socket_udp,
                userResponseStream,
                "list",
                null,
                config);
        activListener.add(udpClientListener);
        executor.execute(udpClientListener);
        return null;
    }

    @Override
    public String msg(String username, String message) throws IOException {

        TCPClientListener tcpClientListener = new TCPClientListener(socket_tcp,
                userResponseStream,
                "send",
                new ArrayList<>(Arrays.asList(username, message)));
        activListener.add(tcpClientListener);
        executor.execute(tcpClientListener);
        return null;
    }

    @Override
    public String lookup(String username) throws IOException {

        TCPClientListener tcpClientListener = new TCPClientListener(socket_tcp,
                userResponseStream,
                "send",
                new ArrayList<>(Collections.singletonList(username)));
        activListener.add(tcpClientListener);
        executor.execute(tcpClientListener);
        return null;
    }

    @Override
    public String register(String privateAddress) throws IOException {

        TCPClientListener tcpClientListener = new TCPClientListener(socket_tcp,
                userResponseStream,
                "send",
                new ArrayList<>(Collections.singletonList(privateAddress)));
        activListener.add(tcpClientListener);
        executor.execute(tcpClientListener);
        return null;
    }

    @Override
    public String lastMsg() throws IOException {

        TCPClientListener tcpClientListener = new TCPClientListener(socket_tcp,
                userResponseStream,
                "logout",
                null);
        executor.execute(tcpClientListener);
        activListener.add(tcpClientListener);
        return null;
    }

    @Override
    public String exit() {
        LOGGER.info("shutting down client");
        running = false;

        String message = "";
        executor.shutdown();

        try {
            socket_tcp.close();
            socket_udp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (ClientListener listener : activListener) {
            listener.terminate();
        }
        return message;
    }

    /**
     * @param args the first argument is the name of the {@link Client} component
     */
    public static void main(String[] args) {
        Client client = new Client(args[0], new Config("client"), System.in,
                System.out);
        client.run();
    }

    // --- Commands needed for Lab 2. Please note that you do not have to
    // implement them for the first submission. ---

    @Override
    public String authenticate(String username) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
