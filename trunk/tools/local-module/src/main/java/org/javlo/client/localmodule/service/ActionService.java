package org.javlo.client.localmodule.service;

import java.awt.Desktop;
import java.net.URI;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.javlo.client.localmodule.model.ServerConfig;
import org.javlo.client.localmodule.ui.ClientTray;
import org.javlo.client.localmodule.ui.ConfigFrame;
import org.javlo.client.localmodule.ui.StatusFrame;

public class ActionService {

	private static final Logger logger = Logger.getLogger(ActionService.class.getName());

	private static ActionService instance;
	public static ActionService getInstance() {
		synchronized (ActionService.class) {
			if (instance == null) {
				instance = new ActionService();
			}
			return instance;
		}
	}

	private ServiceFactory factory = ServiceFactory.getInstance();
	private I18nService i18n = I18nService.getInstance();
	private ConfigService config = ConfigService.getInstance();
	private ClientTray tray = ClientTray.getInstance();

	private ActionService() {
	}

	public void executeDefaultAction() {
		showStatus();
	}

	public void showStatus() {
		StatusFrame.showDialog();
	}

	public void openWebInterface() {
//		try {
//			String remoteLoginId = ServiceFactory.getInstance().getHttpClient().retrieveRemoteLoginId();
//			Desktop.getDesktop().browse(new URI(config.getServerURL() + "/edit/?webaction=changeview&view=4&login_id=" + remoteLoginId));
//		} catch (Exception ex) {
//			tray.displayErrorMessage(i18n.get("error.on-browse"), ex, true);
//		}
	}

	public void openUrl(ServerConfig server, String url) {
		ServerClientService client = factory.getClient(server);
		try {
			url = client.tokenifyUrl(url);
			Desktop.getDesktop().browse(new URI(url));
		} catch (Exception ex) {
			tray.displayErrorMessage(i18n.get("error.on-browse"), ex, true);
		}
	}

	public void showAbout() {
		JOptionPane.showMessageDialog(null, "Javlo Local Module About...");
	}

	public void showConfig() {
		ConfigFrame.showDialog();
	}

	public void showNotifications() {
		//NotificationListFrame.showDialog();
	}

	public void exit() {
		NotificationClientService notifClient = NotificationClientService.getInstance();
		notifClient.stop();
		try {
			while (notifClient.isStarted()) {
				Thread.sleep(300);
				notifClient.stop();
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		tray.dispose();
		System.exit(0);
	}

}
