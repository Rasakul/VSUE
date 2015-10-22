package chatserver.util;

import util.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by Lukas on 22.10.2015.
 */
public class Usermodul {

	private HashMap<String, String>    UserPasswords;
	private Hashtable<String, Boolean> usersStatus;
	private Hashtable<Integer, String> loggedinUser;
	private Hashtable<String, String>  registerdUser;

	public Usermodul() {

		Config user_config = new Config("user");
		usersStatus = new Hashtable<>();
		UserPasswords = new HashMap<>();

		for (String user : user_config.listKeys()) {
			usersStatus.put(user.replaceAll(".password", ""), false);
			UserPasswords.put(user.replaceAll(".password", ""), user_config.getString(user));
		}

		loggedinUser = new Hashtable<>();
		registerdUser = new Hashtable<>();
	}

	public boolean checkPassword(String username, String password) {
		return UserPasswords.containsKey(username) && password.equals(UserPasswords.get(username));
	}

	public boolean checkKnownUser(String username) {
		return UserPasswords.containsKey(username);
	}

	public synchronized boolean isRegisterd(String username) {
		return registerdUser.containsKey(username);
	}

	public String getAdress(String username) {
		return registerdUser.get(username);
	}

	public String getUser(Integer worker_ID) {
		return loggedinUser.get(worker_ID);
	}

	public synchronized boolean isLogedin(Integer worker_ID) {
		return loggedinUser.containsKey(worker_ID);
	}

	public synchronized boolean isLogedin(String username) {
		return loggedinUser.contains(username);
	}

	public synchronized void registerUser(String username, String adress) {
		if (registerdUser.contains(username)) registerdUser.remove(username);
		registerdUser.put(username, adress);
	}

	public synchronized void loginUser(Integer workerID, String username) {
		loggedinUser.put(workerID, username);
	}

	public synchronized void logoutUser(Integer workerID) {
		loggedinUser.remove(workerID);
	}

	public synchronized Set<Integer> getLoggedinWorkers() {
		return loggedinUser.keySet();
	}

	public synchronized String getOnlineUsers() {
		ArrayList<String> users = new ArrayList<>();
		for (int id : loggedinUser.keySet()) {
			users.add(loggedinUser.get(id));
		}

		String online = "Online users: \n";
		for (String user : users) {
			online += "* " + user + "\n";
		}
		return online.substring(0, online.lastIndexOf('\n'));
	}

	public synchronized String getUserString() {
		String result = "";
		for (String user : usersStatus.keySet()) {
			result += user + "    : " + (usersStatus.get(user) ? "online" : "offline") + "\n";
		}
		return result.substring(0, result.lastIndexOf('\n'));
	}
}
