package channel;

import security.Base64Util;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Created by lukas on 03.01.2016.
 */
@SuppressWarnings("ALL")
public class Base64Channel extends Channel {
    private static final Logger LOGGER = Logger.getLogger(Base64Channel.class.getName());

    public Base64Channel(Socket socket) {
        super(socket, new SimpleTCPChannel(socket));
    }

    @Override
    public void send(byte[] data) throws IOException {
        LOGGER.info(data.length + " bytes");

        data = Base64Util.encodeBase64(data);
        LOGGER.info(data.length + " bytes");

        getChannel().send(data);
    }

    @Override
    public byte[] receive() throws IOException, ClassNotFoundException {
        byte[] receive = getChannel().receive();
        LOGGER.info(receive.length + " bytes");

        receive = Base64Util.decodeBase64(receive);
        LOGGER.info(receive.length + " bytes");

        return receive;
    }

    @Override
    public void close() throws IOException {

    }
}
