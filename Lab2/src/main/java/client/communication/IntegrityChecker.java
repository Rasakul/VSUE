package client.communication;

import security.Base64Util;
import security.CipherUtil;
import util.Keyloader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.logging.Logger;

/**
 * Created by lukas on 05.01.2016.
 */
public class IntegrityChecker {
	private static final Logger LOGGER = Logger.getLogger(IntegrityChecker.class.getName());

	private final Key hmac_key;

	public IntegrityChecker(String hmac_key) throws IOException {
		this.hmac_key = Keyloader.loadSecretKey(hmac_key);
	}

	public boolean check(String message) {
		String[] split = message.split(" ");
		String receivedHash = split[0];
		String text = message.replace(receivedHash + " ", "");

		byte[] computedHash = CipherUtil.createHashMAC(text.getBytes(StandardCharsets.UTF_8), hmac_key);
		byte[] receivedHash_bytes = Base64Util.decodeBase64(receivedHash.getBytes(StandardCharsets.UTF_8));

		return MessageDigest.isEqual(computedHash, receivedHash_bytes);
	}

	public String sign(String message) {

		byte[] computedHash = CipherUtil.createHashMAC(message.getBytes(StandardCharsets.UTF_8), hmac_key);
		computedHash = Base64Util.encodeBase64(computedHash);

		return new String(computedHash, StandardCharsets.UTF_8) + " " + message;
	}
}
