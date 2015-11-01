package edu.iit.cs550.peer;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.iit.cs550.common.Constants;
import edu.iit.cs550.common.UtilityClass;

/**
 * Class for maintaining pool of socket connections
 * 
 * @author Rajagopal
 *
 */
public class SocketPool implements Runnable {

	List<SocketHolder> sockets = Collections.synchronizedList(new ArrayList<>());

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
			if (sockets.size() < UtilityClass.getIntValue(Constants.PEERPOOL)) {
				try {
					PeerObject obj = UtilityClass.getPeer(peerId);
					if (obj.equals(peerObject)) {
						continue;
					}
					Socket socket = makeSocket(obj);
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
	private Socket makeSocket(PeerObject peerObject) {
		Socket socket = null;
		try {
			socket = new Socket(peerObject.getIpAddress(), peerObject.getPort());
		} catch (IOException e) {
			System.out.println("PeerId:" + peerObject.getPeerId());
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
			PeerObject obj = UtilityClass.getPeer(peerId);
			socket = makeSocket(obj);
		}
		return socket;
	}

}
