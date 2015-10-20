package channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Lukas on 19.10.2015.
 */
public class TCPChannel implements Channel {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private volatile boolean open;

    public TCPChannel(Socket socket) {

        this.socket = socket;
    }

    @Override
    public void send(String data) throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println(data);
    }

    @Override
    public String receive() throws IOException {
        try {
            String response = "";
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            response = in.readLine();
            return response;
        } catch (SocketException e){
            System.out.println("error at socket " + e);
            throw e;
        }
    }

    @Override
    public String terminate() throws IOException {
        System.out.println("closing tcp channel");
        open = false;
        //socket.close();
        return null;
    }
}
