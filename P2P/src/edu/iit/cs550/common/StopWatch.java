package edu.iit.cs550.common;

import java.util.Calendar;

public class StopWatch {

	long startTime = 0;

	public void start() {
		startTime = Calendar.getInstance().getTimeInMillis();
	}

	public long Stop() {
		long elapsed = Calendar.getInstance().getTimeInMillis() - startTime;
		startTime = 0;
		return elapsed;
	}
}
