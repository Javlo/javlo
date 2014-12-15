package org.javlo.client.localmodule.ui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.javlo.client.localmodule.model.RemoteNotification;
import org.javlo.client.localmodule.model.ServerConfig;
import org.javlo.client.localmodule.model.ServerStatus;
import org.javlo.client.localmodule.service.ActionService;
import org.javlo.client.localmodule.service.ConfigService;
import org.javlo.client.localmodule.service.I18nService;
import org.javlo.client.localmodule.service.ServerClientService;
import org.javlo.client.localmodule.service.ServiceFactory;

public class ClientTray {

	private static final Logger logger = Logger.getLogger(ClientTray.class.getName());

	private static final long TRAY_MESSAGE_ACTION_VALIDITY = 5000;

	private static final int MAX_NOTIFICATIONS = 5;

	private static ClientTray instance;
	public static ClientTray getInstance() {
		synchronized (ClientTray.class) {
			if (instance == null) {
				instance = new ClientTray();
			}
			return instance;
		}
	}

	public static void onServerStatusChange(ServerConfig server) {
		ServerStatus worseStatus = ServerStatus.UNKNOWN;
		ServiceFactory factory = ServiceFactory.getInstance();
		ConfigService config = factory.getConfig();
		synchronized (config) {
			for (ServerConfig sc : config.getBean().getServers()) {
				ServerClientService scs = factory.getClient(sc);
				if (scs.getStatus().compareTo(worseStatus) >= 1) {
					worseStatus = scs.getStatus();
				}
			}
		}
		getInstance().setWorseServerStatus(worseStatus);
	}

	public void onSyncroStateChange(boolean snchroActive) {
		this.snchroActive = snchroActive;
		refreshIcon();
	}

	private I18nService i18n = I18nService.getInstance();

	private PopupMenu menu;
	private PopupMenu notificationsItem;
	private MenuItem emptyNotification;
	private MenuItem showAllNotifications;
	private TrayIcon tray;
	private Image defaultIcon;
	private Image activeIcon;
	private Image warningIcon;
	private Image errorIcon;
	private boolean trayAdded = false;
	private boolean activeState = false;
	private boolean snchroActive = false;
	private ServerStatus worseServerStatus = ServerStatus.UNKNOWN;

	private TrayMessageAction lastMessageAction;

	private ActionService getAction() {
		return ServiceFactory.getInstance().getAction();
	}

