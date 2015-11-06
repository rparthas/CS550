package edu.iit.cs550.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.iit.cs550.common.FileServerObject;
import edu.iit.cs550.common.TransferObject;
import edu.iit.cs550.common.UtilityClass;
import edu.iit.cs550.peer.SocketPool;

/**
 * This is the main class for peer
 * 
 * @author Rajagopal
 *
 */
public class FileServer implements Callable<Object> {

	int port = 0;

	String directory = null;

	int threadCount = 0;

	private Socket socket = null;// Create a socket for the server

	SocketPool socketPool = new SocketPool(null);

	public FileServer(int port, String directory, int threadCount) throws Exception {
		this.port = port;
		this.directory = directory;
		this.threadCount = threadCount;
	}

	/**
	 * This scans the directory for files and sends the files to server
	 * 
	 * @param serverPort
	 * @param ipAddress
	 * @throws Exception
	 */
	public void register() throws Exception {
		try  {

			File folder = new File(directory);
			File[] listOfFiles = folder.listFiles();

			TransferObject to = new TransferObject();
			to.setDirectory(directory);
			to.setIpAddress(UtilityClass.getMyIP());
			to.setPort(port);
			to.setRequestFile(false);
			if (listOfFiles != null) {
				for (File file : listOfFiles) {
					if (file.isFile()) {
						List<String> files = new ArrayList<String>();
						to.setFiles(files);
						files.add(file.getName());
						Socket clientSocket = openSocket();
						UtilityClass.writeObject(to, clientSocket);
						UtilityClass.readObject(clientSocket);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * THis is the method to open a socket to start accepting connections
	 */
	public void connect() {

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		try (ServerSocket serverSocket = new ServerSocket(port, threadCount);) {

			while (true) {
				socket = serverSocket.accept();
				executorService.submit(this);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	/**
	 * This method is to shutdown the peer
	 * 
	 * @throws Exception
	 */
	public void shutDown() {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * THis method is to open a socket if it was closed already or open it for
	 * the first time
	 * 
	 * @throws Exception
	 */
	public Socket openSocket() throws Exception {
		int noOfPeers = UtilityClass.getNoOfPeers();
		Socket socket = null;
		while (socket == null || socket.isClosed()) {
			int peerId = (int) Math.random() * noOfPeers;
			if (peerId == 0) {
				peerId = 1;
			}
			socket = socketPool.getSocket("peer" + peerId);
		}
		return socket;
	}

	/**
	 * This method is to send the requested file by another peer. It recognizes
	 * if file is requested then just buffers the file and sends it
	 */
	@Override
	public Object call() throws Exception {
		try {
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			TransferObject to = UtilityClass.readObject(socket);
			if (to != null) {
				if (to.isRequestFile()) {
					File file = new File(directory + "/" + to.getRequestFileName());
					dos.writeUTF(file.getName());
					Files.copy(file.toPath(), dos);
				}
			}
			dos.flush();
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method is used to lookup the file and send the file
	 * 
	 * @throws Exception
	 */
	public List<FileServerObject> lookUpFile(String fileName) throws Exception {
		List<FileServerObject> peers = new ArrayList<FileServerObject>();
		try (Socket clientSocket = openSocket();) {
			TransferObject to = new TransferObject();
			to.setRequestFile(true);
			to.setRequestFileName(fileName);
			UtilityClass.writeObject(to, clientSocket);
			to = UtilityClass.readObject(clientSocket);
			if (to != null) {
				peers = to.getPeers();
			}
		} catch (Exception e) {

		}
		return peers;
	}

	/**
	 * This method is used to download the file from another peer and save it to
	 * local directory
	 * 
	 * @param fileServerObject
	 * @param fileName
	 */
	public void downloadFile(FileServerObject fileServerObject, String fileName) {
		try (Socket clientSocket = new Socket(InetAddress.getByName(fileServerObject.getIpAddress()),
				fileServerObject.getPort());) {
			TransferObject to = new TransferObject();
			to.setRequestFile(true);
			to.setRequestFileName(fileName);
			UtilityClass.writeObject(to, clientSocket);
			try (BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream());
					DataInputStream din = new DataInputStream(in);) {
				if (din != null) {
					String path = directory + "/" + fileName;
					File file = new File(path);
					if (file.exists()) {
						//System.out.println("file already exists");
					} else {
						Files.copy(din, Paths.get(path));
					}
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

}
