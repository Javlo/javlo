package org.javlo.mailing;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;

import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;

public class POPThread extends Thread {
	
	private static Logger logger = Logger.getLogger(POPThread.class.getName());

	private static final int WAIT_BETWEEN_CHECK_MS = 20*1000; // 10 minutes
	private static final int ACTIVE_DAYS = 10; // 10 days active
	
	
	private boolean stop = false;
	private String host;
	private int port;
	private boolean ssl;
	private String user;
	private String password;
	private Calendar startDate = Calendar.getInstance();
	private Calendar endDate;
	private String contextKey;
	private ServletContext application;
	
	private MailingFactory mailingFactory = null;
	
	public POPThread(GlobalContext globalContext) {
		host = globalContext.getPOPHost();
		port = globalContext.getPOPPort();
		ssl = globalContext.isPOPSsl();
		user = globalContext.getPOPUser();
		password = globalContext.getPOPPassword();
		mailingFactory = MailingFactory.getInstance(globalContext.getServletContext());
		contextKey = globalContext.getContextKey();
		application = globalContext.getServletContext();
		updateEndDate();
	}
	
	@Override
	public void run() {
		while (!stop && Calendar.getInstance().before(endDate)) {
			try {
				Thread.sleep(WAIT_BETWEEN_CHECK_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			MailClient mailClient = new MailClient(host, port, ssl, user, password);
			try {				
				mailClient.connect();
				mailClient.openFolder("inbox");
				for(Message msg : mailClient.getMessages()) {					
					if (msg.getContentType().toLowerCase().contains("delivery-status")) {
						String mailingId = MailClient.getMailingId(msg);
						if (mailingId != null) {
							Mailing mailing = mailingFactory.getMailing(mailingId);
							if (mailing == null) {
								logger.warning("mailing not found : "+mailingId+" ["+contextKey+']');
							} else {
								String text = MailClient.getTextFromMessage(msg);
								boolean foundBadEmail = false;								
								for (InternetAddress add : mailing.getReceivers()) {
									if (text.contains(add.getAddress())) {										
										if (mailing.addErrorReceive(add)) {
											foundBadEmail = true;
										}										
										msg.setFlag(Flags.Flag.DELETED, true);										
									}
								}
								if (foundBadEmail) {
									mailing.store(application);
								}
							}
						}
					}
				}				
			} catch (Exception e) {
				logger.warning("error connect pop [host="+host+" port="+port+" ssl="+ssl+" pwd?="+!StringHelper.isEmpty(password)+"]");
				logger.warning(e.getMessage());
			} finally {
				mailClient.close();
			}
		}
	}	
	
	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		this.stop = stop;
	}
	
	public Date getStartDate() {
		return startDate.getTime();
	}
	
	public void updateEndDate() {
		endDate = Calendar.getInstance();
		endDate.add(Calendar.DAY_OF_YEAR, ACTIVE_DAYS);
	}
	
	
}
