package edu.iit.cs550.util;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.iit.cs550.core.PeerObject;

/**
 * Class for maintaining pool of socket connections
 * 
 * @author ram
 *
 */
public class SocketPool implements Runnable {

	List<SocketHolder> sockets = Collections.synchronizedList(new ArrayList<>());

	public SocketPool() {

	}

	/**
	 * Replenishes the used connections to avoid socket creation overhead
	 */
	@Override
	public void run() {
		for (int i = 1; i <= UtilityClass.getNoOfPeers(); i++) {
			String peerId = "peer" + i;

			if (sockets.size() < UtilityClass.getIntValue("POOL")) {
				try {
					Socket socket = makeSocket(peerId);
					SocketHolder socketHolder = new SocketHolder(peerId, socket);
					sockets.add(socketHolder);
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
	private Socket makeSocket(String peerId) {
		PeerObject peerObject = UtilityClass.getPeer(peerId);
		Socket socket = null;
		try {
			socket = new Socket(peerObject.getIpAddress(), peerObject.getPort());
		} catch (IOException e) {
			System.out.println("PeerId:" + peerId);
			e.printStackTrace();
		}
		return socket;
	}

	/**
	 * Looks for a socket in the connection pool
	 * 
	 * @param peerId
	 * @return
	 */
	public Socket getSocket(String peerId) {
		Socket socket = null;
		int index = 0;
		for (SocketHolder socketHolder : sockets) {
			if (socketHolder.getPeerId().equals(peerId)) {
				socket = sockets.get(index).getSocket();
				sockets.remove(index);
			}
			index++;

		}

		if (socket == null) {
			socket = makeSocket(peerId);
		}
		return socket;
	}

}
