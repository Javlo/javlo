package org.javlo.client.localmodule.service;

import java.awt.Desktop;
import java.net.URI;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.javlo.client.localmodule.ui.ClientTray;
import org.javlo.client.localmodule.ui.ConfigFrame;

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

	private I18nService i18n = I18nService.getInstance();
	private ConfigService config = ConfigService.getInstance();
	private ClientTray tray = ClientTray.getInstance();

	private ActionService() {
	}

	public void openWebInterface() {
		try {
			String remoteLoginId = ServiceFactory.getInstance().getHttpClient().retrieveRemoteLoginId();
			Desktop.getDesktop().browse(new URI(config.getServerURL() + "/edit/?webaction=changeview&view=4&login_id=" + remoteLoginId));
		} catch (Exception ex) {
			tray.displayErrorMessage(i18n.get("error.on-browse"), ex, true);
		}
	}

	public void editMetadata() {
		try {
			String remoteLoginId = ServiceFactory.getInstance().getHttpClient().retrieveRemoteLoginId();
			Desktop.getDesktop().browse(new URI(config.getServerURL() + "/edit/?webaction=changeview&view=4&dir=___nomt___&login_id=" + remoteLoginId));
		} catch (Exception ex) {
			tray.displayErrorMessage(i18n.get("error.on-browse"), ex, true);
		}
	}

	public void showAbout() {
		JOptionPane.showMessageDialog(null, "Javlo Local Folder About...");
	}

	public void showConfig() {
		ConfigFrame.showDialog();
	}

	public void openFolder() {
		try {
			Desktop.getDesktop().open(config.getLocalFolderFile());
		} catch (Exception ex) {
			tray.displayErrorMessage(i18n.get("error.on-open-folder"), ex, true);
		}
	}

	public void startSynchro() {
		SynchroControlService scs = SynchroControlService.getInstance();
		scs.wakeUp();
	}

	public void exit() {
		SynchroControlService scs = SynchroControlService.getInstance();
		scs.stop();
		try {
			while (scs.isStarted()) {
				Thread.sleep(300);
				scs.stop();
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		tray.dispose();
		System.exit(0);
	}

}
