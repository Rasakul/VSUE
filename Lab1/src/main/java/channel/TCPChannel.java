package channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Lukas on 19.10.2015.
 */
public class TCPChannel implements Channel {

    private Socket socket;

    public TCPChannel(Socket socket) {

        this.socket = socket;
    }

    @Override
    public void send(String data) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(data);
    }

    @Override
    public String receive() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return in.readLine();
    }

    @Override
    public String terminate() throws IOException {
        System.out.println("closing tcp channel");
        //socket.close();
        return null;
    }
}
