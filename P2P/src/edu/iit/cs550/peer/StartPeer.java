package edu.iit.cs550.peer;

import edu.iit.cs550.common.Constants;
import edu.iit.cs550.common.UtilityClass;

/**
 * Convenience class to start the peer
 * @author Rajagopal
 *
 */
public class StartPeer {

	public static void main(String[] args) {
		int port = UtilityClass.getIntValue(Constants.PEERPORT);
		Peer peer = new Peer(port);
		peer.execute();

	}
}
