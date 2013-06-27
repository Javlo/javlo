package org.javlo.client.localmodule.service;

import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javlo.client.localmodule.model.RemoteNotification;
import org.javlo.client.localmodule.model.ServerConfig;
import org.javlo.client.localmodule.model.ServerStatus;
import org.javlo.client.localmodule.model.ServerType;

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
					factory.onWorkStart();
					refreshNotifications();
					factory.onWorkFinish();
					//Wait next loop
					logger.info("- wait ----------------------------------------------------------");
					waiting = true;
					lock.wait(WAIT_TIME);
					waiting = false;
				}
			}
		} catch (Exception ex) {
			factory.getTray().displayErrorMessage(factory.getI18n().get("error.refresh.fatal"), ex, false);
			logger.log(Level.SEVERE, ex.getClass().getSimpleName() + " occured during refresh process", ex);
		}
	}

	private void refreshNotifications() {
		Map<ServerConfig, ServerStatus> previousStatuses = new HashMap<ServerConfig, ServerStatus>();
		ConfigService config = factory.getConfig();
		for (ServerConfig server : config.getBean().getServers()) {
			previousStatuses.put(server, factory.getClient(server).getStatus());
		}
		List<RemoteNotification> allNewNotifications = new ArrayList<RemoteNotification>();
		for (ServerConfig server : factory.getConfig().getBean().getServers()) {
			ServerClientService client = factory.getClient(server);
			ServerType type = server.getType();
			if (type == null) {
				type = ServerType.OTHER;
			}
			switch (type) {
			case JAVLO2:
				List<RemoteNotification> notifications = client.getNewDataNotifications();
				if (notifications != null && !notifications.isEmpty()) {
					allNewNotifications.addAll(notifications);
				}
				break;
			case OTHER:
				client.checkThePhrase();
				break;
			default:
				logger.warning("Unmanaged enum value: " + server.getType());
				break;
			}
		}
		if (!allNewNotifications.isEmpty()) {
			Collections.sort(allNewNotifications, new Comparator<RemoteNotification>() {
				@Override
				public int compare(RemoteNotification o1, RemoteNotification o2) {
					return o1.getCreationDate().compareTo(o2.getCreationDate());
				}
			});
		}
		Map<ServerConfig, ServerStatus> serverToNotify = new LinkedHashMap<ServerConfig, ServerStatus>();
		for (ServerConfig server : config.getBean().getServers()) {
			ServerStatus previousStatus = previousStatuses.get(server);
			ServerStatus newStatus = factory.getClient(server).getStatus();
			if (previousStatus != newStatus) {
				if (newStatus.compareTo(ServerStatus.OK) >= 1) {
					serverToNotify.put(server, newStatus);
				}
			}
		}
		boolean showNotif = true;
		if (!serverToNotify.isEmpty()) {
			I18nService i18n = factory.getI18n();
			String msg = "";
			for (Entry<ServerConfig, ServerStatus> entry : serverToNotify.entrySet()) {
				msg += entry.getKey().getLabel() + ": " + i18n.get("status.server." + entry.getValue().name().toLowerCase()) + "\n";
			}
			factory.getTray().displayMessage(i18n.get("alert.new-troubles"), msg, MessageType.WARNING, false, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					factory.getAction().showStatus();
				}
			});
			showNotif = false;
		}
		factory.getNotificationService().pushNotifications(allNewNotifications, showNotif);
	}
}
