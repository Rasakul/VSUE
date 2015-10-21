package chatserver;

import chatserver.listener.Serverlistener;
import chatserver.listener.TCPListener;
import chatserver.listener.UDPListener;
import chatserver.worker.Worker;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chatserver implements IChatserverCli, Runnable {

    public static int OPENCON_COUNTER = 0;

    private String componentName;
    private Config server_config;
    private InputStream userRequestStream;
    private PrintStream userResponseStream;
    private Serverlistener TCPListener;
    private Serverlistener UDPListener;

    private ExecutorService executor;

    private boolean running = true;

    private Hashtable<String, Boolean> usersStatus;
    private HashMap<String, String> passwords;

    private Hashtable<Integer, Worker> openConnections;
    private Hashtable<Integer, String> userConnections_tcp;

    /**
     * @param componentName      the name of the component - represented in the prompt
     * @param server_config      the configuration to use
     * @param userRequestStream  the input stream to read user input from
     * @param userResponseStream the output stream to write the console output to
     */
    public Chatserver(String componentName, Config server_config,
                      InputStream userRequestStream, PrintStream userResponseStream) {
        this.componentName = componentName;
        this.server_config = server_config;
        this.userRequestStream = userRequestStream;
        this.userResponseStream = userResponseStream;

        Config user_config = new Config("user");
        usersStatus = new Hashtable<>();
        passwords = new HashMap<>();

        for (String user : user_config.listKeys()) {
            usersStatus.put(user.replaceAll(".password", ""), false);
            passwords.put(user.replaceAll(".password", ""), user_config.getString(user));
        }

        openConnections = new Hashtable<>();
        userConnections_tcp = new Hashtable<>();
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        TCPListener = new TCPListener(this, server_config, userResponseStream, executor);
        UDPListener = new UDPListener(this, server_config, userResponseStream, executor);

        executor.execute(TCPListener);
        executor.execute(UDPListener);

        Scanner scanner = new Scanner(userRequestStream);

        while (running) {
            try {
                String input = scanner.next();

                switch (input) {
                    case "!usersStatus":
                        userResponseStream.println(this.users());
                        break;
                    case "!exit":
                        userResponseStream.println(this.exit());
                        break;
                    default:
                        userResponseStream.println("unknown command");
                }
            } catch (IOException e) {
                userResponseStream.println("error processing user input: ");
                userResponseStream.println(e.getMessage());
            }
        }
    }

    @Override
    public String users() throws IOException {
        String result = "";
        for (String user : usersStatus.keySet()) {
            result += user + ": " + (usersStatus.get(user) ? "online" : "offline") + "\n";
        }
        return result.substring(0, result.lastIndexOf('\n'));
    }

    @Override
    public String exit() throws IOException {
        running = false;
        String message = "Shutting down " + componentName + "\n";

        for (Worker worker : this.getOpenConnections().values()) {
            if (worker.isRunning()) {
                worker.terminate();
            }
        }

        message += TCPListener.terminate();
        message += UDPListener.terminate();
        executor.shutdown();
        message += "Shutting down finished";
        return message;
    }

    /**
     * @param args the first argument is the name of the {@link Chatserver}
     *             component
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Chatserver chatserver = new Chatserver(args[0],
                new Config("chatserver"), System.in, System.out);

        chatserver.run();
    }

    public Hashtable<String, Boolean> getUsersStatus() {
        return usersStatus;
    }

    public HashMap<String, String> getPasswords() {
        return passwords;
    }

    public synchronized Hashtable<Integer, Worker> getOpenConnections() {
        return openConnections;
    }

    public synchronized Hashtable<Integer, String> getUserConnections_tcp() {
        return userConnections_tcp;
    }
}
