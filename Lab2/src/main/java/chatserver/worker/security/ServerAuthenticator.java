package chatserver.worker.security;

import channel.AESChannel;
import channel.Channel;
import channel.ObjectChannel;
import chatserver.Chatserver;
import chatserver.worker.TCPWorker;
import security.Base64Util;
import security.CipherUtil;
import util.Keyloader;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lukas on 04.01.2016.
 */
public class ServerAuthenticator {
    private static final Logger LOGGER = Logger.getLogger(ServerAuthenticator.class.getName());
    private static State AUTH_STATE;

    private final TCPWorker tcpWorker;
    private final Chatserver chatserver;
    private final AESChannel channel;
    private final PrintStream userResponseStream;
    private SecretKey secretKey;
    private byte[] iv_parameter;
    private byte[] server_challenge;

    public ServerAuthenticator(TCPWorker tcpWorker, Chatserver chatserver, ObjectChannel channel, PrintStream userResponseStream) {
        this.tcpWorker = tcpWorker;
        this.chatserver = chatserver;
        this.channel = (AESChannel) channel.getByteChannel();
        this.userResponseStream = userResponseStream;

        AUTH_STATE = State.STATE_0;
    }

    public boolean isAuthenticated() {
        return AUTH_STATE == State.STATE_2;
    }

    public void process() {
        try {
            while (!isAuthenticated()) {
                byte[] message = channel.receive();

                switch (AUTH_STATE) {
                    case STATE_0:
                        processStage0(message);
                        channel.activateAESEncryption(secretKey,iv_parameter);
                        break;
                    case STATE_1:
                        processStage1(message);
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void processStage1(byte[] bytes) {

        if (!Arrays.equals(bytes, this.server_challenge)) {
            userResponseStream.println("Error, Server hacked!");
        } else {
            AUTH_STATE = State.STATE_2;
        }

        LOGGER.fine(new String(bytes, StandardCharsets.UTF_8));

    }

    private void processStage0(byte[] bytes) {
        bytes = CipherUtil.decryptRSA(bytes, chatserver.getPrivatekey());

        String message1 = new String(bytes, StandardCharsets.UTF_8);
        String[] parts = message1.split(" ");

        LOGGER.fine(message1);

        String client_challenge_string = parts[2];
        String username = parts[1];

        byte[] client_challenge = client_challenge_string.getBytes(StandardCharsets.UTF_8);

        byte[] randomNumber = CipherUtil.generateRandomNumber_32Byte();
        this.server_challenge = Base64Util.encodeBase64(randomNumber);

        this.secretKey = CipherUtil.generateSecretkey_AES();
        byte[] secretKey_bytes = null;
        if (secretKey != null) secretKey_bytes = secretKey.getEncoded();
        secretKey_bytes = Base64Util.encodeBase64(secretKey_bytes);

        this.iv_parameter = CipherUtil.generateRandomNumber_16Byte();
        iv_parameter = Base64Util.encodeBase64(iv_parameter);

        byte[] message2 = "!ok ".getBytes(StandardCharsets.UTF_8);

        message2 = concat(message2, client_challenge);
        message2 = concat(message2, " ".getBytes(StandardCharsets.UTF_8));
        message2 = concat(message2, server_challenge);
        message2 = concat(message2, " ".getBytes(StandardCharsets.UTF_8));
        message2 = concat(message2, secretKey_bytes);
        message2 = concat(message2, " ".getBytes(StandardCharsets.UTF_8));
        message2 = concat(message2, iv_parameter);

        LOGGER.fine(new String(message2, StandardCharsets.UTF_8));

        try {
            Key clientkey_public = Keyloader.loadClientPublickey(chatserver.getKeys_dir(), username);
            message2 = CipherUtil.encryptRSA(message2, clientkey_public);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

        try {
            channel.send(message2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        AUTH_STATE = State.STATE_1;

    }

    private byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private enum State {
        STATE_0, STATE_1, STATE_2
    }
}
