package chatserver.util;

import util.Config;

import java.util.*;

/**
 * Created by Lukas on 22.10.2015.
 */
public class Usermodul {

	private SortedMap<String, String>  userPasswords;
	private SortedMap<Integer, String> loggedinUser;
	private SortedMap<String, String>  registerdUser;

	public Usermodul() {

		Config user_config = new Config("user");

		userPasswords = Collections.synchronizedSortedMap(new TreeMap<String, String>());
		loggedinUser = Collections.synchronizedSortedMap(new TreeMap<Integer, String>());
		registerdUser = Collections.synchronizedSortedMap(new TreeMap<String, String>());

		for (String user : user_config.listKeys()) {
			userPasswords.put(user.replaceAll(".password", ""), user_config.getString(user));
		}
	}

	public boolean checkPassword(String username, String password) {
		return userPasswords.containsKey(username) && password.equals(userPasswords.get(username));
	}

	public boolean checkKnownUser(String username) {
		return userPasswords.containsKey(username);
	}

	public boolean isRegisterd(String username) {
		return registerdUser.containsKey(username);
	}

	public String getAdress(String username) {
		return registerdUser.get(username);
	}

	public String getUser(Integer worker_ID) {
		return loggedinUser.get(worker_ID);
	}

	public boolean isLogedin(Integer worker_ID) {
		return loggedinUser.containsKey(worker_ID);
	}

	public boolean isLogedin(String username) {
		return loggedinUser.containsValue(username);
	}

	public void registerUser(String username, String adress) {
		if (registerdUser.containsKey(username)) registerdUser.remove(username);
		registerdUser.put(username, adress);
	}

	public void loginUser(Integer workerID, String username) {
		loggedinUser.put(workerID, username);
	}

	public void logoutUser(Integer workerID) {
		loggedinUser.remove(workerID);
	}

	public Set<Integer> getLoggedinWorkers() {
		return loggedinUser.keySet();
	}

	public String getOnlineUsers() {
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

	public String getUserString() {
		String result = "";
		for (String user : userPasswords.keySet()) {
			result += user + "    : " + (loggedinUser.containsValue(user) ? "online" : "offline") + "\n";
		}
		return result.substring(0, result.lastIndexOf('\n'));
	}
}
