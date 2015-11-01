package edu.iit.cs550.peer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.iit.cs550.common.Constants;
import edu.iit.cs550.common.FileServerObject;
import edu.iit.cs550.common.TransferObject;
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

	Map<String, List<FileServerObject>> registry = new LinkedHashMap<String, List<FileServerObject>>();

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
	public void performOperation(TransferObject to) {
		String hash = to.getIpAddress() + ":" + to.getPort();
		int peerNo = computeHash(hash);
		String peer = Constants.PEER + peerNo;
		try {
			PeerObject obj = UtilityClass.getPeer(peer);
			if (peerObject.equals(obj)) {
				if (to.isRequestFile()) {
					List<FileServerObject> peers = lookUpFile(to.getRequestFileName());
					to.setPeers(peers);
				} else {
					saveToRegistry(to);
				}
			} else {
				connectToPeer(to, obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method saves the object to registry
	 */
	private void saveToRegistry(TransferObject to) {
		if (to.getFiles() != null) {
			for (String file : to.getFiles()) {
				List<FileServerObject> peers = new ArrayList<FileServerObject>();
				if (registry.containsKey(file)) {
					peers = registry.get(file);
				}
				FileServerObject fileServerObject = new FileServerObject();
				fileServerObject.setDirectory(to.getDirectory());
				fileServerObject.setIpAddress(to.getIpAddress());
				fileServerObject.setPort(to.getPort());
				peers.add(fileServerObject);
				registry.put(file, peers);
			}
		}
	}

	/**
	 * This method returns the Peer list for the files
	 * 
	 * @param fileName
	 */
	private List<FileServerObject> lookUpFile(String fileName) {
		List<FileServerObject> peers = new ArrayList<FileServerObject>();
		if (registry.containsKey(fileName)) {
			peers = registry.get(fileName);
		}
		return peers;
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
	private TransferObject connectToPeer(TransferObject object, PeerObject peerObject) {
		try (Socket clientSocket = socketPool.getSocket(peerObject.getPeerId());) {
			UtilityClass.writeObject(object, clientSocket);
			object = UtilityClass.readObject(clientSocket);
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
			TransferObject input = (TransferObject) ois.readObject();
			peer.performOperation(input);
			oos.writeObject(input);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
