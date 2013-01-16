package org.javlo.helper;

public class CountThreadService {
	
	private static final CountThreadService instance = new CountThreadService();
	
	private int countThread=0;
	
	private int countSleepThread=0;
	
	public static CountThreadService getInstance() {
		return instance;
	}
	
	public void startThread() {
		countThread++;
	}
	
	public void endThread() {
		countThread--;
	}
	
	public int getCountThread() {
		return countThread;
	}
	
	public void startSleepThread() {
		countSleepThread++;
	}
	
	public void endSleepThread() {
		countSleepThread--;
	}
	
	public int getCountSleepThread() {
		return countSleepThread;
	}


}
