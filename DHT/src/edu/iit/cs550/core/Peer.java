package edu.iit.cs550.core;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.iit.cs550.util.UtilityClass;

/**
 * Main Class for the Peer
 * 
 * @author Raja
 *
 */
public class Peer implements Runnable {

	public Peer(int port) {
		this.peerObect = new PeerObject(UtilityClass.getMyIP(), port, null);
	}

	PeerObject peerObect = null;

	Map<Object, Object> internalMap = new LinkedHashMap<Object, Object>();

	/**
	 * Computes the distributed hash function for the key
	 * 
	 * @param key
	 * @return
	 */
	private int computeHash(Object key) {
		int hashCode = key.hashCode();
		return (hashCode % UtilityClass.getNoOfPeers()) + 1;
	}

	/**
	 * Main method for putting key and value
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean put(Object key, Object value) {
		boolean success = false;
		int peerNo = computeHash(key);
		String peer = "peer" + peerNo;
		try {
			PeerObject obj = UtilityClass.getPeer(peer);
			if (peerObect.equals(obj)) {
				internalMap.put(key, value);
				success = true;
			} else {
				DataObject object = new DataObject(key, value, "PUT");
				object = connectToPeer(obj, object);
				if (object != null && object.isSuccess()) {
					success = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return success;

	}

	/**
	 * Main method for getting value from key
	 * 
	 * @param key
	 * @return
	 */
	public Object get(Object key) {
		Object value = null;
		int peerNo = computeHash(key);
		String peer = "peer" + peerNo;
		try {
			PeerObject obj = UtilityClass.getPeer(peer);
			if (peerObect.equals(obj)) {
				value = internalMap.get(key);
			} else {
				DataObject object = new DataObject(key, null, "GET");
				object = connectToPeer(obj, object);
				if (object != null && object.isSuccess()) {
					value = object.getValue();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;

	}

	/**
	 * Main method for removing key
	 * 
	 * @param key
	 * @return
	 */
	public Object remove(Object key) {
		Object value = null;
		int peerNo = computeHash(key);
		String peer = "peer" + peerNo;
		try {
			PeerObject obj = UtilityClass.getPeer(peer);
			if (peerObect.equals(obj)) {
				value = internalMap.remove(key);
			} else {
				DataObject object = new DataObject(key, null, "REM");
				object = connectToPeer(obj, object);
				if (object != null && object.isSuccess()) {
					value = object.getValue();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;

	}

	/**
	 * Connects to peer and obtains result for the operation
	 * @param peerObject
	 * @param object
	 * @return
	 */
	public DataObject connectToPeer(PeerObject peerObject, DataObject object) {
		DataObject result = UtilityClass.connectToDHTPeer(object, peerObject);
		return result;

	}
	/**
	 * Method to start the server and wait for result
	 */
	@Override
	public void run() {
		try (ServerSocket socket = new ServerSocket(peerObect.getPort(), 50, UtilityClass.getMyIPAddress());) {
			int threads = Integer.parseInt(UtilityClass.getValue("THREADS"));
			ExecutorService executorService = Executors.newFixedThreadPool(threads);
			while (true) {
				Socket peerSocket = socket.accept();
				PeerThread pThread = new PeerThread();
				pThread.setPeer(this);
				pThread.setPeerSocket(peerSocket);
				executorService.submit(pThread);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Kicks off server Thread
	 */
	public void execute() {
		System.out.println("Peer Running");
		new Thread(this).start();
	}

}
/**
 * Class to Handle Multithreaded server implementation
 * @author ram
 *
 */
class PeerThread implements Runnable {

	public Socket peerSocket;

	Peer peer = null;

	public Socket getPeerSocket() {
		return peerSocket;
	}

	public void setPeerSocket(Socket peerSocket) {
		this.peerSocket = peerSocket;
	}

	public Peer getPeer() {
		return peer;
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	/**
	 * Main method to handle the incoming requests and perform the requested operation
	 */
	@Override
	public void run() {
		try {
			ObjectInputStream ois = new ObjectInputStream(peerSocket.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(peerSocket.getOutputStream());
			DataObject input = (DataObject) ois.readObject();
			switch (input.getOperation()) {
			case "GET":
				input.setValue(peer.get(input.getKey()));
				input.setSuccess("Y");
				break;
			case "PUT":
				peer.put(input.getKey(), input.getValue());
				input.setSuccess("Y");
				break;
			case "REM":
				input.setValue(peer.remove(input.getKey()));
				input.setSuccess("Y");
				break;
			}
			oos.writeObject(input);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
