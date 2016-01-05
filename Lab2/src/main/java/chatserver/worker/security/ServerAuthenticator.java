package chatserver.worker.security;

import channel.AESChannel;
import channel.ObjectChannel;
import chatserver.Chatserver;
import chatserver.util.Usermodul;
import chatserver.worker.TCPWorker;
import security.Base64Util;
import security.CipherUtil;
import util.Keyloader;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for the 3 steps Handshake Algorithm with the Server using RSA, Base64 and resulting in an AES encrypted
 * Channel
 */
public class ServerAuthenticator {
	private static final Logger LOGGER = Logger.getLogger(ServerAuthenticator.class.getName());
	private static State AUTH_STATE;

	private final Chatserver    chatserver;
	private final AESChannel    channel_byte;
	private final PrintStream   userResponseStream;
	private final Usermodul     usermodul;
	private final ObjectChannel channel_object;
	private final TCPWorker     tcpWorker;
	private       SecretKey     secretKey;
	private       byte[]        iv_parameter;
	private       byte[]        server_challenge;
	private       String        username;
	private boolean error = false;

	public ServerAuthenticator(TCPWorker tcpWorker, Chatserver chatserver, Socket clientSocket,
	                           PrintStream userResponseStream) {
		this.tcpWorker = tcpWorker;
		this.chatserver = chatserver;
		this.channel_object = new ObjectChannel(clientSocket);
		this.channel_byte = (AESChannel) channel_object.getByteChannel();
		this.userResponseStream = userResponseStream;
		this.usermodul = chatserver.getUsermodul();

		AUTH_STATE = State.UNKNOWN;
	}

	public boolean isAuthenticated() {
		return AUTH_STATE == State.AUTHENTICATED;
	}

	/**
	 * performing the handshake algorithm with the client
	 *
	 * @return an ObjectChannel with AES encrypted if the Handshake was successful, otherwise null
	 *
	 * @throws IOException if an error occurs during the communication with the Socket
	 */
	public ObjectChannel authenticate() throws IOException {
		while (!isAuthenticated() && !error) {
			byte[] message = channel_byte.receive();

			switch (AUTH_STATE) {
				case UNKNOWN:
					firstStage(message);
					channel_byte.activateAESEncryption(secretKey, iv_parameter);
					break;
				case HANDSHAKE:
					secondStage(message);
					break;
			}
		}
		if (!error) {
			login();
		}
		return channel_object;
	}

	private void firstStage(byte[] bytes) {
		LOGGER.info("performing first stage of handshake");
		bytes = CipherUtil.decryptRSA(bytes, chatserver.getPrivatekey());

		String message1 = new String(bytes, StandardCharsets.UTF_8);
		LOGGER.fine(message1);

		String[] parts = message1.split(" ");
		this.username = parts[1];

		if (usermodul.checkKnownUser(username)) {

			String client_challenge_string = parts[2];
			byte[] client_challenge = client_challenge_string.getBytes(StandardCharsets.UTF_8);

			this.server_challenge = CipherUtil.generateRandomNumber_32Byte();
			byte[] server_challenge_encoded = Base64Util.encodeBase64(server_challenge);

			this.secretKey = CipherUtil.generateSecretkey_AES();
			byte[] secretKey_bytes = null;
			if (secretKey != null) {
				secretKey_bytes = secretKey.getEncoded();
			} else {
				error = true;
			}
			secretKey_bytes = Base64Util.encodeBase64(secretKey_bytes);

			this.iv_parameter = CipherUtil.generateRandomNumber_16Byte();
			byte[] iv_parameter_encoded = Base64Util.encodeBase64(iv_parameter);

			byte[] message2 = "!ok ".getBytes(StandardCharsets.UTF_8);

			message2 = concat(message2, client_challenge);
			message2 = concat(message2, " ".getBytes(StandardCharsets.UTF_8));
			message2 = concat(message2, server_challenge_encoded);
			message2 = concat(message2, " ".getBytes(StandardCharsets.UTF_8));
			message2 = concat(message2, secretKey_bytes);
			message2 = concat(message2, " ".getBytes(StandardCharsets.UTF_8));
			message2 = concat(message2, iv_parameter_encoded);

			LOGGER.fine(new String(message2, StandardCharsets.UTF_8));

			try {
				Key clientkey_public = Keyloader.loadClientPublickey(chatserver.getKeys_dir(), username);
				message2 = CipherUtil.encryptRSA(message2, clientkey_public);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "error loading key", e);
				exit();
				return;
			}

			try {
				channel_byte.send(message2);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "error on socket", e);
				exit();
				return;
			}

			AUTH_STATE = State.HANDSHAKE;
		} else {
			exit();
		}
	}

	private void secondStage(byte[] bytes) {
		LOGGER.info("performing second stage of handshake");

		if (!Arrays.equals(Base64Util.decodeBase64(bytes), this.server_challenge)) {
			userResponseStream.println("Error, wrong client identity!");
			LOGGER.log(Level.SEVERE, "Error, wrong client identity!");
			LOGGER.log(Level.SEVERE,
			           "generated challenge: " + new String(this.server_challenge, StandardCharsets.UTF_8));
			LOGGER.log(Level.SEVERE, "client answer: " + new String(bytes, StandardCharsets.UTF_8));
			exit();
		} else {
			AUTH_STATE = State.AUTHENTICATED;
			LOGGER.fine(new String(bytes, StandardCharsets.UTF_8));
			LOGGER.info("handshake successful");
		}
	}

	private void login() {
		LOGGER.info("login in the client");
		usermodul.loginUser(tcpWorker.getID(), username);
	}

	private byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	private void exit() {
		error = true;
		tcpWorker.close();
	}

	public boolean hasError() {
		return error;
	}

	private enum State {
		UNKNOWN, HANDSHAKE, AUTHENTICATED
	}
}
