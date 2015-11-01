package edu.iit.cs550.peer;

import java.net.Socket;
/**
 * Class to hold the socket and peer Id to be used in connection pool
 * @author Rajagopal
 *
 */
public class SocketHolder {

	public String peerId;
	public Socket socket;

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public SocketHolder(String peerId, Socket socket) {
		super();
		this.peerId = peerId;
		this.socket = socket;
	}

}
