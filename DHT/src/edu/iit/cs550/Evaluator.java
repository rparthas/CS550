package edu.iit.cs550;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.iit.cs550.core.DataObject;
import edu.iit.cs550.core.Peer;
import edu.iit.cs550.core.PeerObject;
import edu.iit.cs550.util.UtilityClass;

/**
 * Test class for evaluating the system
 * 
 * @author Raja
 *
 */
public class Evaluator implements Callable<Evaluator> {

	PeerObject peerObject = null;
	Object key = null;
	Object value = null;
	String operation = null;

	public static void main(String args[]) throws Exception {
		int start = 2343;
		int peerCount = UtilityClass.getNoOfPeers();

		for (int i = start; i <= start + peerCount - 1; i++) {
			Peer peer = new Peer(i);
			peer.execute();
		}
		Thread.sleep(5000);
		int[] opnCount = { 10 ^ 3, 10 ^ 4, 10 ^ 5 };
		for (int ops : opnCount) {
			performOperation(peerCount, ops, "PUT");
			performOperation(peerCount, ops, "GET");
			performOperation(peerCount, ops, "REM");
		}

		System.exit(0);

	}

	private static void performOperation(int peerCount, int ops, String operation) throws InterruptedException {

		List<Callable<Evaluator>> tasks = new LinkedList<>();
		ExecutorService es = Executors.newFixedThreadPool(200);
		long time = Calendar.getInstance().getTimeInMillis();
		int opnsCount = 0;
		for (int i = 1; i <= peerCount; i++) {
			for (int j = 1; j <= ops; j++) {
				opnsCount++;
				Evaluator evaluator = new Evaluator();
				evaluator.peerObject = UtilityClass.getPeer("peer" + i);
				evaluator.operation = operation;
				evaluator.key = opnsCount;
				evaluator.value = opnsCount;
				tasks.add(evaluator);
			}

		}
		es.invokeAll(tasks);
		tasks.clear();
		System.out.println((Calendar.getInstance().getTimeInMillis() - time) / 1000.0);

	}

	@Override
	public Evaluator call() throws Exception {

		DataObject doj = new DataObject(key, value, operation);
		UtilityClass.connectToDHTPeer(doj, peerObject);
		return this;
	}

}
