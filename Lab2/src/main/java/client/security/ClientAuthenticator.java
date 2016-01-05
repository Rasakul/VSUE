package client.security;

import channel.AESChannel;
import channel.ObjectChannel;
import security.Base64Util;
import security.CipherUtil;
import util.Keyloader;

import javax.crypto.spec.SecretKeySpec;
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
public class ClientAuthenticator {
	private static final Logger LOGGER = Logger.getLogger(ClientAuthenticator.class.getName());

	private final PrintStream userResponseStream;
	private final String      keys_dir;
	private       Key         serverkey;
	private       AESChannel  channel_tcp;
	private       String      username;
	private       Key         clientkey;
	private       boolean     error;

	private byte[]        client_challenge;
	private byte[]        iv_parameter;
	private SecretKeySpec secretKey;

	public ClientAuthenticator(PrintStream userResponseStream, String keys_dir, String serverkey_path) {
		this.userResponseStream = userResponseStream;
		this.keys_dir = keys_dir;

		try {
			this.serverkey = Keyloader.loadServerPublickey(serverkey_path);
		} catch (IOException e) {
			error = true;
			LOGGER.log(Level.SEVERE, "problem while loading the public server key", e);
			userResponseStream.println("Error, can not find the public server key!");
		}
	}

	/**
	 * performing the handshake algorithm with the server
	 *
	 * @param socket_tcp TCP Socket for the communication
	 * @param username   user to login and load the private key
	 *
	 * @return an ObjectChannel with AES encrypted if the Handshake was successful, otherwise null
	 */
	public ObjectChannel authenticate(Socket socket_tcp, String username) {
		error = false;
		this.username = username;
		this.channel_tcp = new AESChannel(socket_tcp);
		loadKeys();

		if (!error) {
			try {
				byte[] message1 = firstStage();
				channel_tcp.send(message1);
				byte[] message2 = channel_tcp.receive();
				byte[] message3 = secondStage(message2);
				if (!error) {
					channel_tcp.send(message3);

					userResponseStream.println("authentication successful!");

					ObjectChannel channel = new ObjectChannel(socket_tcp);
					channel.setByteChannel(channel_tcp);
					return channel;
				} else {
					return null;
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "error on socket", e);
				error = true;
				return null;
			}
		}
		return null;
	}

	private void loadKeys() {
		try {
			this.clientkey = Keyloader.loadClientPrivatekey(keys_dir, username);
		} catch (IOException e) {
			error = true;
			LOGGER.log(Level.SEVERE, "problem while loading the private key", e);
			userResponseStream.println("Error, can not find the private key!");
		}
	}

	public byte[] firstStage() {
		LOGGER.info("performing first stage of handshake");
		this.client_challenge = CipherUtil.generateRandomNumber_32Byte();
		byte[] client_challenge_encode = Base64Util.encodeBase64(client_challenge);

		String message_string = "!authenticate " + username + " ";
		byte[] message1_p1 = message_string.getBytes(StandardCharsets.UTF_8);
		byte[] message1 = concat(message1_p1, client_challenge_encode);

		LOGGER.fine(new String(message1, StandardCharsets.UTF_8));

		return CipherUtil.encryptRSA(message1, serverkey);
	}

	private byte[] secondStage(byte[] bytes) {
		LOGGER.info("performing second stage of handshake");
		bytes = CipherUtil.decryptRSA(bytes, clientkey);

		String message2 = new String(bytes, StandardCharsets.UTF_8);
		String[] parts = message2.split(" ");

		LOGGER.fine(message2);

		byte[] client_challenge = Base64Util.decodeBase64(parts[1].getBytes(StandardCharsets.UTF_8));
		byte[] server_challenge = Base64Util.decodeBase64(parts[2].getBytes(StandardCharsets.UTF_8));

		byte[] secretKey_bytes = Base64Util.decodeBase64(parts[3].getBytes(StandardCharsets.UTF_8));
		this.iv_parameter = Base64Util.decodeBase64(parts[4].getBytes(StandardCharsets.UTF_8));

		this.secretKey = new SecretKeySpec(secretKey_bytes, "AES");

		if (!Arrays.equals(client_challenge, this.client_challenge)) {
			userResponseStream.println("Error, wrong server identity!");
			LOGGER.log(Level.SEVERE, "Error, wrong server identity!");
			LOGGER.log(Level.SEVERE,
			           "my generated challenge: " + new String(this.client_challenge, StandardCharsets.UTF_8));
			LOGGER.log(Level.SEVERE, "server answer: " + new String(client_challenge, StandardCharsets.UTF_8));
			error = true;
		}

		channel_tcp.activateAESEncryption(secretKey, iv_parameter);

		LOGGER.info("sending " + parts[2]);

		return server_challenge;
	}

	private byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public boolean hasError() {
		return error;
	}
}
