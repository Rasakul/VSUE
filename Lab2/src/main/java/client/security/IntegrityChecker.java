package client.security;

import security.Base64Util;
import security.CipherUtil;
import util.Keyloader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.logging.Logger;

/**
 * Class for checking the integrity of a message and generate a hash of a message
 */
public class IntegrityChecker {
	private static final Logger LOGGER = Logger.getLogger(IntegrityChecker.class.getName());

	private final Key hmac_key;

	public IntegrityChecker(String hmac_key) throws IOException {
		this.hmac_key = Keyloader.loadSecretKey(hmac_key);
	}

	/**
	 * check if the given message was manipulated
	 *
	 * @param message to check
	 *
	 * @return true, if the message was not manipulated, otherwise false
	 */
	public boolean check(String message) {
		LOGGER.info("check message integrity");
		String[] split = message.split(" ");
		String receivedHash = split[0];
		String text = message.replace(receivedHash + " ", "");

		byte[] computedHash = CipherUtil.createHashMAC(text.getBytes(StandardCharsets.UTF_8), hmac_key);
		byte[] receivedHash_bytes = Base64Util.decodeBase64(receivedHash.getBytes(StandardCharsets.UTF_8));

		LOGGER.fine("computed: " + new String(Base64Util.encodeBase64(computedHash), StandardCharsets.UTF_8));
		LOGGER.fine("received: " + new String(Base64Util.encodeBase64(receivedHash_bytes), StandardCharsets.UTF_8));

		boolean integrity = MessageDigest.isEqual(computedHash, receivedHash_bytes);
		LOGGER.info("integrity is: " + integrity);
		return integrity;
	}

	/**
	 * generates a hash and modifies the message to the format <hash> <message>
	 *
	 * @param message the sign
	 *
	 * @return the signed message
	 */
	public String sign(String message) {
		LOGGER.info("sign message");

		byte[] computedHash = CipherUtil.createHashMAC(message.getBytes(StandardCharsets.UTF_8), hmac_key);
		computedHash = Base64Util.encodeBase64(computedHash);
		LOGGER.fine("computed: " + new String(computedHash, StandardCharsets.UTF_8));

		return new String(computedHash, StandardCharsets.UTF_8) + " " + message;
	}
}
