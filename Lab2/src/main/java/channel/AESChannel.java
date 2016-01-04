package channel;

import security.Base64Util;
import security.CipherUtil;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by lukas on 04.01.2016.
 */
@SuppressWarnings("ALL")
public class AESChannel extends Channel {
    private static final Logger LOGGER = Logger.getLogger(AESChannel.class.getName());

    private boolean aes_activated = false;
    private SecretKey secretKey;
    private byte[] iv_parameter;

    public AESChannel(Socket socket) {
        super(socket, new Base64Channel(socket));
    }

    @Override
    public void send(byte[] data) throws IOException {
        LOGGER.info("send " + data);

        if (aes_activated) data = CipherUtil.encryptAES(data, secretKey, iv_parameter);
        LOGGER.info("send " + data);

        getChannel().send(data);
    }

    @Override
    public byte[] receive() throws IOException, ClassNotFoundException {
        byte[] data = getChannel().receive();

        if (aes_activated) data = CipherUtil.decryptAES(data, secretKey, iv_parameter);
        LOGGER.info("receive " + data);
        return data;
    }

    public void activateAESEncryption(SecretKey secretKey, byte[] iv_parameter) {

        this.secretKey = secretKey;
        this.iv_parameter = iv_parameter;

        this.aes_activated = true;
    }

    @Override
    public void close() throws IOException {

    }
}