	public void show() {
		if (tray != null) {
			return;
		}
		defaultIcon = new ImageIcon(getClass().getResource("/trayicon_large.png"), "tray icon").getImage();
		activeIcon = new ImageIcon(getClass().getResource("/trayicon_active_large.png"), "tray active icon").getImage();
		warningIcon = new ImageIcon(getClass().getResource("/trayicon_warning_large.png"), "tray warning icon").getImage();
		errorIcon = new ImageIcon(getClass().getResource("/trayicon_error_large.png"), "tray error icon").getImage();
		tray = new TrayIcon(defaultIcon);
		tray.setToolTip(i18n.get("tray.tooltip"));
		tray.setImageAutoSize(true);

		menu = new PopupMenu();

		notificationsItem = new PopupMenu(i18n.get("menu.last-notifications"));
		emptyNotification = new MenuItem(i18n.get("menu.empty-notification"));
		emptyNotification.setEnabled(false);
		notificationsItem.add(emptyNotification);
		showAllNotifications = new MenuItem(i18n.get("menu.show-all-notifications"));

		MenuItem statusItem = new MenuItem(i18n.get("menu.status"));
		MenuItem configItem = new MenuItem(i18n.get("menu.open-config"));
		MenuItem aboutItem = new MenuItem(i18n.get("menu.about"));
		MenuItem exitItem = new MenuItem(i18n.get("menu.exit"));

		menu.add(notificationsItem);
		menu.addSeparator();
		menu.add(statusItem);
		menu.addSeparator();
		menu.add(configItem);
		menu.add(aboutItem);
		menu.add(exitItem);

		tray.setPopupMenu(menu);

		try {
			SystemTray.getSystemTray().add(tray);
			trayAdded = true;
		} catch (AWTException ex) {
			displayErrorMessage(i18n.get("error.error-on-systemtray-op"), ex, true);
		}

		tray.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onTrayClick(e);
			}
		});

		showAllNotifications.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getAction().showNotifications();
			}
		});

		statusItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getAction().showStatus();
			}
		});

		configItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getAction().showConfig();
			}
		});

		aboutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getAction().showAbout();
			}
		});

		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getAction().exit();
			}
		});

		displayInfoMessage(i18n.get("tray.tooltip"), i18n.get("tray.started"), false);
	}

	public boolean isActiveState() {
		return activeState;
	}

	public void setActiveState(boolean runningState) {
		this.activeState = runningState;
		refreshIcon();
	}

	public ServerStatus getWorseServerStatus() {
		return worseServerStatus;
	}

	public void setWorseServerStatus(ServerStatus worseServerStatus) {
		this.worseServerStatus = worseServerStatus;
		refreshIcon();
	}

	private void onTrayClick(ActionEvent e) {
		TrayMessageAction messageAction = lastMessageAction;
		lastMessageAction = null;
		if (messageAction != null && messageAction.isStillActive()) {
			messageAction.getActionListener().actionPerformed(e);
		} else {
			getAction().executeDefaultAction();
		}
	}

	public void refreshNotifications(Collection<RemoteNotification> notifications) {
		notificationsItem.removeAll();
		if (notifications.isEmpty()) {
			notificationsItem.add(emptyNotification);
		} else {
			int startPoint = notifications.size() - MAX_NOTIFICATIONS;
			int i = 0;
			for (RemoteNotification notification : notifications) {
				if (i >= startPoint) {
					MenuItem mi = new MenuItem();
					mi.setLabel(notification.getMenuLabel());
					mi.addActionListener(new NotificationActionListener(notification));
					notificationsItem.add(mi);
				}
				i++;
			}
//			notificationsItem.addSeparator();
//			notificationsItem.add(showAllNotifications);
		}
	}

	private void refreshIcon() {
		if (trayAdded) {
			if (activeState || snchroActive) {
				tray.setImage(activeIcon);
			} else {
				switch (worseServerStatus) {
				case ERROR:
					tray.setImage(errorIcon);
					break;
				case WARNING:
					tray.setImage(warningIcon);
					break;
				case OK:
				case UNKNOWN:
				default:
					tray.setImage(defaultIcon);
					break;
				}
			}
		}
	}

	public void dispose() {
		SystemTray.getSystemTray().remove(tray);
		trayAdded = false;
	}

	public void displayErrorMessage(String text, Throwable ex, boolean forceDialog) {
		String caption = I18nService.getInstance().get("error");
		if (ex != null) {
			text += '\n' + i18n.get("error.details") + ex.getLocalizedMessage();
		}
		if (forceDialog || tray == null || !trayAdded) {
			JOptionPane.showMessageDialog(null, text, caption, JOptionPane.ERROR_MESSAGE);
		} else {
			tray.displayMessage(caption, text, MessageType.ERROR);
		}
	}

	public void displayInfoMessage(String caption, String text, boolean forceDialog) {
		displayMessage(caption, text, MessageType.INFO, forceDialog, null);
	}

	public void displayMessage(String caption, String text, MessageType type, boolean forceDialog, ActionListener actionListener) {
		if (forceDialog || tray == null || !trayAdded) {
			int optType;
			switch (type) {
			case WARNING:
				optType = JOptionPane.WARNING_MESSAGE;
				break;
			case ERROR:
				optType = JOptionPane.ERROR_MESSAGE;
				break;
			case NONE:
				optType = JOptionPane.PLAIN_MESSAGE;
				break;
			case INFO:
			default:
				optType = JOptionPane.INFORMATION_MESSAGE;
			}
			JOptionPane.showMessageDialog(null, text, caption, optType);
		} else {
			if (actionListener != null) {
				lastMessageAction = new TrayMessageAction(actionListener);
			} else {
				lastMessageAction = null;
			}
			logger.info("Display tray message: " + caption + " :: " + text);
			tray.displayMessage(caption, text, type);
		}
	}

//	//TODO not working completely, adapt when needed.
//	private Stack<JWindow> notificationWindows = new Stack<JWindow>();
//
//	public void displayWindow(String message) {
//		Rectangle desktop = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
//
//		synchronized (notificationWindows) {
//			int baseLine;
//			int baseColumn = desktop.x + desktop.width;
//
//			if (!notificationWindows.empty()) {
//				JWindow lastNotif = notificationWindows.peek();
//				baseLine = lastNotif.getY();
//			} else {
//				baseLine = desktop.y + desktop.height;
//			}
//			JLabel lblMessage = new JLabel();
//			lblMessage.setText(message);
//			//lblMessage.setPreferredSize(new Dimension(150, 0));
//			lblMessage.setMaximumSize(new Dimension(150, 500));
//			final JWindow notifWindow = new JWindow();
//			notifWindow.getContentPane().add(lblMessage);
//			notifWindow.pack();
//			notifWindow.setLocation(baseColumn - notifWindow.getWidth(), baseLine - notifWindow.getHeight());
//			notifWindow.setAlwaysOnTop(true);
//			notifWindow.setVisible(true);
//			notificationWindows.push(notifWindow);
//			final Timer timer = new Timer(5000, new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					notifWindow.setVisible(false);
//					notificationWindows.removeElement(notifWindow);
//					notifWindow.dispose();
//					((Timer) e.getSource()).stop();
//				}
//			});
//			notifWindow.addWindowListener(new WindowAdapter() {
//				@Override
//				public void windowClosed(WindowEvent e) {
//					if (timer.isRunning()) {
//						timer.stop();
//					}
//					notificationWindows.removeElement(notifWindow);
//					notifWindow.dispose();
//				}
//			});
//			timer.start();
//		}
//	}

	public static class TrayMessageAction {
		private final ActionListener actionListener;
		private long expires;

		public TrayMessageAction(ActionListener actionListener) {
			this.actionListener = actionListener;
			this.expires = System.currentTimeMillis() + TRAY_MESSAGE_ACTION_VALIDITY;
		}

		public boolean isStillActive() {
			return expires > System.currentTimeMillis();
		}

		public ActionListener getActionListener() {
			return actionListener;
		}

	}

}
