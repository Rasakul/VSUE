package chatserver.util;

import util.Config;

import java.util.*;

/**
 * Class for managing users, passwords, login status and user addresses
 */
public class Usermodul {

	private SortedMap<String, String>  userPasswords;
	private SortedMap<Integer, String> loggedinUser;
	//private SortedMap<String, String>  registerdUser;

	public Usermodul() {

		Config user_config = new Config("user");

		userPasswords = Collections.synchronizedSortedMap(new TreeMap<String, String>());
		loggedinUser = Collections.synchronizedSortedMap(new TreeMap<Integer, String>());
		//registerdUser = Collections.synchronizedSortedMap(new TreeMap<String, String>());

		for (String user : user_config.listKeys()) {
			userPasswords.put(user.replaceAll(".password", ""), user_config.getString(user));
		}
	}

	/**
	 * check, if the user is known and the password matches the username
	 *
	 * @param username must be known
	 * @param password to check
	 *
	 * @return true, if password matches known user, otherwise false
	 */
	public boolean checkPassword(String username, String password) {
		return userPasswords.containsKey(username) && password.equals(userPasswords.get(username));
	}

	/**
	 * check, if the user is known
	 *
	 * @param username to check
	 *
	 * @return true, if user is known, otherwise false
	 */
	public boolean checkKnownUser(String username) {
		return userPasswords.containsKey(username);
	}

	/**
	 * check, if the user has registered an address
	 *
	 * @param username to check
	 *
	 * @return true, if user has registered an address, otherwise false
	 */
	/*public boolean isRegisterd(String username) {
		return registerdUser.containsKey(username);
	}*/


	/**
	 * returns the registered address for the user
	 *
	 * @param username to check
	 *
	 * @return the registered address
	 */
	/*public String getAdress(String username) {
		return registerdUser.containsKey(username) ? registerdUser.get(username) : null;
	}*/

	/**
	 * get the logged in of the worker by his ID
	 *
	 * @param worker_ID to check
	 *
	 * @return the name of the logged in user
	 */
	public String getUser(Integer worker_ID) {
		return loggedinUser.get(worker_ID);
	}

	/**
	 * check, if the worker has a logged in user
	 *
	 * @param worker_ID to check
	 *
	 * @return true, if the worker has a logged in user, otherwise false
	 */
	public boolean isLoggedIn(Integer worker_ID) {
		return loggedinUser.containsKey(worker_ID);
	}

	/**
	 * check, if the user has an open worker
	 *
	 * @param username to check
	 *
	 * @return true, if the user has an open worker, otherwise false
	 */
	public boolean isLoggedIn(String username) {
		return loggedinUser.containsValue(username);
	}

	/**
	 * register the user with his address
	 *
	 * @param adress   of the user, format: IP:Port
	 * @param username who wants to register
	 * @param address
	 */
	/*public void registerUser(String username, String address) {
		if (registerdUser.containsKey(username)) registerdUser.remove(username);
		registerdUser.put(username, address);
	}*/

	/**
	 * log in the user and register the corresponding worker ID
	 *
	 * @param workerID worker who log in the user and manage the communication
	 * @param username to log in
	 */
	public void loginUser(Integer workerID, String username) {
		loggedinUser.put(workerID, username);
	}

	/**
	 * log out the user and remove the corresponding worker ID
	 *
	 * @param workerID to remove
	 */
	public void logoutUser(Integer workerID) {
		loggedinUser.remove(workerID);
	}

	/**
	 * get all worker with logged in users
	 *
	 * @return a set of all IDs of workers with logged in users
	 */
	public Set<Integer> getLoggedInWorkers() {
		return loggedinUser.keySet();
	}

	/**
	 * @return a string representation of all online users
	 */
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

	/**
	 * @return a string representation of users with their login status
	 */
	public String getUserString() {
		String result = "";
		for (String user : userPasswords.keySet()) {
			result += user + "    : " + (loggedinUser.containsValue(user) ? "online" : "offline") + "\n";
		}
		return result.substring(0, result.lastIndexOf('\n'));
	}
}
