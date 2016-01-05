package security;

import org.omg.PortableInterceptor.LOCATION_FORWARD;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Logger;

/**
 * Created by lukas on 04.01.2016.
 */
public class CipherUtil {
    private static final Logger LOGGER = Logger.getLogger(CipherUtil.class.getName());

    private static final String ALGORITHM_RSA = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
    private static final String ALGORITHM_AES = "AES/CTR/NoPadding";
    private static final int KEYSIZE = 256;

    public static byte[] decryptRSA(byte[] text, Key key) {
        LOGGER.info("decryptRSA");
        byte[] cipherText = null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.DECRYPT_MODE, key);
            cipherText = cipher.doFinal(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }


    public static byte[] encryptRSA(byte[] text, Key key) {
        LOGGER.info("encryptRSA");
        byte[] cipherText = null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public static byte[] decryptAES(byte[] text, SecretKey key, byte[] iv_parameter) {
        LOGGER.info("decryptAES");
        byte[] cipherText = null;
        try {
            IvParameterSpec iv = new IvParameterSpec(iv_parameter);

            Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            cipherText = cipher.doFinal(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }


    public static byte[] encryptAES(byte[] text, SecretKey key, byte[] iv_parameter) {
        LOGGER.info("encryptAES");
        byte[] cipherText = null;
        try {
            IvParameterSpec iv = new IvParameterSpec(iv_parameter);

            Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            cipherText = cipher.doFinal(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public static byte[] generateRandomNumber_32Byte() {
        SecureRandom secureRandom = new SecureRandom();
        final byte[] number = new byte[32];
        secureRandom.nextBytes(number);
        return number;
    }

    public static byte[] generateRandomNumber_16Byte() {
        SecureRandom secureRandom = new SecureRandom();
        final byte[] number = new byte[16];
        secureRandom.nextBytes(number);
        return number;
    }

    public static SecretKey generateSecretkey_AES() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(KEYSIZE);
            return generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
