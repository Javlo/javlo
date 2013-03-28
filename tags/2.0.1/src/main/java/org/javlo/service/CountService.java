package org.javlo.service;

import java.util.Arrays;
import java.util.Calendar;

import javax.servlet.ServletContext;

public class CountService {

	private final Integer[] countArrayMinute = new Integer[60];
	private long latestTime;

	private long allTouch = 0;
	private final long startTime = Calendar.getInstance().getTimeInMillis() / 1000;

	private static CountService instance = null;

	private static final String SERVLETCONTEXT_KEY = "globalCount";

	public static CountService getInstance(ServletContext application) {
		CountService res = (CountService) application.getAttribute(SERVLETCONTEXT_KEY);
		if (res == null) {
			res = new CountService();
			application.setAttribute(SERVLETCONTEXT_KEY, res);
		}
		return res;
	}

	/**
	 * only for debug
	 * 
	 * @return
	 */
	private static CountService getInstance() {

		if (instance == null) {
			instance = new CountService();

		}
		return instance;
	}

	private int getIndex(long infinityIndex) {
		if (infinityIndex < 0) {
			return countArrayMinute.length - (int) (infinityIndex % countArrayMinute.length);
		} else {
			return (int) infinityIndex % countArrayMinute.length;
		}
	}

	public void touch() {
		touch(true);
	}

	private synchronized void touch(boolean increment) {
		long time = Calendar.getInstance().getTimeInMillis() / 1000;
		if (time - countArrayMinute.length > latestTime) {
			Arrays.fill(countArrayMinute, 0);
			latestTime = time;
		}
		for (long i = time - 1; i > latestTime; i--) {
			countArrayMinute[getIndex(i)] = 0;
		}
		if (increment) {
			allTouch++;
			if (time == latestTime) {
				countArrayMinute[getIndex(time)]++;
			} else {
				countArrayMinute[getIndex(time)] = 1;
			}
		}
		latestTime = time;
	}

	public int getCount() {
		touch(false);
		int c = 0;
		for (Integer element : countArrayMinute) {
			c = c + element;
		}
		return c;
	}

	public int getAverage() {
		long time = Calendar.getInstance().getTimeInMillis() / 1000;
		return (int) ((allTouch * countArrayMinute.length) / (time - startTime));
	}

	public static void main(String[] args) {
		try {
			CountService count = CountService.getInstance();
			count.touch();
			Thread.sleep(1000);
			count.touch();
			System.out.println("1.total : " + count.getCount());
			Thread.sleep(1000);
			count.touch();
			System.out.println("2.total : " + count.getCount());
			Thread.sleep(1000);
			count.touch();
			System.out.println("3.total : " + count.getCount());
			Thread.sleep(1000);
			count.touch();
			System.out.println("4.total : " + count.getCount());
			Thread.sleep(1000);
			count.touch();
			System.out.println("5.total : " + count.getCount());
			Thread.sleep(1000);
			count.touch();
			System.out.println("6.total : " + count.getCount());
			Thread.sleep(1000);
			count.touch();
			System.out.println("7.total : " + count.getCount());
			Thread.sleep(8000);
			count.touch();
			System.out.println("8.total : " + count.getCount());
			Thread.sleep(1000);
			count.touch();
			System.out.println("9.total : " + count.getCount());

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
