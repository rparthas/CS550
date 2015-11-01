package edu.iit.cs550.peer;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.iit.cs550.common.Constants;
import edu.iit.cs550.common.UtilityClass;

/**
 * Class for maintaining pool of socket connections
 * 
 * @author Rajagopal
 *
 */
public class SocketPool implements Runnable {

	Map<String, List<Socket>> socketHolder = new ConcurrentHashMap<>();

	PeerObject peerObject = null;

	public SocketPool(PeerObject peerObject) {

	}

	/**
	 * Replenishes the used connections to avoid socket creation overhead
	 */
	@Override
	public void run() {
		for (int i = 1; i <= UtilityClass.getNoOfPeers(); i++) {
			String peerId = Constants.PEER + i;
			List<Socket> sockets = socketHolder.get(peerId);
			if (sockets == null) {
				sockets = new ArrayList<>();
				socketHolder.put(peerId, sockets);
			}
			if (sockets.size() < UtilityClass.getIntValue(Constants.PEERPOOL)) {
				try {
					PeerObject obj = UtilityClass.getPeer(peerId);
					if (obj.equals(peerObject)) {
						continue;
					}
					Socket socket = makeSocket(obj);
					sockets.add(socket);
				} catch (Exception e) {
					continue;
				}
			}

		}
	}

	/**
	 * Creates a socket for the given peer
	 * 
	 * @param peerId
	 * @return
	 */
	private Socket makeSocket(PeerObject peerObject) {
		Socket socket = null;
		try {
			socket = new Socket(peerObject.getIpAddress(), peerObject.getPort());
		} catch (IOException e) {

		}
		return socket;
	}

	/**
	 * Looks for a socket in the connection pool
	 * 
	 * @param peerId
	 * @return
	 */
	public synchronized Socket getSocket(String peerId) {
		Socket socket = null;
		List<Socket> sockets = socketHolder.get(peerId);
		if (sockets != null && sockets.size() > 0) {
			socket = sockets.get(0);
			sockets.remove(0);
		}
		if (socket == null) {
			PeerObject obj = UtilityClass.getPeer(peerId);
			socket = makeSocket(obj);
		}
		return socket;
	}

}
