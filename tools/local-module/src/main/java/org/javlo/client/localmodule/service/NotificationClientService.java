package org.javlo.client.localmodule.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javlo.client.localmodule.model.RemoteNotification;
import org.javlo.client.localmodule.model.ServerConfig;

public class NotificationClientService {
	private static final Logger logger = Logger.getLogger(NotificationClientService.class.getName());

	private static final long WAIT_TIME = 10 * 1000; //10 secondes

	private static NotificationClientService instance;
	public static NotificationClientService getInstance() {
		synchronized (NotificationClientService.class) {
			if (instance == null) {
				instance = new NotificationClientService();
			}
			return instance;
		}
	}

	private ServiceFactory factory = ServiceFactory.getInstance();

	private final Object lock = new Object();
	private Thread synchroThread = null;
	private boolean stopping = false;

	private boolean waiting = false;

	private NotificationClientService() {
	}

	public void start() {
		synchronized (lock) {
			if (!isStarted()) {
				stopping = false;
				synchroThread = new Thread(NotificationClientService.class.getSimpleName()) {
					{
						setDaemon(true);
					}
					@Override
					public void run() {
						try {
							work();
						} finally {
							synchronized (lock) {
								synchroThread = null;
								stopping = false;
							}
						}
					}
				};
				synchroThread.start();
			}
		}
	}

	public boolean isStarted() {
		synchronized (lock) {
			return synchroThread != null;
		}
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void wakeUp() {
		if (isWaiting()) {
			synchronized (lock) {
				lock.notify();
			}
		}
	}

	public void stop() {
		synchronized (lock) {
			if (isStarted() && !stopping) {
				stopping = true;
				lock.notify();
			}
		}
	}

	private void work() {
		try {
			synchronized (lock) {
				while (!stopping) {
					refreshNotifications();
					//Wait next loop
					logger.info("- wait ----------------------------------------------------------");
					waiting = true;
					lock.wait(WAIT_TIME);
					waiting = false;
				}
			}
		} catch (Exception ex) {
			factory.getTray().displayErrorMessage(factory.getI18n().get("error.synchro.fatal"), ex, false);
			logger.log(Level.SEVERE, ex.getClass().getSimpleName() + " occured during synchro process", ex);
		}
	}

	private void refreshNotifications() {
		List<RemoteNotification> allNewNotifications = new ArrayList<RemoteNotification>();
		for (ServerConfig server : factory.getConfig().getBean().getServers()) {
			ServerClientService client = factory.getClient(server);
			try {
				List<RemoteNotification> notifications = client.getNewDataNotifications();
				if (notifications != null && !notifications.isEmpty()) {
					allNewNotifications.addAll(notifications);
				}
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Exception on notification request.", ex);
			}
		}
		if (!allNewNotifications.isEmpty()) {
			Collections.sort(allNewNotifications, new Comparator<RemoteNotification>() {
				@Override
				public int compare(RemoteNotification o1, RemoteNotification o2) {
					return o1.getCreationDate().compareTo(o2.getCreationDate());
				}
			});
			factory.getNotificationService().pushNotifications(allNewNotifications);
		}
	}
}
