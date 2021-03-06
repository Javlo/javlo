package org.javlo.navigation;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.javlo.helper.DebugHelper;
import org.javlo.helper.ResourceHelper;

public class URLTriggerThread extends Thread {
	
	private static long COUNT = 0;

	protected static Logger logger = Logger.getLogger(URLTriggerThread.class.getName());

	private URL urlToTrigger;
	private int secBetweenTrigger;

	private CountDownLatch sleepLatch;
	private CountDownLatch stoppingLatch;

	public URLTriggerThread(String threadName, int secBetweenTrigger, URL urlToTrigger) {
		super(threadName);
		logger.info("create URLTriggerThread : "+threadName+" "+secBetweenTrigger+" "+urlToTrigger+ " caller:"+DebugHelper.getCaller());
		this.setDaemon(true);
		this.secBetweenTrigger = secBetweenTrigger;
		this.urlToTrigger = urlToTrigger;
		setName(URLTriggerThread.class.getName());
	}

	@Override
	public synchronized void run() {		
		sleepLatch = new CountDownLatch(1);
		logger.info("START URL TRIGGER on " + urlToTrigger.toString());
		try {
			sleepLatch.await(5, TimeUnit.SECONDS); //Delay start to avoid overload
			while (stoppingLatch == null) {
				InputStream in=null;
				try {
					//Trig url
					in = urlToTrigger.openConnection().getInputStream();
				} catch (Throwable t) {
					//t.printStackTrace();
				} finally {
					ResourceHelper.closeResource(in);
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
