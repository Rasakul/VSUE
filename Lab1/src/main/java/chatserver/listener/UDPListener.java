package chatserver.listener;

import chatserver.Chatserver;
import chatserver.worker.UDPWorker;
import chatserver.worker.Worker;
import util.Config;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
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
			LOGGER.info("UDP is UP!");

			while (running) {
				DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
				socket.receive(packet);
				Worker worker = new UDPWorker(chatserver, packet, userResponseStream, socket);
				executor.execute(worker);
			}

		} catch (IOException e) {
			if (running) LOGGER.log(Level.SEVERE, "Error on UDP Socket", e);
		}

	}

	@Override
	public void terminate() {
		LOGGER.info("Stopping UDP Listener");
		running = false;
		socket.close();
	}
}
