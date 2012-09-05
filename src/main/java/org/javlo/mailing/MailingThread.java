package org.javlo.mailing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.config.StaticConfig;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
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
	}

	/**
	 * return the list of mailing id stored in this webapps.
	 *
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public Mailing[] getMailingList() throws ConfigurationException, IOException {
		return MailingFactory.getInstance(application).getMailingList();
	}

	public void sendReport(Mailing mailing) throws IOException {
		ByteArrayOutputStream mailBody = new ByteArrayOutputStream();
		PrintWriter mailOut = new PrintWriter(mailBody);
		mailOut.println("MAILING REPORT");
		mailOut.println("--------------");
		mailOut.println("");
		mailOut.println("subject : " + mailing.getSubject());
		mailOut.println("from : " + mailing.getFrom());
		mailOut.println("sent? : " + mailing.isSend());
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

		MailingManager mailingManager = MailingManager.getInstance(StaticConfig.getInstance(application));
		String content = new String(mailBody.toByteArray());
		InternetAddress bcc = null;
		if (mailing.getAdminEmail() != null) {
			try {
				bcc = new InternetAddress(mailing.getAdminEmail());
			} catch (AddressException e) {
				e.printStackTrace();
			}
		}
		mailingManager.sendMail(mailing.getFrom(), mailing.getNotif(), bcc, "report mailing : " + mailing.getSubject(), content, false);

		logger.info("report mailing sent to : " + mailing.getNotif() + " for mailing : " + mailing);
	}

	public String extractContent(Mailing mailing) {
		String content = mailing.getContent();
		Collection<Map.Entry<String, String>> datas = mailing.getAllData();
		for (Map.Entry<String, String> entry : datas) {
			content = content.replaceAll("##" + entry.getKey() + "##", entry.getValue());
		}
		return content;
	}

	public void sendMailing(Mailing mailing) throws IOException {
		try {
			mailing.onStartMailing();
			InternetAddress to = mailing.getNextReceiver();

			MailingManager mailingManager = MailingManager.getInstance(StaticConfig.getInstance(application));

			while (to != null) {
				DataToIDService dataToID = DataToIDService.getInstance(application);
				String data = "mailing=" + mailing.getId() + "&to=" + to;
				mailing.addData("data", dataToID.setData(data));
				mailing.addData("roles", StringHelper.arrayToString(mailing.getRoles(), ";"));

				String content = extractContent(mailing);

				mailingManager.sendMail(mailing.getFrom(), to, (InternetAddress) null, mailing.getSubject(), content, true);
				mailing.onMailSent(to);
				to = mailing.getNextReceiver();
			}
		} finally {
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
					synchronized (application) {
						synchronized (ResourceHelper.SYNCHRO_RESSOURCE) {
							Mailing[] mailing = getMailingList();
							if (mailing.length > 0) {
								for (int i = 0; i < mailing.length; i++) {
									boolean itsTime = true;
									if (mailing[i].getSendDate() != null) {
										Date currentDate = new Date();
										if (currentDate.getTime() < mailing[i].getSendDate().getTime()) {
											itsTime = false;
										}
									}
									if (itsTime) {
										if (!mailing[i].isSend()&&mailing[i].isValid()) {
											if (!mailing[i].isExistInHistory(application, mailing[i].getId())) {
												sendMailing(mailing[i]);
												mailing[i].store(application);
												sendReport(mailing[i]);
											} else {
												logger.severe("MailingThread have try to send a mailing founded in the history : "+mailing[i]);
											}
										} else {
											logger.info("mailing not send : "+mailing[i]);
										}
										mailing[i].close(application);
									}
								}
							}
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			Mailing[] mailing = getMailingList();
			for (int i = 0; i < mailing.length; i++) {
				mailing[i].store(application);
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
