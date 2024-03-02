package org.javlo.service.notification;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.*;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserInfo;
import org.javlo.user.IUserInfo;
import org.javlo.utils.JSONMap;
import org.javlo.utils.MapCollectionWrapper;

import javax.mail.internet.InternetAddress;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.*;

public class NotificationService {

	private static final String USER_SYSTEM = "SYSTEM";

	public static final class NotificationContainer {

		private Notification notification;
		private boolean read = false;
		private String userId;

		public NotificationContainer(Notification notification, boolean read, String userId) {
			this.notification = notification;
			this.read = read;
			this.userId = userId;
		}

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

	private final List<Notification> notifications = new LinkedList<Notification>();
	private final MapCollectionWrapper<String, WeakReference<Notification>> allReadyReaded = new MapCollectionWrapper<String, WeakReference<Notification>>();

	public static final NotificationService getInstance(GlobalContext globalContext) {
		NotificationService notificationService = (NotificationService) globalContext.getAttribute(KEY);
		if (notificationService == null) {
			notificationService = new NotificationService();
			globalContext.setAttribute(KEY, notificationService);
		}
		return notificationService;
	}

	public void clearList() {
		synchronized (notifications) {
			notifications.clear();
			allReadyReaded.clear();
		}
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
				markAsRead.remove(weakReference);
			} else {
				if (notif.equals(inNotif)) {
					return true;
				}
			}
		}
		return false;
	}

	public void markAllAsRead(String userId) {
		System.out.println(">>>>>>>>> NotificationService.markAllAsRead : userId = "+userId); //TODO: remove debug trace
		List<WeakReference<Notification>> markAsRead = allReadyReaded.get(userId);
		synchronized (notifications) {
			for (Notification notif : notifications) {
				if (notif.getReceiver() == null || notif.getReceiver().equals(userId) || notif.getUserId().equals(USER_SYSTEM)) {
					System.out.println(">>>>>>>>> NotificationService.markAllAsRead : noctif = "+notif.getMessage()); //TODO: remove debug trace
					markAsRead.add(new WeakReference<Notification>(notif));
				}
			}
		}
	}

	public List<NotificationContainer> getNotifications(String userId, boolean admin, int maxSize, boolean markRead) {
		if (userId == null) {
			return Collections.emptyList();
		}
		cleanList();
		List<WeakReference<Notification>> markAsRead = allReadyReaded.get(userId);
		List<NotificationContainer> outNotif = new LinkedList<NotificationContainer>();
		int size = 0;
		synchronized (notifications) {
			for (Notification notif : notifications) {
				if (notif.getReceiver() == null || notif.getReceiver().equals(userId) || notif.getUserId().equals(USER_SYSTEM)) {
					if (admin || !notif.isAdmin()) {
						size++;
						outNotif.add(new NotificationContainer(notif, isAllReadyReaded(notif, userId), userId));
						if (markRead) {
							markAsRead.add(new WeakReference<Notification>(notif));
						}
					}
				}
				if (size == maxSize) {
					break;
				}
			}
			for (List<WeakReference<Notification>> list : allReadyReaded.getMap().values()) {
				LangHelper.clearWeekReferenceCollection(list);
			}
		}
		return outNotif;
	}

	public List<NotificationContainer> getNotifications(int maxSize) {
		cleanList();
		List<NotificationContainer> outNotif = new LinkedList<NotificationContainer>();
		int size = 0;
		synchronized (notifications) {
			for (Notification notif : notifications) {
				size++;
				outNotif.add(new NotificationContainer(notif, isAllReadyReaded(notif, notif.getUserId()), notif.getUserId()));
				if (size == maxSize) {
					break;
				}
			}
		}
		return outNotif;
	}

	public int size() {
		if (notifications == null) {
			return -1;
		}
		return notifications.size();
	}

	public int getUnreadNotificationSize(String userId, boolean admin, int maxSize) {
		List<NotificationContainer> notifs = getNotifications(userId, admin, maxSize, false);
		int outUnreadCount = 0;
		for (NotificationContainer notif : notifs) {
			if (!notif.isRead()) {
				outUnreadCount++;
			}
		}
		return outUnreadCount;
	}

	public void addNotification(String message, int type, String userId, boolean admin) {
		addNotification(message, null, type, userId, admin);
	}

	public void addSystemNotification(ContentContext ctx, String message, int type, boolean admin) {
		addNotification(message, null, type, USER_SYSTEM, admin);
		notifExternalService(ctx, message, type, null, null, admin);
	}

	public static void notifExternalService(ContentContext ctx, String message, int type, String inURL, String userId, boolean admin) {
		notifExternalService(ctx, message, type, inURL, userId, admin, null);
	}

	public static void notifExternalService(ContentContext ctx, String message, int type, String inURL, String userId, boolean admin, Collection<String> targetUsers) {
		List<String> tokens = new LinkedList<String>();
		for (IUserInfo userInfo : AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession()).getUserInfoList()) {
			if (userInfo instanceof AdminUserInfo) {
				if (targetUsers == null || targetUsers.contains(userInfo.getLogin())) {
					String userToken = ((AdminUserInfo) userInfo).getPushbulletToken();
					if (!StringHelper.isEmpty(userToken)) {
						tokens.add(userToken);
					}
				}
			}
		}
		for (String token : tokens) {
			String url = "https://api.pushbullet.com/v2/pushes";
			Map<String, String> header = new HashMap<String, String>();
			header.put("Access-Token", token);
			Map<String, String> json = new HashMap<String, String>();
			json.put("body", message);
			json.put("title", ctx.getGlobalContext().getGlobalTitle() + " [" + type + ' ' + StringHelper.neverEmpty(userId, "?") + ']');
			if (!StringHelper.isURL(inURL)) {
				inURL = URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), inURL);
			}
			json.put("url", inURL);
			json.put("type", "link");
			try {
				NetHelper.postJsonRequest(new URL(url), null, header, JSONMap.JSON.toJson(json));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
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
	 *            a notification can be specific for a user or for everybody (userId
	 *            null)
	 */
	public void addNotification(String message, String url, int type, String userId, boolean admin) {
		addNotification(message, url, type, userId, userId, admin);
	}

	public void addNotification(String message, String url, int type, String userId, String receiver, boolean admin) {
		Notification notif = new Notification();
		notif.setMessage(message);
		notif.setUrl(url);
		notif.setCreationDate(new Date());
		notif.setType(type);
		notif.setUserId(userId);
		notif.setReceiver(receiver);
		notif.setAdmin(admin);
		synchronized (notifications) {
			notifications.add(0, notif);
		}
		cleanList();
	}

	public static void directUserNotification(ContentContext ctx, boolean error, String title, String msg) {
		try {
			String mailBody = XHTMLHelper.createNotificationMail(title, msg, ctx.getGlobalContext().getGlobalTitle());
			String sbj = ctx.getGlobalContext().getGlobalTitle()+" "+title;
			if (error) {
				sbj = "\u26A0 "+sbj;
			} else {
				sbj = "\u2139 "+sbj;
			}
			InternetAddress from = new InternetAddress(ctx.getGlobalContext().getAdministratorEmail());
			InternetAddress to = new InternetAddress(ctx.getCurrentUser().getUserInfo().getEmail());
			MailService.getInstance(new MailConfig(ctx)).sendMail(ctx.getGlobalContext(), from, to, sbj, mailBody, true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
