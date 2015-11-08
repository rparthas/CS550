package edu.iit.cs550.server;

import java.util.List;
import java.util.Scanner;

import edu.iit.cs550.common.Constants;
import edu.iit.cs550.common.FileServerObject;
import edu.iit.cs550.common.UtilityClass;

public class StartFileServer implements Runnable {

	public FileServer fileServer = null;

	public static void main(String[] args) {

		int port = UtilityClass.getIntValue(Constants.FILESERVERPORT);
		String directory = UtilityClass.getValue(Constants.DIRECTORY);
		int threads = UtilityClass.getIntValue(Constants.SERVERTHREADS);
		FileServer fileServer = null;
		StartFileServer startFileServer = new StartFileServer();
		try (Scanner scanner = new Scanner(System.in);) {
			fileServer = new FileServer(port, directory, threads);
			fileServer.register();
			startFileServer.fileServer = fileServer;
			new Thread(startFileServer).start();
			while (true) {
				System.out.println("1:Lookup File \n2:Exit");
				System.out.println("Please Enter your choice");
				int choice = 0;
				try {
					choice = Integer.parseInt(scanner.nextLine());
					if (choice == 1) {
						boolean fileLoop = true;
						while (fileLoop) {
							System.out.println("Please Enter the filename");
							String fileName = scanner.nextLine();
							if (fileName != null || !"".equals(fileName)) {
								List<FileServerObject> peers = fileServer.lookUpFile(fileName);
								if (peers == null || peers.size() == 0) {
									System.out.println("File not found");
									continue;
								}
								int peerId = 0;
								for (FileServerObject fileServerObject : peers) {
									peerId++;
									System.out.println("Peer " + peerId + " Details:" + fileServerObject);

								}
								chooseServer(fileServer, scanner, fileName, peers);
								fileLoop = false;
							}
						}

					} else if (choice == 2) {
						fileServer.shutDown();
						System.exit(0);
					} else {
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method to get the peer to download
	 * 
	 * @param fileServer
	 * @param scanner
	 * @param fileName
	 * @param peers
	 * @param peerLoop
	 */
	private static void chooseServer(FileServer fileServer, Scanner scanner, String fileName, List<FileServerObject> servers) {
		int peerId = 0;
		boolean peerLoop = true;
		while (peerLoop) {
			System.out.println("Please Enter the peer to connect to");
			try {
				peerId = Integer.parseInt(scanner.nextLine());
				if (peerId <= 0 || peerId > servers.size()) {
					continue;
				} else {
					boolean download=fileServer.downloadFile(servers.get(peerId - 1), fileName);
					if(download)
					System.out.println("File Download successful");
					else
					System.out.println("Choose another peer as download failed");
					peerLoop = false;
				}

			} catch (Exception e) {
				continue;
			}
		}
	}

	@Override
	public void run() {
		fileServer.connect();
	}
}
