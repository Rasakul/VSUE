package client.security;

import channel.TCPChannel;
import client.Client;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lukas on 03.01.2016.
 */
public class ClientAuthenticator implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientAuthenticator.class.getName());

    private final String ALGORITHM = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";

    private final TCPChannel channel_tcp;
    private final Client client;
    private final Socket socket_tcp;
    private final PrintStream userResponseStream;
    private final Key server_key;
    private final Key client_key;
    private final String username;

    public ClientAuthenticator(Client client, Socket socket_tcp, PrintStream userResponseStream, Key server_key, Key client_key, String username) {
        this.client = client;
        this.socket_tcp = socket_tcp;
        this.userResponseStream = userResponseStream;
        this.server_key = server_key;
        this.client_key = client_key;
        this.username = username;
        this.channel_tcp = new TCPChannel(socket_tcp);
    }

    @Override
    public void run() {

        byte[] randomNumber = generateRandomNumber();
        randomNumber = encodeBase64(randomNumber);
        byte[] client_challenge = encrypt(randomNumber, server_key);

        String message_string = "!authenticate " + username + " ";
        byte[] message1_p1 = message_string.getBytes();
        byte[] message1 = concat(message1_p1,client_challenge);

        try {
            channel_tcp.send(message1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            byte[] message2 = (byte[]) channel_tcp.receive();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private byte[] generateRandomNumber() {
        SecureRandom secureRandom = new SecureRandom();
        final byte[] number = new byte[32];
        secureRandom.nextBytes(number);
        return number;
    }


    private byte[] encodeBase64(byte[] bytes) {
        LOGGER.log(Level.FINE, "encodeBase64");
        return Base64.encode(bytes);
    }

    private byte[] decodeBase64(byte[] bytes) {
        LOGGER.log(Level.FINE, "decodeBase64");
        return Base64.decode(bytes);
    }

    public byte[] encrypt(byte[] text, Key key) {
        byte[] cipherText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
