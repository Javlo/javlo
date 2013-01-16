package org.javlo.client.localmodule.service;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import org.javlo.client.localmodule.ui.ClientTray;
import org.javlo.client.localmodule.ui.ClientTray.ConfirmMessage;
import org.javlo.service.syncro.AbstractSynchroContext.SynchroAction;
import org.javlo.service.syncro.AbstractSynchroContext.SynchroSide;
import org.javlo.service.syncro.exception.SynchroFatalException;
import org.javlo.service.syncro.BaseSynchroContext;
import org.javlo.service.syncro.BaseSynchroService;
import org.javlo.service.syncro.FileInfo;
import org.javlo.service.syncro.HttpClientService;

public class ObserverSynchroService extends BaseSynchroService {

	public static ObserverSynchroService createInstance(HttpClientService httpClientService, File baseFolderFile) {
		return new ObserverSynchroService(httpClientService, baseFolderFile);
	}

	private String localName;

	private Object deleteLock = new Object();

	private Set<String> acceptedDeletedPath = new HashSet<String>();
	private Set<String> refusedDeletedPath = new HashSet<String>();
	private Set<String> pendingDeletedPath = new HashSet<String>();

	//private String lastMessageKey;

	protected ObserverSynchroService(HttpClientService httpClientService, File baseFolderFile) {
		super(httpClientService, baseFolderFile);
	}

	@Override
	public String getLocalName() {
		return this.localName;
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public ServiceFactory getFactory() {
		return ServiceFactory.getInstance();
	}

	@Override
	protected void onActionsDefined(BaseSynchroContext context) {
		super.onActionsDefined(context);
		List<String> deletedPath = context.getPathsWithAction(SynchroAction.DELETE_DISTANT);
		if (deletedPath.size() > 0) {
			synchronized (deleteLock) {
				for (String path : deletedPath) {
					FileInfo info = context.getInfo(SynchroSide.DISTANT, path);
					if (info == null || !info.isDirectory()) { //Skip directory delete
						if (acceptedDeletedPath.contains(path)) {
							acceptedDeletedPath.remove(path);
						} else if (refusedDeletedPath.contains(path)) {
							refusedDeletedPath.remove(path);
							context.setAction(path, SynchroAction.COPY_TO_LOCAL);
						} else {
							context.setAction(path, null);
							pendingDeletedPath.add(path);
						}
					}
				}

				if (pendingDeletedPath.size() > 0) {
					ClientTray tray = ServiceFactory.getInstance().getTray();
					tray.confirm(confirmDelete, false);
				}
			}
		}
	}

	private ConfirmMessage confirmDelete = new ConfirmMessage(null, JOptionPane.YES_NO_CANCEL_OPTION) {

		@Override
		public String getMessage() {
			I18nService i18n = ServiceFactory.getInstance().getI18n();
			return i18n.get("question.distant-delete.tray");
		}
		@Override
		public String getFullMessage() {
			I18nService i18n = ServiceFactory.getInstance().getI18n();
			StringWriter buffer = new StringWriter();
			PrintWriter pw = new PrintWriter(buffer);
			pw.print(i18n.get("question.distant-delete.full"));
			int max = 20;
			for (String path : pendingDeletedPath) {
				pw.println();
				pw.print('\t');
				pw.print(path);
				max--;
				if (max <= 0) {
					pw.println();
					pw.print("\t...");
					break;
				}
			}
			return buffer.toString();
		}

		@Override
		public boolean isStillActive() {
			synchronized (deleteLock) {
				return !pendingDeletedPath.isEmpty();
			}
		}

		@Override
		public void onChoice(int result) {
			synchronized (deleteLock) {
				if (result == JOptionPane.YES_OPTION) {
					acceptedDeletedPath.addAll(pendingDeletedPath);
					pendingDeletedPath.clear();
				} else if (result == JOptionPane.NO_OPTION) {
					refusedDeletedPath.addAll(pendingDeletedPath);
					pendingDeletedPath.clear();
				}
			}
		}
	};

	@Override
	protected void onShutdown(BaseSynchroContext context) {
		super.onShutdown(context);
		if (context.isErrorOccured()) {
			displayErrorMessage(context.getReport(), null);
		}
	}

	@Override
	protected void onFatalException(BaseSynchroContext context, SynchroFatalException ex) {
		//super.onFatalException(context, ex);
		Throwable c = ex;
		if (ex.getCause() != null) {
			c = ex.getCause();
		}
		displayErrorMessage(getFactory().getI18n().get("error.synchro.fatal"), c);
		logger.log(Level.SEVERE, c.getClass().getSimpleName() + " occured during synchro process", c);
	}

	@Override
	protected void onUncaughtException(BaseSynchroContext context, Throwable ex) {
		//super.onUncaughtException(context, c);
		displayErrorMessage(getFactory().getI18n().get("error.synchro.fatal"), ex);
		logger.log(Level.SEVERE, ex.getClass().getSimpleName() + " occured during synchro process", ex);
	}

	private void displayErrorMessage(String text, Throwable ex) {
		//String msgKey = text + "|" + (ex == null ? null : ex.getLocalizedMessage());
		//if (lastMessageKey == null || !lastMessageKey.equals(msgKey)) {
		getFactory().getTray().displayErrorMessage(text, ex, false);
		//	lastMessageKey = msgKey;
		//}
	}
}
