package edu.iit.cs550.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Pattern;

import edu.iit.cs550.peer.PeerObject;

/**
 * Class for Utility Operations
 * 
 * @author Rajagopal
 *
 */
public class UtilityClass {

	public static Properties prop = new Properties();

	/**
	 * loads the property file
	 */
	static {
		InputStream input = null;
		try {
			input = ClassLoader.getSystemResourceAsStream(Constants.PROPERTYFILE);
			prop.load(input);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Returns the value for property from property file
	 * 
	 * @param key
	 * @return
	 */
	public static String getValue(String key) {
		return prop.getProperty(key);
	}

	/**
	 * Returns the value converted to integer
	 * 
	 * @param key
	 * @return
	 */
	public static int getIntValue(String key) {
		return Integer.parseInt(getValue(key));
	}

	/**
	 * Gets the IPAddress as String
	 * 
	 * @return
	 */
	public static String getMyIP() {
		String myIp = getMyIPAddress().getCanonicalHostName();
		return myIp;
	}

	/**
	 * Gets the IP Address using the wlan interface to identify public address
	 * 
	 * @return
	 */
	public static InetAddress getMyIPAddress() {
		InetAddress myip = null;
		String regex = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		try {
			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
			loop: while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();
				if (Pattern.matches("wlp2s[0-9]", iface.getDisplayName())) {
					InetAddress ia = null;
					for (Enumeration<InetAddress> ips = iface.getInetAddresses(); ips.hasMoreElements();) {
						ia = (InetAddress) ips.nextElement();
						if (Pattern.matches(regex, ia.getCanonicalHostName())) {
							myip = ia;
							break loop;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return myip;
	}

	/**
	 * Forms a peer given the peer id. Lookup of property file
	 * 
	 * @param peerId
	 * @return
	 */
	public static PeerObject getPeer(String peerId) {
		String address = UtilityClass.getValue(peerId);
		String ip = address.split(":")[0];
		int port = Integer.parseInt(address.split(":")[1]);
		return new PeerObject(ip, port, peerId);
	}

	/**
	 * Convenience method to get total number of peers in system
	 * 
	 * @return
	 */
	public static int getNoOfPeers() {
		return getIntValue(Constants.PEERS);
	}

	/**
	 * Writes an output object back to the requestor
	 * 
	 * @param object
	 * @throws Exception
	 */
	public static void writeObject(Object object, Socket socket) throws Exception {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(object);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads the input stream and converts it into transfer object
	 * 
	 * @param socket
	 * @return
	 * @throws Exception
	 */
	public static TransferObject readObject(Socket socket) throws Exception {
		TransferObject to = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			Object obj = ois.readObject();
			if (obj instanceof TransferObject) {
				to = (TransferObject) obj;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return to;
	}

}
