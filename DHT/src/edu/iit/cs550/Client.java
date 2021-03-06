package edu.iit.cs550;

import static edu.iit.cs550.util.UtilityClass.connectToDHTPeer;

import java.net.Socket;
import java.util.Scanner;

import edu.iit.cs550.core.DataObject;
import edu.iit.cs550.core.PeerObject;
import edu.iit.cs550.util.UtilityClass;

/**
 * Interactive client  Supporting GET,PUT and REM
 * @author ram
 *
 */
public class Client {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		DataObject object = null;
		String key = null;
		String value = null;
		PeerObject peerObject = UtilityClass.getPeer("peer");
		Socket socket = null;
		loop: while (true) {
			System.out.println("Please Enter the number for operations you perform to in DHT");
			System.out.println("1:GET 2:PUT 3:REMOVE");
			System.out.println("Enter your choice");
			String choice = scanner.nextLine();
			try {
				switch (Integer.parseInt(choice)) {
				case 1:
					key = getValue("Enter the key", scanner);
					object = new DataObject(key, null, "GET");
					socket = new Socket(peerObject.getIpAddress(), peerObject.getPort());
					object = connectToDHTPeer(object, socket);
					System.out.println("value:" + object.getValue());
					break;
				case 2:
					key = getValue("Enter the key", scanner);
					value = getValue("Enter the value", scanner);
					object = new DataObject(key, value, "PUT");
					socket = new Socket(peerObject.getIpAddress(), peerObject.getPort());
					object = connectToDHTPeer(object, socket);
					System.out.println("key value has been put");
					break;
				case 3:
					key = getValue("Enter the key", scanner);
					object = new DataObject(key, null, "REM");
					socket = new Socket(peerObject.getIpAddress(), peerObject.getPort());
					object = connectToDHTPeer(object, socket);
					System.out.println("value Removed:" + object.getValue());
					break;
				case 4:
					break loop;
				default:
					System.out.println("Please Enter 1 to 3");
					continue;
				}
			} catch (Exception e) {
				continue;
			}

		}

	}

	private static String getValue(String message, Scanner scanner) {
		String value = null;
		while (true) {
			System.out.println(message);
			value = scanner.nextLine();
			if (value == null || value.trim().isEmpty()) {
				continue;
			}
			break;
		}
		return value;
	}

}
