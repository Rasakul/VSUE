package client.security;

import channel.AESChannel;
import client.Client;
import security.Base64Util;
import security.CipherUtil;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Created by lukas on 03.01.2016.
 */
public class ClientAuthenticator {
    private static final Logger LOGGER = Logger.getLogger(ClientAuthenticator.class.getName());

    private AESChannel channel_tcp;
    private final Client client;
    private final PrintStream userResponseStream;
    private final Key serverkey;
    private Key client_key;
    private String username;
    private Key clientkey;

    private byte[] client_challenge;
    private byte[] iv_parameter;
    private SecretKeySpec secretKey;

    public ClientAuthenticator(Client client, PrintStream userResponseStream, Key serverkey) {

        this.client = client;
        this.userResponseStream = userResponseStream;
        this.serverkey = serverkey;
    }

    public void process(Socket socket_tcp, String username, Key clientkey) {
        this.username = username;
        this.clientkey = clientkey;
        this.channel_tcp = new AESChannel(socket_tcp);

        try {
            channel_tcp.send(firstStage());
            byte[] message2 = channel_tcp.receive();
            channel_tcp.send(secondStage(message2));

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private byte[] secondStage(byte[] bytes) {
        bytes = CipherUtil.decryptRSA(bytes, client_key);

        String message2 = new String(bytes, StandardCharsets.UTF_8);
        String[] parts = message2.split(" ");

        LOGGER.fine(message2);

        byte[] client_challenge = Base64Util.encodeBase64(parts[1].getBytes(StandardCharsets.UTF_8));
        byte[] server_challenge = Base64Util.encodeBase64(parts[2].getBytes(StandardCharsets.UTF_8));

        byte[] secretKey_bytes = Base64Util.encodeBase64(parts[3].getBytes(StandardCharsets.UTF_8));
        this.iv_parameter = Base64Util.encodeBase64(parts[4].getBytes(StandardCharsets.UTF_8));

        this.secretKey = new SecretKeySpec(secretKey_bytes, 0, secretKey_bytes.length, "AES");

        if (!Arrays.equals(client_challenge, this.client_challenge)) {
            userResponseStream.println("Error, Server hacked!");
        }

        channel_tcp.activateAESEncryption(secretKey,iv_parameter);

        return server_challenge;
    }

    public byte[] firstStage() {
        this.client_challenge = CipherUtil.generateRandomNumber_32Byte();
        client_challenge = Base64Util.encodeBase64(client_challenge);

        String message_string = "!authenticate " + username + " ";
        byte[] message1_p1 = message_string.getBytes(StandardCharsets.UTF_8);
        byte[] message1 = concat(message1_p1, client_challenge);

        LOGGER.fine(new String(message1, StandardCharsets.UTF_8));

        return CipherUtil.encryptRSA(message1, serverkey);
    }

    private byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
