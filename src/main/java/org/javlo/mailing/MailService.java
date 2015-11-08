/*
 * Created on 24-mai-2005
 * eena
 */
package org.javlo.mailing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

/**
 * This class, working in a singleton mode, is a utility for sending mail
 * messages.
 * 
 * @author pvandermaesen
 */
public class MailService {

	public static class Attachment {
		private String name;
		private byte[] data;
		
		public Attachment(String name, byte[] data) {
			super();
			this.name = name;
			this.data = data;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}
	}

	public static final String SMTP_HOST_PARAM = "mail.smtp.host";
	public static final String SMTP_PORT_PARAM = "mail.smtp.port";
	public static final String SMTP_USER_PARAM = "mail.smtp.user";
	public static final String SMTP_PASSWORD_PARAM = "mail.smtp.password";

	private static final boolean DEBUG = false;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MailService.class.getName());

	private static final String DEFAULT_SMTP_HOST = "127.0.0.1";
	// private static final String DEFAULT_SMTP_PORT = "25";

	private Properties props;
	//private StaticConfig staticConfig;
	private MailConfig mailConfig;
	private String tempDir;

	private static MailService instance = null;

	private MailService() {
	}

	private void updateInfo(MailConfig mailConfig) {
		Properties finalProps = new Properties();

		if (mailConfig.getSMTPHost() != null) {
			finalProps.put(SMTP_HOST_PARAM, mailConfig.getSMTPHost());
		}
		if (mailConfig.getSMTPPort() != null) {
			finalProps.put(SMTP_PORT_PARAM, mailConfig.getSMTPPort());
		}
		if (mailConfig.getLogin() != null) {
			finalProps.put(SMTP_USER_PARAM, mailConfig.getLogin());
		}
		if (mailConfig.getPassword() != null) {
			finalProps.put(SMTP_PASSWORD_PARAM, mailConfig.getPassword());
		}

		this.tempDir = mailConfig.getTempDir();

		this.props = finalProps;
	}
	
	private void updateInfo(StaticConfig staticConfig, Mailing mailing) {
		Properties finalProps = new Properties();

		if (staticConfig.getSMTPHost() != null) {
			finalProps.put(SMTP_HOST_PARAM, StringHelper.neverEmpty(mailing.getSmtpHost(), staticConfig.getSMTPHost()));
		}
		if (staticConfig.getSMTPPort() != null) {
			finalProps.put(SMTP_PORT_PARAM, StringHelper.neverEmpty(mailing.getSmtpPort(), staticConfig.getSMTPPort()));
		}
		if (staticConfig.getSMTPUser() != null) {
			finalProps.put(SMTP_USER_PARAM, StringHelper.neverEmpty(mailing.getSmtpUser(), staticConfig.getSMTPUser()));
		}
		if (staticConfig.getSMTPPasswordParam() != null) {
			finalProps.put(SMTP_PASSWORD_PARAM, StringHelper.neverEmpty(mailing.getSmtpPassword(),staticConfig.getSMTPPasswordParam()));
		}

		this.tempDir = staticConfig.getTempDir();

		this.props = finalProps;
	}

	/**
	 * This method is kept to be able to use this class outside a Servlet
	 * context
	 * @param mailConfig config for mailing.
	 * @return the MailingManager singleton
	 */
	public static MailService getInstance(MailConfig mailConfig) {
		if (instance == null) {
			instance = new MailService();
		}
		instance.updateInfo(mailConfig);
		instance.mailConfig = mailConfig;
		return instance;
	}	

	private static Properties getMailInfo(MailConfig mailing) {
		Properties finalProps = new Properties();
		
		if (mailing != null) {			
			if (mailing.getSMTPHost() != null ) {
				finalProps.put(MailService.SMTP_HOST_PARAM, mailing.getSMTPHost());
			}
			if (mailing.getSMTPPort() != null) {
				finalProps.put(MailService.SMTP_PORT_PARAM, mailing.getSMTPPort());
			}
			if (mailing.getLogin() != null) {
				finalProps.put(MailService.SMTP_USER_PARAM, mailing.getLogin());
			}
			if (mailing.getPassword() != null) {
				finalProps.put(MailService.SMTP_PASSWORD_PARAM, mailing.getPassword());
			}
		} else {
			finalProps.put(MailService.SMTP_HOST_PARAM, "localhost");
			finalProps.put(MailService.SMTP_PORT_PARAM, 25);
		}

		return finalProps;

	}
	
	public static final Transport getMailTransport(StaticConfig staticConfig, MailConfig mailConfig) throws MessagingException {
		Session mailSession = Session.getDefaultInstance(getMailInfo(mailConfig));
		Transport transport = mailSession.getTransport("smtp");
		transport.connect(mailConfig.getSMTPHost(), mailConfig.getSMTPPortInt(), mailConfig.getLogin(), mailConfig.getPassword());
		return transport;
	}
	
	public static final Transport getMailTransport(MailConfig mailConfig) throws MessagingException {
		Session mailSession = Session.getDefaultInstance(getMailInfo(mailConfig));
		Transport transport = mailSession.getTransport("smtp");
		transport.connect(mailConfig.getSMTPHost(),mailConfig.getSMTPPortInt(), mailConfig.getLogin(), mailConfig.getPassword());
		return transport;
	}

	/**
	 * Send <strong><em>one</em></strong> mail to multiple recipients and
	 * multiple BCC recipients <em>(in one mail)</em>.
	 * 
	 * @param transport
	 *            transport connection, if null transport is create inside the
	 *            method
	 * @param sender
	 *            the "From" field
	 * @param recipients
	 *            the "To" field with multiple addresses.
	 * @param bccRecipients
	 *            the "Bcc" field with multiple addresses.
	 * @param subject
	 *            the Subject of the message
	 * @param content
	 *            the Content of the message
	 * @param isHTML
	 *            flag indicating wether the Content is html (<code>true</code>)
	 *            or text (<code>false</code>)
	 * @throws MessagingException
	 *             Forwarded exception from javax.mail
	 * @throws IllegalArgumentException
	 *             if no recipient provided or no sender
	 */
	public void sendMail(Transport transport, InternetAddress sender, List<InternetAddress> recipients, List<InternetAddress> ccRecipients, List<InternetAddress> bccRecipients, String subject, String content, String txtContent, boolean isHTML, Collection<Attachment> attachments) throws MessagingException {

		String recipientsStr = new LinkedList<InternetAddress>(recipients).toString();

		if (sender == null || recipients == null || recipients.size() == 0) {
			throw new IllegalArgumentException("Sender null (sender: " + sender + ") or no recipient: " + recipients);
		}

		if (props != null) {
			logger.info("Sending mail with subject: " + subject + " to: " + recipients.size() + " recipients: " + recipientsStr + "\n" + "Using smtp: " + props.getProperty(SMTP_HOST_PARAM, DEFAULT_SMTP_HOST) + " / " + props.getProperty(SMTP_USER_PARAM) + " / " + props.getProperty(SMTP_PASSWORD_PARAM));
		}

		Date sendDate = new Date();

		if (!DEBUG) {
			Session mailSession = Session.getDefaultInstance(props);

			MimeMessage msg = new MimeMessage(mailSession);
			msg.setSentDate(sendDate);
			msg.setFrom(sender);
			msg.setRecipients(Message.RecipientType.TO, recipients.toArray(new InternetAddress[recipients.size()]));
			if (ccRecipients != null && ccRecipients.size() > 0) {
				msg.setRecipients(Message.RecipientType.CC, ccRecipients.toArray(new InternetAddress[ccRecipients.size()]));
			}
			if (bccRecipients != null && bccRecipients.size() > 0) {
				msg.setRecipients(Message.RecipientType.BCC, bccRecipients.toArray(new InternetAddress[bccRecipients.size()]));
			}
			msg.setSubject(subject, ContentContext.CHARACTER_ENCODING);

			if (isHTML) {
				MimeBodyPart wrap = new MimeBodyPart();
				MimeMultipart cover = new MimeMultipart("alternative");

				MimeBodyPart bp = new MimeBodyPart();
				if (txtContent == null) {
					txtContent = StringHelper.html2txt(content);
				}
				bp.setText(txtContent, ContentContext.CHARACTER_ENCODING);
				cover.addBodyPart(bp);
				bp = new MimeBodyPart();
				bp.setText(content, ContentContext.CHARACTER_ENCODING, "html");
				cover.addBodyPart(bp);
				wrap.setContent(cover);

				MimeMultipart contentMail = new MimeMultipart("related");

				if (attachments != null) {
					for (Attachment attach : attachments) {
						String id = UUID.randomUUID().toString();
						/*
						 * sb.append("<img src=\"cid:"); sb.append(id);
						 * sb.append("\" alt=\"ATTACHMENT\"/>\n");
						 */

						MimeBodyPart attachment = new MimeBodyPart();

						DataSource fds = new ByteArrayDataSource(attach.getData(), ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(attach.getName())));
						attachment.setDataHandler(new DataHandler(fds));
						attachment.setHeader("Content-ID", "<" + id + ">");
						attachment.setFileName(attach.getName());

						contentMail.addBodyPart(attachment);
					}
				}

				contentMail.addBodyPart(wrap);
				msg.setContent(contentMail);
				msg.setSentDate(new Date());

			} else {
				assert attachments == null : "no attachements in text format.";
				msg.setText(content);
			}

			/*
			 * if (isHTML) { msg.addHeader("Content-Type",
			 * "text/html; charset=\"" + ContentContext.CHARACTER_ENCODING +
			 * "\""); }
			 */

			msg.saveChanges();

			if (transport == null || !transport.isConnected()) {
				transport = getMailTransport(mailConfig);
				try {
					transport.sendMessage(msg, msg.getAllRecipients());
				} finally {
					transport.close();
				}
			} else {
				transport.sendMessage(msg, msg.getAllRecipients());
			}

		} else {
			FileOutputStream out = null;
			try {
				PrintStream w = System.out;
				if (tempDir != null && new File(tempDir).exists()) {
					File mailFile = new File(tempDir, "mail-debug/mail-" + StringHelper.renderFileTime(sendDate) + "-" + StringHelper.stringToFileName(subject) + ".txt");
					mailFile.getParentFile().mkdirs();
					out = new FileOutputStream(mailFile, true);
					w = new PrintStream(mailFile, ContentContext.CHARACTER_ENCODING);
				} else {
					w.println("");
				}

				w.println("FROM:");
				w.println(sender.toString());
				w.print("TO: #");
				w.println(Integer.toString(recipients.size()));
				for (InternetAddress recipient : recipients) {
					w.println(recipient.toString());
				}
				if (ccRecipients != null) {
					w.print("CC: #");
					w.println(Integer.toString(ccRecipients.size()));
					for (InternetAddress ccRecipient : ccRecipients) {
						w.println(ccRecipient.toString());
					}
				}
				if (bccRecipients != null) {
					w.print("BCC: #");
					w.println(Integer.toString(bccRecipients.size()));
					for (InternetAddress bccRecipient : bccRecipients) {
						w.println(bccRecipient.toString());
					}
				}
				w.println("SUBJECT:");
				w.println(subject);
				w.print("IS HTML: ");
				w.println(isHTML);
				w.println("CONTENT:");
				w.print("--BEGIN--");
				w.print(content);
				w.println("--END--");
				w.println("");
				w.flush();
			} catch (IOException e) {
				throw new RuntimeException("Exception when writing debug mail file.", e);
			} finally {
				ResourceHelper.safeClose(out);
			}
		}
		logger.info("Mail sent to: " + recipientsStr);
	}

	/**
	 * Send one mail to one recipient and multiple BCC recipients (in one mail).
	 * 
	 * @param transport
	 *            transport connection, if null transport is create inside the
	 *            method
	 * @param sender
	 *            the "From" field
	 * @param recipient
	 *            the "To" field
	 * @param bccRecipients
	 *            the "Bcc" field with multiple addresses.
	 * @param subject
	 *            the Subject of the message
	 * @param content
	 *            the Content of the message
	 * @param isHTML
	 *            flag indicating wether the Content is html (<code>true</code>)
	 *            or text (<code>false</code>)
	 * @throws MessagingException
	 *             Forwarded exception from javax.mail
	 * @throws IllegalArgumentException
	 *             if no recipient provided or no sender
	 */
	public void sendMail(Transport transport, InternetAddress sender, InternetAddress recipient, List<InternetAddress> ccRecipients, List<InternetAddress> bccRecipients, String subject, String content, boolean isHTML) throws MessagingException {
		List<InternetAddress> recipients = null;
		if (recipient != null) {
			recipients = Arrays.asList(recipient);
		}
		sendMail(transport, sender, recipients, ccRecipients, bccRecipients, subject, content, null, isHTML, null);
	}

	public void sendMail(Transport transport, InternetAddress sender, InternetAddress recipient, InternetAddress ccRecipient, InternetAddress bccRecipient, String subject, String content, boolean isHTML) throws MessagingException {
		List<InternetAddress> ccRecipientsList = new LinkedList<InternetAddress>();
		if (ccRecipient != null) {
			ccRecipientsList.add(ccRecipient);
		}
		List<InternetAddress> bccRecipientsList = new LinkedList<InternetAddress>();
		if (bccRecipient != null) {
			bccRecipientsList.add(bccRecipient);
		}
		sendMail(transport, sender, recipient, ccRecipientsList, bccRecipientsList, subject, content, isHTML);
	}

	public void sendMail(Transport transport, InternetAddress sender, InternetAddress recipient, InternetAddress ccRecipient, InternetAddress bccRecipient, String subject, String content, String contentTxt, boolean isHTML) throws MessagingException {
		List<InternetAddress> ccRecipientsList = new LinkedList<InternetAddress>();
		if (ccRecipient != null) {
			ccRecipientsList.add(ccRecipient);
		}
		List<InternetAddress> bccRecipientsList = new LinkedList<InternetAddress>();
		if (bccRecipient != null) {
			bccRecipientsList.add(bccRecipient);
		}
		List<InternetAddress> recipientsList = new LinkedList<InternetAddress>();
		recipientsList.add(recipient);
		sendMail(transport, sender, recipientsList, ccRecipientsList, bccRecipientsList, subject, content, contentTxt, isHTML, null);
	}

	/**
	 * Send one mail to one recipient.
	 * 
	 * @param transport
	 *            transport connection, if null transport is create inside the
	 *            method
	 * @param sender
	 *            the "From" field
	 * @param recipient
	 *            the "To" field
	 * @param subject
	 *            the Subject of the message
	 * @param content
	 *            the Content of the message
	 * @param isHTML
	 *            flag indicating wether the Content is html (<code>true</code>)
	 *            or text (<code>false</code>)
	 * @throws MessagingException
	 *             Forwarded exception from javax.mail
	 * @throws IllegalArgumentException
	 *             if no recipient provided or no sender
	 */
	public void sendMail(Transport transport, InternetAddress sender, InternetAddress recipient, String subject, String content, boolean isHTML) throws MessagingException {
		sendMail(transport, sender, recipient, (List<InternetAddress>) null, (List<InternetAddress>) null, subject, content, isHTML);
	}

	public void sendMail(InternetAddress sender, InternetAddress recipient, String subject, String content, boolean isHTML) throws MessagingException {
		sendMail(null, sender, recipient, (List<InternetAddress>) null, (List<InternetAddress>) null, subject, content, isHTML);
	}

}
