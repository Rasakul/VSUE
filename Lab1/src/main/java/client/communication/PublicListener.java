package client.communication;

import channel.TCPChannel;
import client.Client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Lukas on 16.10.2015.
 */
public class PublicListener implements ClientCommunication {
	private static final Logger LOGGER = Logger.getLogger(PublicListener.class.getName());

	private final Client      client;
	private final PrintStream userResponseStream;
	private final TCPChannel  channel;

	private volatile boolean running = true;

	public PublicListener(Client client, Socket socket, PrintStream userResponseStream) {
		this.client = client;
		this.userResponseStream = userResponseStream;

		this.channel = new TCPChannel(socket);
	}

	@Override
	public void run() {
		try {
			LOGGER.info("Server TCP listening activ");
			while (running) {
				String response = channel.receive();
				if (response != null) {
					if (response.contains("lookup:")) {
						response = response.replaceFirst("lookup:", "");
						client.setLastLookupAdress(response);
					}
					userResponseStream.println(response);
				}
			}
		} catch (IOException e) {
			if (running) {
				LOGGER.log(Level.SEVERE, "error communicate with tcp socket", e);
				userResponseStream.println("Error, server not reachable!");
				running = false;
			}
		}
	}

	@Override
	public String terminate() {
		try {
			LOGGER.info("closing TCP socket");
			running = false;
			channel.terminate();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "error closing tcp socket", e);
		}
		return null;
	}
}
