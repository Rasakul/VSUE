package client.communication;

import channel.ObjectChannel;
import channel.util.DataPacket;
import client.Client;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Lukas on 16.10.2015.
 */
public class PublicListener implements ClientCommunication {
	private static final Logger LOGGER = Logger.getLogger(PublicListener.class.getName());

	private final Client        client;
	private final PrintStream   userResponseStream;
	private final ObjectChannel channel;

	private volatile boolean running = true;

	public PublicListener(Client client, ObjectChannel objectChannel, PrintStream userResponseStream) {
		this.client = client;
		this.userResponseStream = userResponseStream;

		this.channel = objectChannel;
	}

	@Override
	public void run() {
		try {
			LOGGER.info("Server TCP listening activ");
			while (running) {
				DataPacket dataPacket = channel.receive();
				if (dataPacket.getCommand() != null) {

					String response;

					switch (dataPacket.getCommand()) {
						case "lookup":
							if (dataPacket.hasError()) {
								client.setLookupError(true);
							} else {
								client.setLastLookupAdress(dataPacket.getResponse());
							}
							response = dataPacket.getResponse();
							break;
						case "send":
							if (dataPacket.getResponse() != null) client.setLastMsg(dataPacket.getResponse());
							response = dataPacket.getResponse();
							break;
						case "login":
							client.setLoggedIn(!dataPacket.hasError());
							response = dataPacket.getResponse();
							break;
						case "logout":
							client.setLoggedIn(dataPacket.hasError());
							response = dataPacket.getResponse();
							break;
						case "register":
							client.setRegisterError(dataPacket.hasError());
							client.setRegisterSuccess(!dataPacket.hasError());
							response = dataPacket.getResponse();
							break;
						case "serverend":
							response = "Server not reachable, please shut down client";
							running = false;
							client.setServerdown(true);
							break;
						default:
							response = dataPacket.getResponse();
							break;
					}

					if (dataPacket.hasError()) {
						userResponseStream.println(dataPacket.getErrorMsg());
					} else if (response != null) {
						userResponseStream.println(response);
					}
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			if (running) {
				LOGGER.log(Level.SEVERE, "error communicate with tcp socket", e);
				userResponseStream.println("Error, server not reachable!");
				running = false;
				client.setServerdown(true);
			}
		}
	}

	@Override
	public void close() {
		try {
			LOGGER.info("closing TCP socket");
			running = false;
			channel.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "error closing tcp socket", e);
		}
	}
}
