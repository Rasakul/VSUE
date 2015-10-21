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
    private final Socket socket;
    private PrintWriter serverWriter;
    private BufferedReader reader;

    public TCPChannel(Socket socket) {
        this.socket = socket;
    }


    @Override
    public void send(String data) throws IOException {
        try {
            serverWriter = new PrintWriter(socket.getOutputStream(), true);
            serverWriter.println(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String receive() throws IOException {
        try {
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String data = reader.readLine();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String terminate() throws IOException {
        System.out.println("shutdown tcp channel");
        //serverWriter.close();
        //reader.close();
        return null;
    }
}
