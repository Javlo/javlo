/*
 * Created on 24-mai-2005
 * eena
 */
package org.javlo.mailing;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

/**
 * This class, working in a singleton mode, is a utility for sending mail messages. It will send mails to a bunch of recipients one by one, making it personalized.
 * 
 * @author plemarchand
 */
public class MailingManager {

	private static final String SMTP_HOST_PARAM = "mail.smtp.host";
	private static final String SMTP_PORT_PARAM = "mail.smtp.port";
	private static final String SMTP_USER_PARAM = "mail.smtp.user";
	private static final String SMTP_PASSWORD_PARAM = "mail.smtp.password";

	private static final boolean DEBUG = false;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MailingManager.class.getName());

	private static final String DEFAULT_SMTP_HOST = "127.0.0.1";
	// private static final String DEFAULT_SMTP_PORT = "25";

	private static final String DEFAULT_NOTIF_ADDRESS = "webmaster@eena.org";

	private Properties props;

	private static MailingManager instance = null;

	private MailingManager() {
	}

	private void updateInfo(StaticConfig staticConfig) {
		Properties finalProps = new Properties();

		if (staticConfig.getSMTPHost() != null) {
			finalProps.put(SMTP_HOST_PARAM, staticConfig.getSMTPHost());
		}
		if (staticConfig.getSMTPPort() != null) {
			finalProps.put(SMTP_PORT_PARAM, staticConfig.getSMTPPort());
		}
		if (staticConfig.getSMTPUser() != null) {
			finalProps.put(SMTP_USER_PARAM, staticConfig.getSMTPUser());
		}
		if (staticConfig.getSMTPPasswordParam() != null) {
			finalProps.put(SMTP_PASSWORD_PARAM, staticConfig.getSMTPPasswordParam());
		}

		this.props = finalProps;
	}

	/**
	 * This method is kept to be able to use this class outside a Servlet context
	 * 
	 * @param props
	 *            Properties with necessary parameters to connect to the smtp server in /WEB-INF/config/mailing.properties
	 * 
	 * @return the MailingManager singleton
	 */
	public static MailingManager getInstance(StaticConfig staticConfig) {
		if (instance == null) {
			instance = new MailingManager();
		}
		instance.updateInfo(staticConfig);
		return instance;
	}

	/**
	 * Send a mailing as text
	 */
	public void sendMailing(InternetAddress sender, InternetAddress[] recipients, InternetAddress[] bccRecipients, String subject, String content) {

		sendMailing(sender, recipients, bccRecipients, subject, content, false);
	}

	/**
	 * @param sender
	 *            the "From" field
	 * @param recipients
	 *            all recipients, each one will receive the message with its address in the "To" field
	 * @param optional
	 *            "Bcc" recipients if only 1 recipient in recipients[], address used to send notification on start / end mailing, set to null if none required
	 * @param subject
	 *            the Subject of the message
	 * @param content
	 *            the Content of the message
	 * @param html
	 *            flag indicating wether the Content is html (true) or text (false)
	 * @throws IllegalArgumentException
	 *             if no recipient provided or no sender
	 */
	public String sendMailing(InternetAddress sender, InternetAddress[] recipients, InternetAddress[] bccRecipients, String subject, String content, boolean html) {

		logger.info("using: " + props.getProperty(SMTP_HOST_PARAM, DEFAULT_SMTP_HOST) + " / " + props.getProperty(SMTP_USER_PARAM) + " / " + props.getProperty(SMTP_PASSWORD_PARAM));

		if (sender == null || recipients == null || recipients.length == 0) {
			throw new IllegalArgumentException("sender null (sender: " + sender + ")or no recipient: " + recipients);
		}

		logger.info("sending mail with subject: " + subject + " to: " + recipients.length + " recipients.");

		// BufferedWriter bw = null;
		String errorMessage = null;
		try {
			// bw = new BufferedWriter(new FileWriter("/var/tmp/eenamailingout.txt"));

			final Session session = Session.getDefaultInstance(props);

			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(sender);
			msg.setSubject(subject);

			// text modified each time with replacement
			// msg.setText(content);

			// send start notification mail if more than 1 recipient
			if (recipients.length > 1) {
				MimeMessage startMsg = new MimeMessage(session);

				startMsg.setFrom(sender);
				startMsg.setSubject("Starting mailing to " + recipients.length + " recipients");
				startMsg.setText(subject);
				startMsg.setRecipients(Message.RecipientType.TO, DEFAULT_NOTIF_ADDRESS);
				if (bccRecipients != null) {
					startMsg.setRecipients(Message.RecipientType.BCC, bccRecipients);
				}
				startMsg.setSentDate(new Date());

				final Transport startTransport = session.getTransport("smtp");
				startTransport.connect(props.getProperty(SMTP_HOST_PARAM, DEFAULT_SMTP_HOST), props.getProperty(SMTP_USER_PARAM), props.getProperty(SMTP_PASSWORD_PARAM));

				startTransport.sendMessage(startMsg, startMsg.getAllRecipients());
				startTransport.close();
			} else {
				if (bccRecipients != null) {
					msg.setRecipients(Message.RecipientType.BCC, bccRecipients);
				}
			}

			StringWriter sw = new StringWriter();
			PrintWriter notifContent = new PrintWriter(sw);

			notifContent.println(subject);
			notifContent.println();

			int numRecip = recipients.length;
			for (int i = 0; i < numRecip; i++) {
				InternetAddress[] recipient = { recipients[i] };

				msg.setRecipients(Message.RecipientType.TO, recipient);
				msg.setText(content.replaceAll("\\@mail\\.address\\@", recipients[i].getAddress()));
				msg.setSentDate(new Date());
				if (html) {
					msg.addHeader("Content-Type", "text/html; charset=\"" + ContentContext.CHARACTER_ENCODING + "\"");
				}

				try {
					final Transport transport = session.getTransport("smtp");
					transport.connect(props.getProperty(SMTP_HOST_PARAM, DEFAULT_SMTP_HOST), props.getProperty(SMTP_USER_PARAM), props.getProperty(SMTP_PASSWORD_PARAM));

					transport.sendMessage(msg, msg.getAllRecipients());
					transport.close();

					logger.info("mail " + String.valueOf(i + 1) + " of " + numRecip + " sent to: " + recipients[i].getAddress());

					System.out.println(recipients[i].getAddress());
				} catch (Exception e) {
					e.printStackTrace();
					logger.warning(recipients[i] + " : " + e.getMessage());
					notifContent.print(recipients[i] + " : " + e.getMessage());
					errorMessage = e.getMessage();
				}

				Thread.sleep(20);
			}

			if (recipients.length > 1) {
				MimeMessage endMsg = new MimeMessage(session);
				endMsg.setFrom(sender);
				endMsg.setSubject("Mailing sent");

				notifContent.close();
				endMsg.setText(sw.toString());

				endMsg.setRecipients(Message.RecipientType.TO, DEFAULT_NOTIF_ADDRESS);
				if (bccRecipients != null) {
					endMsg.setRecipients(Message.RecipientType.BCC, bccRecipients);
				}
				endMsg.setSentDate(new Date());

				final Transport endTransport = session.getTransport("smtp");
				endTransport.connect(props.getProperty(SMTP_HOST_PARAM, DEFAULT_SMTP_HOST), props.getProperty(SMTP_USER_PARAM), props.getProperty(SMTP_PASSWORD_PARAM));

				endTransport.sendMessage(endMsg, endMsg.getAllRecipients());
				endTransport.close();
			}
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorMessage = e.getMessage();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			errorMessage = e.getMessage();
			e.printStackTrace();
		} catch (InterruptedException ie) {
			errorMessage = ie.getMessage();
			ie.printStackTrace();
		}
		return errorMessage;
	}

	/**
	 * @param sender
	 *            the "From" field
	 * @param recipient
	 *            address in the "To" field
	 * @param bcc
	 *            address in the "bcc" field (for control)
	 * @param subject
	 *            the Subject of the message
	 * @param content
	 *            the Content of the message
	 * @param isHTML
	 *            flag indicating wether the Content is html (true) or text (false)
	 * @throws IllegalArgumentException
	 *             if no recipient provided or no sender
	 */
	public boolean sendMail(InternetAddress sender, InternetAddress recipient, InternetAddress bcc, String subject, String content, boolean isHTML) {

		// System.out.println("******* SENDMAIL recipient = "+recipient);

		if (!DEBUG) {
			InternetAddress[] bccAddress = null;
			if (bcc != null) {
				bccAddress = new InternetAddress[] { bcc };
			}
			if (sendMailing(sender, new InternetAddress[] { recipient }, bccAddress, subject, content, isHTML) != null) {
				return false;
			} else {
				return true;
			}
		} else {
			/*
			 * System.out.print("["+countMail+"]"); System.out.println(""); System.out.println("***************** SEND MAILING *****************"); System.out.println(""); System.out.println("sender: "+sender); System.out.println("recipient: "+recipient); System.out.println("subject: "+subject); System.out.println(""); System.out.println("** CONTENT : **"); System.out.println(content); System.out.println("");
			 */

			try {
				FileUtils.writeStringToFile(new File("/tmp/" + StringHelper.stringToFileName(subject) + ".html"), content);
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
	}

	/**
	 * send mail with multi bcc
	 * 
	 * @param sender
	 *            the "From" field
	 * @param recipient
	 *            address in the "To" field
	 * @param bccAddress
	 *            addresses in the "bcc" field (for control)
	 * @param subject
	 *            the Subject of the message
	 * @param content
	 *            the Content of the message
	 * @param isHTML
	 *            flag indicating wether the Content is html (true) or text (false)
	 * @throws IllegalArgumentException
	 *             if no recipient provided or no sender
	 */
	public void sendMail(InternetAddress sender, InternetAddress recipient, InternetAddress[] bccAddress, String subject, String content, boolean isHTML) {
		if (!DEBUG) {
			sendMailing(sender, new InternetAddress[] { recipient }, bccAddress, subject, content, isHTML);
		} else {
			System.out.print("[mail send : " + subject + " to:" + recipient + "]");
			/*
			 * System.out.println(""); System.out.println("***************** SEND MAILING *****************"); System.out.println(""); System.out.println("sender: "+sender); System.out.println("recipient: "+recipient); System.out.println("subject: "+subject); System.out.println(""); System.out.println("** CONTENT : **"); System.out.println(content); System.out.println("");
			 */
		}
	}
}
