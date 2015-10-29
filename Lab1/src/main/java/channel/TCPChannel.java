package channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by Lukas on 19.10.2015.
 */
public class TCPChannel implements Channel {
	private static final Logger LOGGER = Logger.getLogger(TCPChannel.class.getName());

	private final Socket socket;

	public TCPChannel(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void send(String data) throws IOException {
		LOGGER.fine("sending: " + data);
		PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);
		serverWriter.println(data);
	}

	@Override
	public String receive() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String response = reader.readLine();
		LOGGER.fine("receiving: " + response);
		return response;
	}

	@Override
	public void close() throws IOException {
		//nothing to do
		LOGGER.info("Shutdown TCP channel");
	}
}
