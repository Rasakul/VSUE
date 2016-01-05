package channel;

import security.Base64Util;
import security.CipherUtil;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Implementation of the BytChannel abstract class using AES Encryption
 */
public class AESChannel extends ByteChannel {
	private static final Logger LOGGER = Logger.getLogger(AESChannel.class.getName());

	private boolean aes_activated = false;
	private SecretKey secretKey;
	private byte[]    iv_parameter;

	public AESChannel(Socket socket) {
		super(socket, new Base64Channel(socket));
	}

	@Override
	public void send(byte[] data) throws IOException {
		if (aes_activated) data = CipherUtil.encryptAES(data, secretKey, iv_parameter);

		getByteChannel().send(data);
	}

	@Override
	public byte[] receive() throws IOException {
		byte[] data = getByteChannel().receive();

		if (aes_activated) data = CipherUtil.decryptAES(data, secretKey, iv_parameter);
		return data;
	}

	/**
	 * activates AES Encryption of all further communication
	 *
	 * @param secretKey    secrete key for AES encryption
	 * @param iv_parameter IV parameters for AES encryption
	 */
	public void activateAESEncryption(SecretKey secretKey, byte[] iv_parameter) {
		this.secretKey = secretKey;
		this.iv_parameter = iv_parameter;

		LOGGER.info("activateAESEncryption, secretKey " + secretKey + ", iv_parameter " +
		            new String(Base64Util.encodeBase64(iv_parameter), StandardCharsets.UTF_8));

		this.aes_activated = true;
	}

	@Override
	public void close() throws IOException {
		aes_activated = false;
		secretKey = null;
		iv_parameter = null;
		getByteChannel().close();
	}
}
