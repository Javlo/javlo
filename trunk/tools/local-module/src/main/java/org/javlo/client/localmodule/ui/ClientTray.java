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
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.javlo.client.localmodule.service.ActionService;
import org.javlo.client.localmodule.service.I18nService;
import org.javlo.client.localmodule.service.ServiceFactory;
import org.javlo.client.localmodule.service.SynchroControlService;

public class ClientTray {

	private static final Logger logger = Logger.getLogger(ClientTray.class.getName());

	private static ClientTray instance;
	public static ClientTray getInstance() {
		synchronized (ClientTray.class) {
			if (instance == null) {
				instance = new ClientTray();
			}
			return instance;
		}
	}

	private I18nService i18n = I18nService.getInstance();

	private PopupMenu menu;
	private TrayIcon tray;
	private Image passiveIcon;
	private Image activeIcon;
	private boolean trayAdded = false;

	private ConfirmMessage lastMessage;

	private ActionService getAction() {
		return ServiceFactory.getInstance().getAction();
	}

	public void show() {
		if (tray != null) {
			return;
		}
		passiveIcon = new ImageIcon(getClass().getResource("/trayicon_large.png"), "tray icon").getImage();
		activeIcon = new ImageIcon(getClass().getResource("/trayicon_active_large.png"), "tray active icon").getImage();
		tray = new TrayIcon(passiveIcon);
		tray.setToolTip(i18n.get("tray.tooltip"));
		tray.setImageAutoSize(true);

		menu = new PopupMenu();

		MenuItem openFolderItem = new MenuItem(i18n.get("menu.open-folder"));
		MenuItem openWebInterfaceItem = new MenuItem(i18n.get("menu.open-webinterface"));
		MenuItem editMetadataItem = new MenuItem(i18n.get("menu.edit-metadata"));
		MenuItem startSynchroItem = new MenuItem(i18n.get("menu.start-synchro"));
		MenuItem configItem = new MenuItem(i18n.get("menu.open-config"));
		MenuItem aboutItem = new MenuItem(i18n.get("menu.about"));
		MenuItem exitItem = new MenuItem(i18n.get("menu.exit"));

		menu.add(openFolderItem);
		menu.add(openWebInterfaceItem);
		menu.add(editMetadataItem);
		menu.addSeparator();
		menu.add(startSynchroItem);
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
				onTrayClick();
			}
		});
		openFolderItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getAction().openFolder();
			}
		});
		openWebInterfaceItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getAction().openWebInterface();
			}
		});
		editMetadataItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getAction().editMetadata();
			}
		});
		startSynchroItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getAction().startSynchro();
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

	private void onTrayClick() {
		ConfirmMessage message = lastMessage;
		lastMessage = null;
		if (message != null && message.isStillActive()) {
			confirm(message, true);
		} else {
			getAction().openFolder();
		}
	}

	/**
	 * Called by {@link SynchroControlService} to update graphical information. TODO Use listeners?
	 * @param running
	 */
	public void onSyncroStateChange(boolean running) {
		if (trayAdded) {
			tray.setImage(running ? activeIcon : passiveIcon);
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
		displayMessage(caption, text, MessageType.INFO, forceDialog);
	}

	public void displayMessage(String caption, String text, MessageType type, boolean forceDialog) {
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
			tray.displayMessage(caption, text, type);
		}
	}

	public void confirm(ConfirmMessage message, boolean forceDialog) {
		if (forceDialog || tray == null || !trayAdded) {
			message.onChoice(JOptionPane.showConfirmDialog(null, message.getFullMessage(), I18nService.getInstance().get("app.title"), message.getOptionType()));
		} else {
			lastMessage = message;
			displayMessage(I18nService.getInstance().get("app.title"), message.getMessage(), MessageType.WARNING, false);
		}
	}

	public abstract static class ConfirmMessage {
		private final String message;
		private final int optionType;

		public ConfirmMessage(String message, int optionType) {
			this.message = message;
			this.optionType = optionType;
		}

		public String getMessage() {
			return message;
		}

		public int getOptionType() {
			return optionType;
		}

		public String getFullMessage() {
			return getMessage();
		}

		public abstract boolean isStillActive();

		public abstract void onChoice(int result);
	}

}
