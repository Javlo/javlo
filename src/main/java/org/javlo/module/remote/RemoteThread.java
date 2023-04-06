package org.javlo.module.remote;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;

public class RemoteThread extends Thread {
	
	private static Logger logger = Logger.getLogger(RemoteThread.class.getName());

	private long TIME_BETWEEN_CHECK = 100000; // 100 sec

	private boolean stop = false;
	private long countCheck = 0;
	private int latestDaySendStatus = -1;
	private static int HOUR_SEND_STATUS = 7;
	RemoteService remoteService;
	
	public RemoteThread(RemoteService remoteService) {
		setDaemon(true);
		setName("remote thread : "+remoteService.getGlobalContext().getContextKey());
		this.remoteService = remoteService;
	}
	
	public static String renderRemoteStatus(RemoteService remoteService, AtomicInteger error) {

		String mail = "";
		if (remoteService.getRemotes().size()==0) {
			return "";
		}
		for (RemoteBean bean : remoteService.getRemotes()) {
			URL url;
			try {
				url = new URL(bean.getUrl()+"/status.html");
				try {
					String status =  NetHelper.readPageGet(url);
					if (status.contains("data-error=\"true\"")) {
						error.getAndIncrement();
					} else if (!status.contains("data-error=\"false\"")) {
						mail += "<div style=\"background-color: #cccccc; color: #ffffff; padding: 8px; margin: 15px;\">NOT JAVLO : "+bean.getUrl()+"</div>"; 
					} else {
						mail += status;
					}
					
				} catch (Exception e) {
					try {
						NetHelper.readPageGet(new URL(bean.getUrl()));
						mail += "<div style=\"background-color: #cccccc; color: #ffffff; padding: 8px; margin: 15px;\">NOT JAVLO : "+bean.getUrl()+"</div>"; 
					} catch (Exception e1) {
						mail += "<div style=\"background-color: #dc3545; color: #ffffff; padding: 8px; margin: 15px;\">ERROR : "+bean.getUrl()+" ["+e1.getMessage()+"]</div>";
						error.getAndIncrement();
					}
				}
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
			
		}
		return mail;
	}

	@Override
	public void run() {
		super.run();
		try {
			while (!stop) {
				countCheck++;
				String defaulSynchroCode = remoteService.getDefaultSynchroCode();
				if (StringHelper.isEmpty(defaulSynchroCode)) {
					logger.severe("no synchro core found.");
					stop=true;
					return;
				}
				for (RemoteBean bean : remoteService.getRemotes()) {
					if (bean.getPriority() == RemoteBean.PRIORITY_HIGH) {
						bean.check(defaulSynchroCode);
					} else if (bean.getPriority() == RemoteBean.PRIORITY_MIDDLE && countCheck%100==0) {
						bean.check(defaulSynchroCode);
					} else if (bean.getPriority() == RemoteBean.PRIORITY_LOW && countCheck%1000==0) {
						bean.check(defaulSynchroCode);
					}
					if (!bean.isValid()) {
						logger.fine("error on : "+bean.getUrl()+" msg:"+bean.getError());
					}
				}
				remoteService.sendNotification();
				
				/** send all status **/
				Calendar cal = Calendar.getInstance();
				
				if (cal.get(Calendar.DAY_OF_WEEK) != latestDaySendStatus && cal.get(Calendar.HOUR) == HOUR_SEND_STATUS) {
					latestDaySendStatus = cal.get(Calendar.DAY_OF_WEEK);
					try {
						GlobalContext globalContext = remoteService.getGlobalContext();
						AtomicInteger error = new AtomicInteger(0);
						String mail = renderRemoteStatus(remoteService, error);
						if (!StringHelper.isEmpty(mail)) {
							NetHelper.sendMail(globalContext, new InternetAddress(globalContext.getAdministratorEmail()), new InternetAddress(globalContext.getAdministratorEmail()), null, null, "javlo status (error : "+error+") from :  "+globalContext.getContextKey(), mail, null, true);
						}
					} catch (AddressException e) {
						e.printStackTrace();
					}
				}
				sleep(TIME_BETWEEN_CHECK);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

}
