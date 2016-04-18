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
import javax.servlet.ServletContext;

import org.apache.commons.configuration.ConfigurationException;
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

	ServletContext application;

	public Boolean stop = false;

	public MailingThread(ServletContext inApplication) {
		application = inApplication;
		setName("MailingThread");
	}

	/**
	 * return the list of mailing id stored in this webapps.
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public List<Mailing> getMailingList() throws ConfigurationException, IOException {
		return MailingFactory.getInstance(application).getMailingList();
	}

	public void sendReport(Mailing mailing) throws IOException {

		if (mailing.getNotif() == null) {
			return;
		}

		MailConfig mailConfig = new MailConfig(null, StaticConfig.getInstance(application), mailing);

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
			mailService.sendMail(null, mailing.getFrom(), mailing.getNotif(), null, bcc, "report mailing : " + mailing.getSubject(), content, false);
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
		StaticConfig staticConfig = StaticConfig.getInstance(application);
		Transport transport = null;
		try {
			transport = MailService.getMailTransport(new MailConfig(null, staticConfig, mailing));
			mailing.onStartMailing();
			InternetAddress to = mailing.getNextReceiver();

			MailConfig mailConfig = new MailConfig(null, StaticConfig.getInstance(application), mailing);
			MailService mailingManager = MailService.getInstance(mailConfig);

			logger.info("send mailling '" + mailing.getSubject() + "' config:" + mailConfig);

			while (to != null) {
				DataToIDService dataToID = DataToIDService.getInstance(application);
				String data = "mailing=" + mailing.getId() + "&to=" + to;
				mailing.addData("data", dataToID.setData(data));
				mailing.addData("roles", StringHelper.collectionToString(mailing.getRoles(), ";"));

				String content = extractContent(mailing);

				if (mailing.getUsers() != null) {
					try {
						content = XHTMLHelper.replaceJSTLUserInfo(content, mailing.getUsers().get(to));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				try {
					mailingManager.sendMail(transport, mailing.getFrom(), to, mailing.getSubject(), content, true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				mailing.onMailSent(to);
				Thread.sleep(20);
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
											if (!currentMailing.isExistInHistory(application, currentMailing.getId())) {
												sendMailing(currentMailing);
												currentMailing.store(application);
												sendReport(currentMailing);
											} else {
												logger.severe("MailingThread have try to send a mailing founded in the history : " + currentMailing);
											}
										} else {
											logger.info("mailing not send : " + currentMailing);
										}
										currentMailing.close(application);
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
				element.store(application);
			}
		} catch (ConfigurationException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
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
