package org.javlo.service.syncro;

import java.net.URL;

import org.javlo.config.StaticConfig;
import org.javlo.context.GlobalContext;
import org.javlo.thread.AbstractThread;


@Deprecated
public class SynchronisationThread extends AbstractThread {

	public void initSynchronisationThread(StaticConfig staticConfig, GlobalContext globalContext) {
		setServerURL("" + globalContext.getDMZServerIntra());

		setDataCtxFolder(globalContext.getDataFolder());
		setMailingFolder(staticConfig.getMailingFolder());
		setMailingHistoryFolder(staticConfig.getMailingHistoryFolder());
		setTemplateFolder(staticConfig.getTemplateFolder());
		setMailingTemplateFolder(staticConfig.getMailingTemplateFolder());
		setShareFolder(staticConfig.getShareDataFolder());
		setContext(globalContext.getAllValue());
		setSynchroCode(staticConfig.getSynchroCode());
		setProxyHost(staticConfig.getProxyHost());
		setProxyPort(staticConfig.getProxyPort());
	}

	public void initSynchronisationThreadForMailingHistory(StaticConfig staticConfig, GlobalContext globalContext) {
		setServerURL("" + globalContext.getDMZServerIntra());
		setMailingHistoryFolder(staticConfig.getMailingHistoryFolder());
		setSynchroCode(staticConfig.getSynchroCode());
	}

	public String getServerURL() {
		String outServerURL = getField("server-url");
		return outServerURL;
	}

	public void setServerURL(String serverURL) {
		setField("server-url", serverURL);
		getServerURL();
	}

	public String getDataCtxFolder() {
		return getField("data-ctx");
	}

	public void setDataCtxFolder(String dateContextFolder) {
		setField("data-ctx", dateContextFolder);
	}

	public String getMailingFolder() {
		return getField("mailing-folder");
	}

	public void setMailingFolder(String mailingFolder) {
		setField("mailing-folder", mailingFolder);
	}

	public String getMailingHistoryFolder() {
		return getField("mailing-history-folder");
	}

	public void setMailingHistoryFolder(String mailingFolder) {
		setField("mailing-history-folder", mailingFolder);
	}

	public String getTemplateFolder() {
		return getField("template-folder");
	}

	public void setTemplateFolder(String templateFolder) {
		setField("template-folder", templateFolder);
	}

	public String getMailingTemplateFolder() {
		return getField("mailing-template-folder");
	}

	public void setMailingTemplateFolder(String templateFolder) {
		setField("mailing-template-folder", templateFolder);
	}

	public void setShareFolder(String dateContextFolder) {
		setField("share", dateContextFolder);
	}

	public String getShareFolder() {
		return getField("share");
	}

	public void setContext(String context) {
		setField("context", context);
	}

	public String getContext() {
		return getField("context");
	}

	public void setSynchroCode(String code) {
		setField("synchro-code", code);
	}

	public String getSynchroCode() {
		return getField("synchro-code");
	}

	public void setProxyHost(String host) {
		if (host != null) {
			setField("proxy-host", host);
		}
	}

	public String getProxyHost() {
		return getField("proxy-" + "host");
	}

	public void setProxyPort(int port) {
		setField("proxy-port", "" + port);
	}

	public int getProxyPort() {
		String portStr = getField("proxy-port");
		int port = -1;
		if (portStr != null) {
			try {
				port = Integer.parseInt(portStr);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return port;
	}

	@Override
	public void run() {
		try {
			if (getTemplateFolder() != null) {
				SynchronisationService synchroService = SynchronisationService.getInstanceForTemplate(new URL(getServerURL()), getProxyHost(), getProxyPort(),
						getSynchroCode(), getTemplateFolder());
				synchroService.syncroRessource();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (getMailingTemplateFolder() != null) {
				SynchronisationService synchroService = SynchronisationService.getInstanceForMailingTemplate(new URL(getServerURL()), getProxyHost(),
						getProxyPort(), getSynchroCode(), getMailingTemplateFolder());
				synchroService.syncroRessource();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (getDataCtxFolder() != null) {
				SynchronisationService synchroService = SynchronisationService.getInstance(new URL(getServerURL()), getProxyHost(), getProxyPort(),
						getSynchroCode(), getDataCtxFolder());
				synchroService.syncroRessource();
				synchroService.pushContext(getContext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (getMailingHistoryFolder() != null) {
				SynchronisationService synchroService = SynchronisationService.getInstanceForMailingHistory(new URL(getServerURL()), getProxyHost(),
						getProxyPort(), getSynchroCode(), getMailingHistoryFolder());
				synchroService.syncroRessource();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (getMailingFolder() != null) {
				SynchronisationService synchroService = SynchronisationService.getInstanceForMailing(new URL(getServerURL()), getProxyHost(), getProxyPort(),
						getSynchroCode(), getMailingFolder());
				synchroService.syncroRessource();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (getShareFolder() != null) {
				SynchronisationService synchroService = SynchronisationService.getInstanceForShareFiles(new URL(getServerURL()), getProxyHost(),
						getProxyPort(), getSynchroCode(), getShareFolder());
				synchroService.syncroRessource();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
