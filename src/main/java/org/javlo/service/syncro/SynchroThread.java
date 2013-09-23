package org.javlo.service.syncro;

import java.util.logging.Level;

import javax.servlet.ServletContext;

import org.javlo.config.StaticConfig;
import org.javlo.context.GlobalContext;
import org.javlo.thread.AbstractThread;


public class SynchroThread extends AbstractThread {

//	private static final String PREVIOUS_STATE_FILENAME_PREFIX = "/WEB-INF/work/synchro/";
//	private static final String PREVIOUS_STATE_FILENAME_SUFFIX = "-previous-state.properties";

	public void initSynchronisationThread(StaticConfig staticConfig, GlobalContext globalContext, ServletContext application) {
		setLocalName(globalContext.getContextKey());
		setServerURL("" + globalContext.getDMZServerIntra());

		setDataCtxFolder(globalContext.getDataFolder());
		setMailingFolder(staticConfig.getMailingFolder());
		setMailingHistoryFolder(staticConfig.getMailingHistoryFolder());
		setTemplateFolder(staticConfig.getTemplateFolder());
		setShareFolder(staticConfig.getShareDataFolder());

		setContext(globalContext.getAllValue());
		setSynchroCode(staticConfig.getSynchroCode());
		setProxyHost(staticConfig.getProxyHost());
		setProxyPort(staticConfig.getProxyPort());
//		String previousStateFile = application.getRealPath(PREVIOUS_STATE_FILENAME_PREFIX + globalContext.getContextKey() + PREVIOUS_STATE_FILENAME_SUFFIX);
//		setPreviousStateFile(previousStateFile);
	}

	public String getLocalName() {
		return getField("local-name");
	}
	public void setLocalName(String localName) {
		setField("local-name", localName);
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

	public void setShareFolder(String dateContextFolder) {
		setField("share", dateContextFolder);
	}

	public String getShareFolder() {
		return getField("share");
	}

	public String getContext() {
		return getField("context");
	}
	public void setContext(String context) {
		setField("context", context);
	}

	public String getSynchroCode() {
		return getField("synchro-code");
	}
	public void setSynchroCode(String code) {
		setField("synchro-code", code);
	}

	public String getProxyHost() {
		return getField("proxy-" + "host");
	}
	public void setProxyHost(String host) {
		if (host != null) {
			setField("proxy-host", host);
		}
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
	public void setProxyPort(int port) {
		setField("proxy-port", "" + port);
	}

//	public String getPreviousStateFile() {
//		return getField("previous-state-file");
//	}
//	public void setPreviousStateFile(String previousStateFile) {
//		setField("previous-state-file", previousStateFile);
//	}

	@Override
	public void run() {
		try {
			if (getTemplateFolder() != null) {
				ServerSynchroService synchroService = ServerSynchroService.getInstanceForTemplate(getLocalName(), getServerURL(), getProxyHost(), getProxyPort(), getSynchroCode(), getTemplateFolder());
				synchroService.synchronize();
			}
			if (getDataCtxFolder() != null) {
				ServerSynchroService synchroService = ServerSynchroService.getInstance(getLocalName(), getServerURL(), getProxyHost(), getProxyPort(), getSynchroCode(), getDataCtxFolder());
				synchroService.synchronize();
				synchroService.pushContext(getContext());
				synchroService.sendRefresh();
			}
			if (getMailingHistoryFolder() != null) {
				ServerSynchroService synchroService = ServerSynchroService.getInstanceForMailingHistory(getLocalName(), getServerURL(), getProxyHost(), getProxyPort(), getSynchroCode(), getMailingHistoryFolder());
				synchroService.synchronize();
			}
			if (getMailingFolder() != null) {
				ServerSynchroService synchroService = ServerSynchroService.getInstanceForMailing(getLocalName(), getServerURL(), getProxyHost(), getProxyPort(), getSynchroCode(), getMailingFolder());
				synchroService.synchronize();
			}
			if (getShareFolder() != null) {
				ServerSynchroService synchroService = ServerSynchroService.getInstanceForShareFiles(getLocalName(), getServerURL(), getProxyHost(), getProxyPort(), getSynchroCode(), getShareFolder());
				synchroService.synchronize();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in SynchroThread: " + e.getMessage(), e);
		}
	}

}
