package org.javlo.service;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.message.GenericMessage;
import org.javlo.utils.MapCollectionWrapper;

public class NotificationService {

	public static final class Notification {
		private String message;
		private String url;
		private int type;
		private Date creationDate;
		private String userId;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		/**
		 * get the type, same time than in GenericMessage
		 * 
		 * @return
		 */
		public int getType() {
			return type;
		}

		public String getTypeLabel() {
			return GenericMessage.getTypeLabel(getType());
		}

		public void setType(int type) {
			this.type = type;
		}

		public Date getCreationDate() {
			return creationDate;
		}

		public void setCreationDate(Date creationDate) {
			this.creationDate = creationDate;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getTimeLabel() {
			return StringHelper.renderTime(getCreationDate());
		}
	}

	public static final class NotificationContainer {
		public NotificationContainer(Notification notification, boolean read, String userId) {
			this.notification = notification;
			this.read = read;
			this.userId = userId;
		}

		private Notification notification;
		private boolean read = false;
		private String userId;

		public Notification getNotification() {
			return notification;
		}

		public void setNotification(Notification notification) {
			this.notification = notification;
		}

		public boolean isRead() {
			return read;
		}

		public void setRead(boolean read) {
			this.read = read;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}
	}

	private static final String KEY = NotificationService.class.getName();

	/**
	 * max age of the notification in second.
	 */
	private static final long NOTIFICATION_MAX_AGE = 60 * 60;

	private List<Notification> notifications = new LinkedList<Notification>();
	private MapCollectionWrapper<String, WeakReference<Notification>> allReadyReaded = new MapCollectionWrapper<String, WeakReference<Notification>>();

	public static final NotificationService getInstance(GlobalContext globalContext) {
		NotificationService notificationService = (NotificationService) globalContext.getAttribute(KEY);
		if (notificationService == null) {
			notificationService = new NotificationService();
			globalContext.setAttribute(KEY, notificationService);
		}
		return notificationService;
	}

	public void clearList() {
		notifications = new LinkedList<Notification>();
		allReadyReaded = new MapCollectionWrapper<String, WeakReference<Notification>>();
	}

	private void cleanList() {
		long currentTime = System.currentTimeMillis();
		Collection<Notification> mustBeRemoved = new LinkedList<Notification>();
		synchronized (notifications) {
			for (Notification notif : notifications) {
				if (notif != null) {
					if (currentTime - notif.getCreationDate().getTime() > NOTIFICATION_MAX_AGE) {
						mustBeRemoved.remove(notif);
					}
				}
			}
			for (Notification notification : mustBeRemoved) {
				notifications.remove(notification);
			}
		}
	}

	private boolean isAllReadyReaded(Notification inNotif, String userId) {
		List<WeakReference<Notification>> markAsRead = allReadyReaded.get(userId);
		for (WeakReference<Notification> weakReference : markAsRead) {
			Notification notif = weakReference.get();
			if (notif == null) {
				markAsRead.remove(notif);
			} else {
				if (notif.equals(inNotif)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<NotificationContainer> getNotifications(String userId, int maxSize, boolean markRead) {
		cleanList();
		List<WeakReference<Notification>> markAsRead = allReadyReaded.get(userId);
		List<NotificationContainer> outNotif = new LinkedList<NotificationContainer>();
		int size = 0;
		for (Notification notif : notifications) {
			if (notif.getUserId() == null || notif.getUserId().equals(userId)) {
				size++;
				outNotif.add(new NotificationContainer(notif, isAllReadyReaded(notif, userId), userId));
				if (markRead) {
					markAsRead.add(new WeakReference<NotificationService.Notification>(notif));
				}
			}
			if (size == maxSize) {
				break;
			}
		}
		return outNotif;
	}

	public int getUnreadNotificationSize(String userId, int maxSize) {
		List<NotificationContainer> notifs = getNotifications(userId, maxSize, false);
		int outUnreadCount = 0;
		for (NotificationContainer notif : notifs) {
			if (!notif.isRead()) {
				outUnreadCount++;
			}
		}
		return outUnreadCount;
	}

	public void addNotification(String message, int type, String userId) {
		addNotification(message, null, type, userId);
	}

	/**
	 * add a notification.
	 * 
	 * @param message
	 *            the message of the notification
	 * @param url
	 *            a url with more information (can be null)
	 * @param type
	 *            the type of the message (same type than GenericMessage)
	 * @param userId
	 *            a notification can be specific for a user or for everybody (userId null)
	 */
	public void addNotification(String message, String url, int type, String userId) {
		Notification notif = new Notification();
		notif.setMessage(message);
		notif.setUrl(url);
		notif.setCreationDate(new Date());
		notif.setType(type);
		notif.setUserId(userId);
		notifications.add(0, notif);
	}

}
