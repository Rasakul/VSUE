package client.communication;

import channel.Base64Channel;
import channel.ByteChannel;
import client.Client;
import client.security.IntegrityChecker;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Lukas on 22.10.2015.
 */
public class PrivateListener implements ClientCommunication {
	private static final Logger LOGGER = Logger.getLogger(PrivateListener.class.getName());

	private final Client           client;
	private final ServerSocket     serverSocket;
	private final PrintStream      userResponseStream;
	private final IntegrityChecker integrityChecker;

	private volatile boolean running = true;

	public PrivateListener(Client client, ServerSocket serverSocket, PrintStream userResponseStream,
	                       IntegrityChecker integrityChecker) {
		this.client = client;
		this.serverSocket = serverSocket;
		this.userResponseStream = userResponseStream;
		this.integrityChecker = integrityChecker;
	}

	@Override
	public void run() {
		try {
			LOGGER.info("Private TCP listening active");
			while (running) {
				Socket clientSocket = serverSocket.accept();
				ByteChannel channel = new Base64Channel(clientSocket);
				byte[] bytes = channel.receive();
				String message = new String(bytes, StandardCharsets.UTF_8);

				String[] split = message.split(" ");
				String command = split[1];
				String text = message.replace(split[0] + " ", "");
				text = text.replace(command + " ", "");
				String respond = null;

				boolean send = false;

				switch (command) {
					case "!ack":
						if (!integrityChecker.check(message)) {
							userResponseStream.println("the client answer was compromised");
							respond = "!tampered " + text;
							send = true;
						}
						break;
					case "!msg":
						userResponseStream.println(text);

						if (!integrityChecker.check(message)) {
							userResponseStream.println("the client message was compromised");
							respond = "!tampered " + text;
							send = true;
						} else {
							respond = "!ack";
							send = true;
						}

						break;
					case "!tampered":
						userResponseStream.println("your message was compromised");
						send = false;
						break;
				}
				if (send) {
					respond = integrityChecker.sign(respond);
					channel.send(respond.getBytes(StandardCharsets.UTF_8));
				}
				channel.close();
				clientSocket.close();

			}
		} catch (IOException e) {
			if (running) LOGGER.log(Level.SEVERE, "Error on TCP Socket", e);
		} finally {
			try {
				LOGGER.info("Stopping TCP Listener");
				serverSocket.close();
				running = false;
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Error closing TCP Socket", e);
			}
		}
		running = false;
	}

	@Override
	public void close() {
		try {
			LOGGER.info("closing TCP socket");
			running = false;
			serverSocket.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "error closing tcp serverSocket", e);
		}
	}
}
