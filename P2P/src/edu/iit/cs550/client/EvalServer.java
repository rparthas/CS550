package edu.iit.cs550.client;

import java.util.List;

import edu.iit.cs550.common.Constants;
import edu.iit.cs550.common.FileServerObject;
import edu.iit.cs550.common.StopWatch;
import edu.iit.cs550.common.UtilityClass;
import edu.iit.cs550.server.FileServer;
import edu.iit.cs550.server.StartFileServer;

public class EvalServer {

	public int port = 0;
	public String directory = null;
	public String lookupFile = null;
	int ops =0;

	public void run() throws Exception {
		StopWatch sw = new StopWatch();
		int threads = UtilityClass.getIntValue(Constants.SERVERTHREADS);
		StartFileServer startFileServer = new StartFileServer();
		startFileServer.fileServer = new FileServer(port, directory, threads);
		new Thread(startFileServer).start();
		Thread.sleep(5000);
		sw.start();
		startFileServer.fileServer.register();
		System.out.println("Register Time:" + sw.Stop());
		sw.start();
		for (int i = 1; i <= ops; i++) {
			startFileServer.fileServer.lookUpFile(lookupFile + i + ".bin");
		}
		System.out.println("Lookup Time:" + sw.Stop());
		sw.start();
		for (int i = 1; i <= ops; i++) {
			List<FileServerObject> servers = startFileServer.fileServer.lookUpFile(lookupFile + i + ".bin");
			if (servers!=null && servers.size() > 0) {
				startFileServer.fileServer.downloadFile(servers.get(0), lookupFile + i + ".bin");
			}
		}
		System.out.println("Download Time:" + sw.Stop());

	}
}