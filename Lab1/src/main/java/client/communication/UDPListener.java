package client.communication;

import channel.UDPChannel;
import client.Client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Lukas on 28.10.2015.
 */
public class UDPListener implements ClientCommunication {
	private static final Logger LOGGER = Logger.getLogger(UDPListener.class.getName());

	private final Client      client;
	private final PrintStream userResponseStream;
	private final UDPChannel  channel;
	private volatile boolean running = true;

	public UDPListener(Client client, DatagramSocket socket_udp, PrintStream userResponseStream, String host,
	                   Integer port_udp) {

		this.client = client;
		this.userResponseStream = userResponseStream;

		this.channel = new UDPChannel(socket_udp, host, port_udp);
	}

	@Override
	public void run() {
		try {
			LOGGER.info("UDP listening activ");

			while (running) {
				userResponseStream.println(channel.receive().getResponse());
			}

		} catch (IOException | ClassNotFoundException e) {
			if (running) {
				LOGGER.log(Level.SEVERE, "error communicate with udp socket", e);
				userResponseStream.println("Error with udp!");
				running = false;
			}
		}
	}

	@Override
	public void close() {
		LOGGER.info("closing UDP socket");
		if (running) {
			running = false;
			channel.close();
		}
	}
}
