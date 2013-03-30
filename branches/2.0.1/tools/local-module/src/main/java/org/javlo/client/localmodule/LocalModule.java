package org.javlo.client.localmodule;

import java.awt.SystemTray;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.javlo.client.localmodule.service.ActionService;
import org.javlo.client.localmodule.service.ConfigService;
import org.javlo.client.localmodule.service.I18nService;
import org.javlo.client.localmodule.service.NotificationClientService;
import org.javlo.client.localmodule.ui.ClientTray;

public class LocalModule implements Runnable {

	private static final Logger logger = Logger.getLogger(LocalModule.class.getName());

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.warning("Load of system Look and Feel failed: " + e.getMessage());
		}

		SwingUtilities.invokeLater(new LocalModule());
	}

	@Override
	public void run() {
		ClientTray tray = ClientTray.getInstance();
		I18nService i18n = I18nService.getInstance();
		ConfigService config = ConfigService.getInstance();
		ActionService action = ActionService.getInstance();

		if (!SystemTray.isSupported()) {
			tray.displayErrorMessage(i18n.get("error.tray-not-supported"), null, true);
			action.exit();
			return;
		}

		try {
			config.init();
		} catch (Exception ex) {
			tray.displayErrorMessage(i18n.get("error.config-op"), ex, true);
			action.exit();
			return;
		}

		if (!config.getBean().isValid()) {
			action.showConfig();
			if (!config.getBean().isValid()) {
				action.exit();
				return;
			}
		}

		tray.show();

		NotificationClientService.getInstance().start();
	}

}
