package client;

import channel.Channel;
import channel.TCPChannel;
import channel.UDPChannel;
import cli.Command;
import cli.Shell;
import client.listener.TCPClientListener;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements IClientCli, Runnable {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ExecutorService executor;
    private Channel channel_tcp;
    private Channel channel_udp;
    private Shell shell;
    private TCPClientListener activListener;

    private Socket socket_tcp;
    private DatagramSocket socket_udp;

    private final String componentName;
    private final String host;
    private final Integer port_tcp;
    private final Integer port_udp;
    private String username = "";
    private boolean loggedIn;

    private Config config;
    private InputStream userRequestStream;
    private PrintStream userResponseStream;

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

        this.host = config.getString("chatserver.host");
        this.port_udp = config.getInt("chatserver.udp.port");
        this.port_tcp = config.getInt("chatserver.tcp.port");

        shell = new Shell(componentName, userRequestStream, userResponseStream);
        shell.register(this);
    }

    @Override
    public void run() {
        LOGGER.info("starting client " + componentName);

        try {
            socket_tcp = new Socket(host, port_tcp);
            socket_udp = new DatagramSocket();

            this.activListener = new TCPClientListener(socket_tcp, userResponseStream);
            this.channel_tcp = new TCPChannel(socket_tcp);
            this.channel_udp = new UDPChannel(socket_udp, host, port_udp);

            executor.execute(shell);
            executor.execute(activListener);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "error openen sockets", e);
            userResponseStream.println("Error, server not reachable!");
            this.exit();
        }
        System.out.println(getClass().getName() + " up and waiting for commands!");
    }

    @Override
    @Command
    public String login(String username, String password) throws IOException {
        this.username = username;
        this.loggedIn = true;
        channel_tcp.send("login;" + username + ";" + password);
        return null;
    }

    @Override
    @Command
    public String logout() throws IOException {
        loggedIn = false;
        channel_tcp.send("logout;" + username);
        return null;
    }

    @Override
    @Command
    public String send(String message) throws IOException {
        channel_tcp.send("send;" + message);
        return null;
    }

    @Override
    @Command
    public String list() throws IOException {
        channel_udp.send("list");
        return channel_udp.receive();
    }

    @Override
    @Command
    public String msg(String username, String message) throws IOException {
        channel_tcp.send("msg;" + username + ";" + message);
        return null;
    }

    @Override
    @Command
    public String lookup(String username) throws IOException {
        channel_tcp.send("lookup;" + username);
        return null;
    }

    @Override
    @Command
    public String register(String privateAddress) throws IOException {
        channel_tcp.send("register;" + privateAddress);
        return null;
    }

    @Override
    @Command
    public String lastMsg() throws IOException {
        channel_tcp.send("lastMsg");
        return null;
    }

    @Override
    @Command
    public String exit() {
        LOGGER.info("shutting down client");
        try {
            if (loggedIn) this.logout();
            channel_tcp.send("quit");

            activListener.terminate();
            channel_udp.terminate();
            channel_tcp.terminate();
            socket_tcp.close();
            socket_udp.close();
            shell.close();
            executor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "shutdown complete";
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
