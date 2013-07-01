package org.javlo.module.remote;

import java.util.logging.Logger;

public class RemoteThread extends Thread {
	
	private static Logger logger = Logger.getLogger(RemoteThread.class.getName());

	private long TIME_BETWEEN_CHECK = 60 * 100; // 10 sec

	private boolean stop = false;
	private long countCheck = 0;
	RemoteService remoteService;
	
	public RemoteThread(RemoteService remoteService) {
		setDaemon(true);
		setName("remote thread : "+remoteService.getGlobalContext().getContextKey());
		this.remoteService = remoteService;
	}

	@Override
	public void run() {
		super.run();
		try {
			while (!stop) {
				countCheck++;
				for (RemoteBean bean : remoteService.getRemotes()) {
					if (bean.getPriority() == RemoteBean.PRIORITY_HIGH) {
						bean.check();
					} else if (bean.getPriority() == RemoteBean.PRIORITY_MIDDLE && countCheck%100==0) {
						bean.check();						
					} else if (bean.getPriority() == RemoteBean.PRIORITY_LOW && countCheck%1000==0) {
						bean.check();
					}
					if (!bean.isValid()) {
						logger.warning("error on : "+bean.getUrl()+" msg:"+bean.getError());
					}
				}
				remoteService.sendNotification();
				sleep(TIME_BETWEEN_CHECK);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

}
