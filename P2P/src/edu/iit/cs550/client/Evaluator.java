package edu.iit.cs550.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.iit.cs550.common.UtilityClass;
import edu.iit.cs550.peer.Peer;

/**
 * Main Class for Evaluating the system
 * 
 * @author Rajagopal
 *
 */
public class Evaluator implements Callable<Evaluator> {

	EvalServer evalServer = null;

	public static void main(String[] args) throws Exception {

		ExecutorService es = Executors.newFixedThreadPool(8);
		int ops = Integer.parseInt(args[0]);

		// code for initializing the DHT
		int peerStartPort = 2343;
		for (int i = 0; i < UtilityClass.getNoOfPeers(); i++) {
			Peer peer = new Peer(peerStartPort + i);
			peer.execute();
		}
		List<Evaluator> tasks = new ArrayList<Evaluator>();
		for (int i = 1; i <= UtilityClass.getNoOfPeers(); i++) {
			EvalServer evalServer = new EvalServer();
			evalServer.ops = ops;
			evalServer.port = 2350 + i;
			evalServer.directory = "/media/ram/Data/IIT/CS 550/PA3/server" + i;
			int lookupIndex = i != UtilityClass.getNoOfPeers() ? i + 1 : 1;
			evalServer.lookupFile = "s" + lookupIndex;
			Evaluator evaluator = new Evaluator();
			evaluator.evalServer = evalServer;
			tasks.add(evaluator);
		}
		es.invokeAll(tasks);
		System.exit(0);

	}

	@Override
	public Evaluator call() throws Exception {
		evalServer.run();
		return this;
	}
}
