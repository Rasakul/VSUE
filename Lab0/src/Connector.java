import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Lukas on 05.10.2015.
 */
public class Connector {
    private static Socket socket;
    private static int port = 9000;
    private static String hostname = "dslab.bringsfear.net";
    private static String mknr = "1326526";
    private static String id = "52395";
    private static String message = "!login " + mknr + " " + id;

    public static void main(String[] args) {

        try {
            socket = new Socket(hostname, port);
            socket.setSoTimeout(10000);
            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Try to send messega: " + message);
            out.println(message);
            System.out.println("Message successfully send");
            System.out.println("Try to get answer1");
            System.out.println(in.nextLine());
            System.out.println("Try to get answer2");
            System.out.println(in.nextLine());
            System.out.println("Success!");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
        }
    }

}
