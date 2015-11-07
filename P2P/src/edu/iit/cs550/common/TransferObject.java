package edu.iit.cs550.common;

import java.io.Serializable;
import java.util.List;

public class TransferObject implements Serializable {

	private static final long serialVersionUID = 5872778768159963837L;

	private String ipAddress;

	private String directory;

	private int port;

	private String fileName;

	private boolean requestFile;

	private List<FileServerObject> peers;

	public List<FileServerObject> getPeers() {
		return peers;
	}

	public void setPeers(List<FileServerObject> peers) {
		this.peers = peers;
	}

	@Override
	public String toString() {
		return "TransferObject [ipAddress=" + ipAddress + ", directory=" + directory + ", port=" + port
				+ ", requestFileName=" + fileName + ", requestFile=" + requestFile + ", peers=" + peers + "]";
	}

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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isRequestFile() {
		return requestFile;
	}

	public void setRequestFile(boolean requestFile) {
		this.requestFile = requestFile;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

}
