package edu.iit.cs550;

import edu.iit.cs550.core.Peer;
import edu.iit.cs550.util.UtilityClass;

public class StartPeer {

	public static void main(String[] args) {
		int port =UtilityClass.getIntValue("port");		
		Peer peer= new Peer(port);
		peer.execute();
		
	}
}
