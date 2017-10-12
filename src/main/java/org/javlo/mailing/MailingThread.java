package org.javlo.mailing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.ConfigurationException;
import javax.servlet.ServletContext;

import org.javlo.config.MailingStaticConfig;
import org.javlo.config.StaticConfig;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.module.mailing.MailingAction;
import org.javlo.service.DataToIDService;

public class MailingThread extends Thread {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MailingThread.class.getName());

	private static final long SLEEP_BETWEEN_MAILING = 2000;

	//ServletContext application;
	
	MailingFactory mailingFactory;
	
	private MailingStaticConfig mailingStaticConfig;
	
	private MailConfig mailConfig;
	
	private DataToIDService dataToIDService;

	public Boolean stop = false;

	public MailingThread(ServletContext inApplication) {
//		application = inApplication;
		mailingStaticConfig = StaticConfig.getInstance(inApplication).getMailingStaticConfig();
		mailingFactory = MailingFactory.getInstance(inApplication);
		mailConfig = new MailConfig(null, StaticConfig.getInstance(inApplication), null);
		dataToIDService = DataToIDService.getInstance(inApplication);
		setName("MailingThread");
	}

	/**
	 * return the list of mailing id stored in this webapps.
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public List<Mailing> getMailingList() throws IOException {
		return mailingFactory.getMailingList();
	}

	public void sendReport(Mailing mailing) throws IOException {

		if (mailing.getNotif() == null) {
			return;
		}

		MailConfig mailConfig = new MailConfig(mailing, this.mailConfig);

		ByteArrayOutputStream mailBody = new ByteArrayOutputStream();
		PrintWriter mailOut = new PrintWriter(mailBody);
		mailOut.println("MAILING REPORT");
		mailOut.println("--------------");
		mailOut.println("");
		mailOut.println("subject : " + mailing.getSubject());
		mailOut.println("from : " + mailing.getFrom());
		mailOut.println("sent? : " + mailing.isSend());
		mailOut.println("config : " + mailConfig);
		mailOut.println("");
		mailOut.println("");
		mailOut.println("receivers detail :");
		mailOut.println("");

		Map<String, String> sent = mailing.loadSent();
		for (InternetAddress recevier : mailing.getReceivers()) {
			String data = sent.get(mailing.getSentKey(recevier));
			if (StringHelper.isEmpty(data)) {
				mailOut.println("" + recevier + " : not sent.");
			} else if (data.toLowerCase().indexOf("unsubscribe") >= 1) {
				mailOut.println("" + recevier + " : not sent - " + data);
			} else {
				mailOut.println("" + recevier + " : sent at " + data);
			}
		}
		mailOut.close();

		MailService mailService = MailService.getInstance(mailConfig);
		String content = new String(mailBody.toByteArray());
		List<InternetAddress> bcc = new LinkedList<InternetAddress>();
		if (mailing.getAdminEmail() != null) {
			try {
				bcc.add(new InternetAddress(mailing.getAdminEmail()));
			} catch (AddressException e) {
				e.printStackTrace();
			}
		}
		try {	
			DKIMBean dkimBean = null;
			if (!StringHelper.isOneEmpty(mailing.getDkimDomain(), mailing.getDkimSelector())) {
				dkimBean = new DKIMBean(mailing.getDkimDomain(), mailing.getDkimSelector(), mailing.getDkimPrivateKeyFile().getAbsolutePath(), null);
			}
			mailService.sendMail(null, mailing.getFrom(), mailing.getNotif(), null, bcc, "report mailing : " + mailing.getSubject(), content, false, null, dkimBean);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		logger.info("report mailing sent to : " + mailing.getNotif() + " for mailing : " + mailing);
	}

	public String extractContent(Mailing mailing) {
		String content = mailing.getContent();
		Collection<Map.Entry<String, String>> datas = mailing.getAllData();
		for (Map.Entry<String, String> entry : datas) {
			content = content.replaceAll(MailingAction.DATA_MAIL_PREFIX + entry.getKey() + MailingAction.DATA_MAIL_SUFFIX, entry.getValue());
		}
		return content;
	}

	public void sendMailing(Mailing mailing) throws IOException, InterruptedException, MessagingException {		
		Transport transport = null;
		try {
			DKIMBean dkimBean = null;
			if (!StringHelper.isOneEmpty(mailing.getDkimDomain(), mailing.getDkimSelector())) {
				dkimBean = new DKIMBean(mailing.getDkimDomain(), mailing.getDkimSelector(), mailing.getDkimPrivateKeyFile().getAbsolutePath(), null);
			}
			MailConfig mailConfig = new MailConfig(mailing,this.mailConfig);
			transport = MailService.getMailTransport(mailConfig);
			mailing.onStartMailing();
			InternetAddress to = mailing.getNextReceiver();
			
			MailService mailingManager = MailService.getInstance(mailConfig);
			
			logger.info("send mailling '" + mailing.getSubject() + "' config:" + mailConfig+ " DKIM ? "+(dkimBean != null));
			int countSending = 0;
			while (to != null) {				
				String data = "mailing=" + mailing.getId() + "&to=" + to;
				mailing.addData("data", dataToIDService.setData(data));
				mailing.addData("roles", StringHelper.collectionToString(mailing.getRoles(), ";"));

				String content = extractContent(mailing);

				if (mailing.getUsers() != null) {
					try {
						content = XHTMLHelper.replaceJSTLUserInfo(content, mailing.getUsers().get(to));
					} catch (Exception e) {
						e.printStackTrace();
						mailing.setErrorMessage(e.getMessage()+" [replaceJSTLUserInfo]");
						try {
							mailing.store(mailingStaticConfig);
						} catch (IOException e1) {
							e1.printStackTrace();
							mailing.setWarningMessage(e.getMessage());
						}
					}
				}

				String error = null;
				try {
					String unsubsribeLink = mailing.getManualUnsubscribeLink();
					if (!StringHelper.isEmpty(unsubsribeLink)) {
						unsubsribeLink = unsubsribeLink.replace("${email}", to.getAddress());
					}					
					mailing.setWarningMessage(mailingManager.sendMail(transport, mailing.getFrom(), to, mailing.getSubject(), content.replace("##MAILING-ID##", mailing.getId()), true, unsubsribeLink, dkimBean, mailing.getId()));
				} catch (Exception ex) {
					error=ex.getMessage();
					ex.printStackTrace();
					mailing.setErrorMessage(ex.getMessage()+" [to="+to+"]");
				}
				mailing.onMailSent(to, error);
				if (countSending%20!=0) {
					Thread.sleep(20);
				} else {
					Thread.sleep(1000);
				}
				to = mailing.getNextReceiver();
			}
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
				transport = null;
			}
			mailing.onEndMailing();
		}
	}

	@Override
	public synchronized void run() {
		logger.info("START MAILING");
		try {
			while (!stop) {
				try {
					try {
						Thread.sleep(SLEEP_BETWEEN_MAILING);
					} catch (InterruptedException e) {
						logger.warning(e.getMessage());
					}
					synchronized (ResourceHelper.SYNCHRO_RESOURCE) {
						List<Mailing> mailing = getMailingList();
						if (mailing.size() > 0) {
							for (Mailing currentMailing : mailing) {
								try {
									boolean itsTime = true;
									if (currentMailing.getSendDate() != null) {
										Date currentDate = new Date();
										if (currentDate.getTime() < currentMailing.getSendDate().getTime()) {
											itsTime = false;
										}
									}
									if (itsTime) {
										if (!currentMailing.isSend() && currentMailing.isValid()) {
											if (!currentMailing.isExistInHistory(mailingStaticConfig, currentMailing.getId())) {
												sendMailing(currentMailing);
												currentMailing.store(mailingStaticConfig);
												sendReport(currentMailing);
											} else {
												logger.severe("MailingThread have try to send a mailing founded in the history : " + currentMailing);
											}
										} else {
											logger.info("mailing not send : " + currentMailing);
										}
										currentMailing.close(mailingStaticConfig);
									}
								} catch (Throwable t) {
									t.printStackTrace();
									currentMailing.setErrorMessage(t.getMessage());
									currentMailing.store(mailingStaticConfig);
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			for (Mailing element : getMailingList()) {
				element.store(mailingStaticConfig);
			}
		} catch (IOException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		} 
		logger.info("STOP MAILING");
		synchronized (stop) {
			stop.notifyAll();
		}
	}
	
	

}
