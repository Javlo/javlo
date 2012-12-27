package org.javlo.thread;

import java.io.File;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

public class ThreadManager extends Thread {
	
	private static final String KEY = ThreadManager.class.getName();
	
	AbstractThread currentThread;

	private class LocalThread extends Thread {

		private AbstractThread thread = null;

		boolean running = true;

		LocalThread(AbstractThread thread) {
			this.thread = thread;
		}

		@Override
		public void run() {

			thread.run();

			running = false;
		}
	}

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ThreadManager.class.getName());

	private static final long WAIT_BETWEEN_THREAD = 100;

	private static final long WAIT_BETWEEN_TIMEOUT_CHECK = 1000;

	private static final long WAIT_BETWEEN_THREAD_LIST = 10000;

	public Boolean stop = false;

	private File threadDir = null;
	
	private ThreadManager(){};
	
	public static ThreadManager getInstance(ServletContext application) {
		ThreadManager instance = (ThreadManager) application.getAttribute(KEY);
		if (instance == null) {
			instance = new ThreadManager();
			application.setAttribute(KEY, instance);
		}
		return instance;
	}

	@Override
	public void run() {
		while (!stop) {
			File[] threadFiles = threadDir.listFiles(AbstractThread.threadFileFilter);
			if (threadFiles != null) {
				if (threadFiles.length > 0) {
					logger.info("thread found : " + threadFiles.length);
				}
				for (File file : threadFiles) {
					if (!stop) {
						
						try {
							currentThread = AbstractThread.getInstance(file);
							if (currentThread != null && currentThread.needRunning()) {
								LocalThread localThread = new LocalThread(currentThread);
								localThread.start();
								long startTime = System.currentTimeMillis();
								long currentTime = System.currentTimeMillis() - startTime;
								while ((currentTime < currentThread.getTimeout()) && localThread.running) {
									sleep(WAIT_BETWEEN_TIMEOUT_CHECK);
									currentTime = System.currentTimeMillis() - startTime;
								}
								if (localThread.running) {
									localThread.interrupt();
								}
								currentThread.destroy();

								logger.info("end run : " + currentThread.getClass().getName() + " - info : " + currentThread.logInfo());
							}
						} catch (Throwable e) {
							e.printStackTrace();
						}
						try {
							Thread.sleep(WAIT_BETWEEN_THREAD);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			try {
				Thread.sleep(WAIT_BETWEEN_THREAD_LIST);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized (stop) {
			stop.notify();
		}
	}

	public void setThreadDir(File threadDir) {
		this.threadDir = threadDir;
	}
	
	public int countThread() {
		File[] threadFiles = threadDir.listFiles(AbstractThread.threadFileFilter);
		if (threadFiles == null) {
			return 0;
		}
		return threadFiles.length;
	}
	
	public int purgeAllThread() {
		File[] threadFiles = threadDir.listFiles(AbstractThread.threadFileFilter);
		int fileDeleted = 0;
		for (int i = 0; i < threadFiles.length; i++) {
			if (threadFiles[i].delete()) {
				fileDeleted++;
			}
		}
		return fileDeleted;
	}
	
	public String getCurrentThreadName() {
		if (currentThread != null) {
			return currentThread.getClass().getSimpleName();
		}
		return "";
	}
	
	public String getCurrentThreadInfo() {
		if (currentThread != null) {
			return currentThread.logInfo();
		}
		return "";
	}

}
