package channel;

import channel.util.DataPacket;

import javax.crypto.SecretKey;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/**
 * Interface for abstraction of the communication with a socket
 */
public abstract class Channel implements Closeable {
    private final Socket socket;
    private Channel channel;

    public Channel(Socket socket, Channel channel){

        this.socket = socket;
        this.channel = channel;
    }

    /**
     * Sending a {@link DataPacket} over the channel
     *
     * @param data packet for sending
     * @throws IOException if an error occurs during the communication with the Socket
     */
    public abstract void send(byte[] bytes) throws IOException;

    /**
     * Receiving a {@link DataPacket} over the channel
     * The method is a blocking I/O operation
     *
     * @return the received datapacket
     * @throws IOException            if an error occurs during the communication with the Socket
     * @throws ClassNotFoundException if the wrong type of {@link DataPacket} was received
     */
    public abstract byte[] receive() throws IOException, ClassNotFoundException;

    public Socket getSocket() {
        return socket;
    }

    public Channel getChannel() {
        return channel;
    }
}
