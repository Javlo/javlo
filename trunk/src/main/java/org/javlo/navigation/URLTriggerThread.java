package org.javlo.navigation;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class URLTriggerThread extends Thread {

	protected static Logger logger = Logger.getLogger(URLTriggerThread.class.getName());

	private URL urlToTrigger;
	private int secBetweenTrigger;

	private CountDownLatch sleepLatch;
	private CountDownLatch stoppingLatch;

	public URLTriggerThread(int minBetweenTrigger, URL urlToTrigger) {
		this.secBetweenTrigger = minBetweenTrigger;
		this.urlToTrigger = urlToTrigger;
		setName(URLTriggerThread.class.getName());
	}

	@Override
	public synchronized void run() {
		sleepLatch = new CountDownLatch(1);
		logger.info("START URL TRIGGER on " + urlToTrigger.toString());
		try {
			sleepLatch.await(30, TimeUnit.SECONDS); //Delay start to avoid overload

			while (stoppingLatch == null) {
				try {
					//Trig url
					urlToTrigger.openConnection().getInputStream().close();
				} catch (Throwable t) {
					//t.printStackTrace();
				}

				sleepLatch.await(secBetweenTrigger, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		} finally {
			if (stoppingLatch != null) {
				stoppingLatch.countDown();
			}
			logger.info("STOP URL TRIGGER on " + urlToTrigger.toString());
		}
	}

	public void stopThread() {
		if (stoppingLatch == null) {
			stoppingLatch = new CountDownLatch(1);
			sleepLatch.countDown();
		}
		try {
			stoppingLatch.await();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		logger.info("URL TRIGGER on " + urlToTrigger.toString() + " stopped");
	}

}
