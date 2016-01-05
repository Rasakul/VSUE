package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;

/**
 * Created by lukas on 03.01.2016.
 */
public class Keyloader {


	public static Key loadServerPublickey(String server_key) throws IOException {
		String path = Keyloader.class.getProtectionDomain().getCodeSource().getLocation().getPath();

		String server_path = path.replace("build/", server_key);
		File pub = new File(server_path);
		return Keys.readPublicPEM(pub);
	}

	public static Key loadServerPrivatekey(String server_key) throws IOException {
		String path = Keyloader.class.getProtectionDomain().getCodeSource().getLocation().getPath();

		String server_path = path.replace("build/", server_key);
		File pem = new File(server_path);
		return Keys.readPrivatePEM(pem);
	}

	public static Key loadClientPrivatekey(String keys_dir, String username) throws IOException {
		String path = Keyloader.class.getProtectionDomain().getCodeSource().getLocation().getPath();

		String server_path = path.replace("build/", keys_dir + File.separator + username + ".pem");
		File pem = new File(server_path);
		if (!pem.exists()) throw new FileNotFoundException(server_path);
		return Keys.readPrivatePEM(pem);
	}

	public static Key loadClientPublickey(String keys_dir, String username) throws IOException {
		String path = Keyloader.class.getProtectionDomain().getCodeSource().getLocation().getPath();

		String server_path = path.replace("build/", keys_dir + File.separator + username + ".pub.pem");
		File pem = new File(server_path);
		return Keys.readPublicPEM(pem);
	}
}
