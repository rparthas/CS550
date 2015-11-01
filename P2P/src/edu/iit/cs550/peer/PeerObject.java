package edu.iit.cs550.peer;

/**
 * Data transfer object holding information about the peer
 * @author Rajagopal
 *
 */
public class PeerObject {

	private String peerId;

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	private String ipAddress;

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private int port = 0;

	public PeerObject(String ipAddress, int port, String id) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.peerId = id;
	}

	public PeerObject() {

	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof PeerObject) {
			PeerObject obj = (PeerObject) object;
			if (obj != null && obj.ipAddress != null && obj.ipAddress.equals(ipAddress) && obj.port == port) {
				return true;
			}
		}
		return false;
	}
}
