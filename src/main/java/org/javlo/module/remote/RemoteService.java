package org.javlo.module.remote;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.service.NotificationService;
import org.javlo.utils.TimeMap;

public class RemoteService {
	
	private static Logger logger = Logger.getLogger(RemoteService.class.getName());

	private static final String KEY = RemoteService.class.getName();

	public static final String MODULE_NAME = "remote";

	private File folder;
	
	private RemoteThread remoteThread;

	private Map<String, RemoteBean> remotes;
	
	private GlobalContext globalContext;
	
	private String url = null;
	
	private String notificationEmail = null;
	
	private boolean notificationEmailSended = false;
	
	private Map<String, String> sendedNotification = new TimeMap<String, String>(60*10);

	public static RemoteService getInstance(ContentContext ctx) throws Exception {
		GlobalContext globalContext = ctx.getGlobalContext();
		RemoteService service = (RemoteService) globalContext.getAttribute(KEY);
		if (service == null) {
			service = new RemoteService();
			service.folder = new File(URLHelper.mergePath(globalContext.getDataFolder(), "remotes"));
			service.loadRemote();
			service.globalContext = globalContext;
			globalContext.setAttribute(KEY, service);
			service.url = URLHelper.createInterModuleURL(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.EDIT_MODE), "/", "remote");
			service.notificationEmail = globalContext.getAdministratorEmail();
		}
		if (service.remoteThread == null) {
			service.remoteThread = new RemoteThread(service);
			service.remoteThread.start();
		}
		return service;
	}

	private synchronized void loadRemote() throws IOException {
		remotes = new HashMap<String, RemoteBean>();
		File[] files = folder.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".xml"));
		if (files != null) {
			for (File file : files) {
				RemoteBean bean = loadRemote(file);	
				remotes.put(bean.getId(), bean);
			}
		}
	}
	
	public synchronized boolean deleteBean(String id) {
		File remoteFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), id + ".xml"));		
		boolean outDel = remoteFile.delete();
		if (outDel) {
			remotes.remove(id);
		} else {
			logger.warning("error on delete file : " + remoteFile);
		}
		return outDel;
	}

	private synchronized RemoteBean loadRemote(File file) throws IOException {
		if (file == null || !file.exists()) {
			return null;
		}
		String xml = ResourceHelper.loadStringFromFile(file);
		RemoteBean bean = (RemoteBean) ResourceHelper.loadBeanFromXML(xml);
		return bean;
	}

	private synchronized void storeRemote(RemoteBean bean) throws IOException {		
		if (bean.latestHashStore != bean.getStoreHashCode()) {
			bean.latestHashStore = bean.getStoreHashCode();
			File remoteFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), bean.getId() + ".xml"));
			remoteFile.getParentFile().mkdirs();
			String xml = ResourceHelper.storeBeanFromXML(bean);
			ResourceHelper.writeStringToFile(remoteFile, xml, ContentContext.CHARACTER_ENCODING);
		}
	}

	public synchronized void updateRemove(RemoteBean remote) throws IOException {		
		remotes.put(remote.getId(), remote);		
		storeRemote(remote);
	}

	public synchronized RemoteBean getRemote(String id) {
		return remotes.get(id);
	}

	public synchronized Collection<RemoteBean> getRemotes() {
		return remotes.values();
	}

	void sendNotification() {
		boolean errorFound = false;
		for(RemoteBean bean : getRemotes()) {			
			if (!bean.isValid() && !sendedNotification.containsKey(bean.getId())) {
				NotificationService service = NotificationService.getInstance(globalContext);
				String details = bean.getUrl();
				if(bean.getError() != null) {
					details = details + " : " + bean.getError();
				}
				service.addNotification("RC Error (" + details + ")", url, GenericMessage.ERROR, null, null, false);
				errorFound = true;
				sendedNotification.put(bean.getId(), bean.getId());
			}
			try {
				storeRemote(bean);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (errorFound) {
			if (!notificationEmailSended) {
				notificationEmailSended = true;
				if (PatternHelper.MAIL_PATTERN.matcher(notificationEmail).matches()) {
					MailService mailService = MailService.getInstance(new MailConfig(globalContext, globalContext.getStaticConfig(), null));
					InternetAddress admin;
					try {
						admin = new InternetAddress(notificationEmail);
						mailService.sendMail(admin, admin, "remote error on : "+globalContext.getContextKey(), "click here : "+url, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					logger.warning("bad email : "+notificationEmail);
				}
			}
		} else {
			notificationEmailSended = false;
		}
	}
	
	GlobalContext getGlobalContext() {
		return globalContext;
	}

	public String getDefaultSynchroCode() {
		return getGlobalContext().getStaticConfig().getSynchroCode();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		stopService();
	}

	public void stopService() {
		if (remoteThread != null) {
			remoteThread.setStop(true);
			remoteThread = null;
		}
	}

}
