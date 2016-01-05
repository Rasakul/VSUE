package security;

import org.bouncycastle.util.encoders.Base64;

import java.io.*;
import java.util.logging.Logger;

/**
 * Created by lukas on 04.01.2016.
 */
public class Base64Util {
    private static final Logger LOGGER = Logger.getLogger(Base64Util.class.getName());

    public static byte[] encodeBase64(byte[] bytes) {
        return Base64.encode(bytes);
    }

    public static byte[] decodeBase64(byte[] bytes) {
        return Base64.decode(bytes);
    }

    public static byte[] convertToBytes(Object obj) throws IOException {
        LOGGER.fine("convertToBytes");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object convertFromBytes(byte[] data) throws IOException, ClassNotFoundException {
        LOGGER.fine("convertFromBytes");
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
}
