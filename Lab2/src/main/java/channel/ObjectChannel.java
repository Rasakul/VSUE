package channel;

import channel.util.DataPacket;
import security.Base64Util;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by lukas on 04.01.2016.
 */
public class ObjectChannel {
    private static final Logger LOGGER = Logger.getLogger(ObjectChannel.class.getName());

    private Channel channel;

    public ObjectChannel(Socket socket) {
        this.channel = new AESChannel(socket);
    }

    public void send(DataPacket datagramPacket) throws IOException {
        LOGGER.fine("sending: " + datagramPacket);
        channel.send(Base64Util.convertToBytes(datagramPacket));
    }

    public DataPacket receive() throws IOException, ClassNotFoundException {
        DataPacket dataPacket = (DataPacket) Base64Util.convertFromBytes(channel.receive());
        LOGGER.fine("receive: " + dataPacket);
        return dataPacket ;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void close() throws IOException {
        channel.close();
    }

    public Channel getByteChannel() {
        return channel;
    }
}
