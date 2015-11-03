package chatserver.listener;

import channel.Channel;
import channel.UDPChannel;
import channel.util.DataPacket;
import chatserver.Chatserver;
import chatserver.worker.UDPWorker;
import chatserver.worker.Worker;
import util.Config;

import java.io.IOException;
import java.io.PrintStream;
import java.net.BindException;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Lukas on 16.10.2015.
 */
public class UDPListener implements Serverlistener {
	private static final Logger LOGGER = Logger.getLogger(UDPListener.class.getName());

	private final Chatserver      chatserver;
	private final Config          server_config;
	private final PrintStream     userResponseStream;
	private final ExecutorService executor;
	private       DatagramSocket  socket;
	private       Channel         channel;
	private volatile boolean running = true;

	public UDPListener(Chatserver chatserver, Config server_config, PrintStream userResponseStream,
	                   ExecutorService executor) {
		this.chatserver = chatserver;
		this.server_config = server_config;
		this.userResponseStream = userResponseStream;
		this.executor = executor;
	}

	@Override
	public void run() {
		try {
			socket = new DatagramSocket(server_config.getInt("udp.port"));
			this.channel = new UDPChannel(socket, null, null); //crete a channel only for receiving
			LOGGER.info("UDP is UP!");

			while (running) {
				DataPacket dataPacket = channel.receive();
				Worker worker = new UDPWorker(chatserver, dataPacket, userResponseStream, socket);
				executor.execute(worker);
			}

		} catch (BindException e) {
			userResponseStream.println("Error, another server use my ports! Please shut down");
		} catch (IOException | ClassNotFoundException e) {
			if (running) LOGGER.log(Level.SEVERE, "Error on UDP Socket", e);
		}

	}

	public void close() {
		try {
			LOGGER.info("Stopping UDP Listener");
			running = false;
			if (channel != null) channel.close();
			if (socket != null) socket.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error closing UDP Listener", e);
		}
	}
}
