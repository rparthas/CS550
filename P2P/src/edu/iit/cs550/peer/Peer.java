package edu.iit.cs550.peer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.iit.cs550.common.Constants;
import edu.iit.cs550.common.UtilityClass;

/**
 * Main Class for the Peer
 * 
 * @author Rajagopal
 *
 */
public class Peer implements Runnable {

	public Peer(int port) {
		this.peerObject = new PeerObject(UtilityClass.getMyIP(), port, null);
		socketPool = new SocketPool(peerObject);
	}

	PeerObject peerObject = null;

	SocketPool socketPool = null;

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
	 * Main Method to perform all operations
	 * 
	 * @param input
	 */
	public void performOperation(DataObject input) {
		int peerNo = computeHash(input.getKey());
		String peer = Constants.PEER + peerNo;
		Object value = null;
		try {
			PeerObject obj = UtilityClass.getPeer(peer);
			if (peerObject.equals(obj)) {
				switch (input.getOperation()) {
				case "GET":
					value = internalMap.get(input.getKey());
					input.setValue(value);
					input.setSuccess("Y");
					break;
				case "PUT":
					internalMap.put(input.getKey(), input.getValue());
					input.setSuccess("Y");
					break;
				case "REM":
					value = internalMap.remove(input.getKey());
					input.setValue(value);
					input.setSuccess("Y");
					break;
				}
			} else {
				connectToPeer(input, peerObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method to start the server and wait for result.Keeps running
	 */
	@Override
	public void run() {
		try (ServerSocket socket = new ServerSocket(peerObject.getPort(), 50, UtilityClass.getMyIPAddress());) {
			new Thread(socketPool).start();
			int threads = Integer.parseInt(UtilityClass.getValue(Constants.PEERTHREADS));
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

	/**
	 * Connects to specified peer in DHT and sends the message to the peer
	 * 
	 * @param object
	 * @param peerObject
	 * @return
	 */
	private DataObject connectToPeer(DataObject object, PeerObject peerObject) {
		try (Socket clientSocket = socketPool.getSocket(peerObject.getPeerId());) {
			UtilityClass.connectToPeer(object, clientSocket);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return object;
	}

}

/**
 * Class to Handle Multithreaded server implementation
 * 
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
	 * Main method to handle the incoming requests
	 * 
	 */
	@Override
	public void run() {
		try {
			ObjectInputStream ois = new ObjectInputStream(peerSocket.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(peerSocket.getOutputStream());
			DataObject input = (DataObject) ois.readObject();
			peer.performOperation(input);
			oos.writeObject(input);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
