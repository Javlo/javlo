/*
 * Created on 24-mai-2005
 * eena
 */
package org.javlo.mailing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.external.agitos.dkim.Canonicalization;
import org.javlo.external.agitos.dkim.DKIMSigner;
import org.javlo.external.agitos.dkim.SMTPDKIMMessage;
import org.javlo.external.agitos.dkim.SigningAlgorithm;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

/**
 * This class, working in a singleton mode, is a utility for sending mail
 * messages.
 * 
 * @author pvandermaesen
 */
public class MailService {

	public static final String HIDDEN_DIV = "<div style=\"display:none;width:0px;max-height:0px;overflow:hidden;mso-hide:all;height:0;font-size:0;max-height:0;line-height:0;margin:0 auto;\">";
	
	public static final String MAILING_ID_MAIL_KEY = "Mailing-ID";

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
	// private StaticConfig staticConfig;
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

	/**
	 * This method is kept to be able to use this class outside a Servlet
	 * context
	 * 
	 * @param mailConfig
	 *            config for mailing.
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
			if (mailing.getSMTPHost() != null) {
				finalProps.put(MailService.SMTP_HOST_PARAM, mailing.getSMTPHost());
			}
			if (mailing.getSMTPPort() != null) {
				finalProps.put(MailService.SMTP_PORT_PARAM, mailing.getSMTPPort());
			}
			if (mailing.getLogin() != null) {
				finalProps.put(MailService.SMTP_USER_PARAM, mailing.getLogin());
				finalProps.put("mail.smtp.auth", "true");
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

	public static final Session getMailSession(final MailConfig mailConfig) throws MessagingException {
		Session mailSession;
		if (mailConfig == null || !mailConfig.isAuthentification()) {
			mailSession = Session.getDefaultInstance(getMailInfo(mailConfig));
		} else {
			mailSession = Session.getInstance(getMailInfo(mailConfig), new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(mailConfig.getLogin(), mailConfig.getPassword());
				}
			});
		}
		return mailSession;
	}

	public static final Transport getMailTransport(final MailConfig mailConfig) throws MessagingException {
		Session mailSession = getMailSession(mailConfig);
		logger.info("get transport [host:" + mailConfig.getSMTPHost() + " port:" + mailConfig.getSMTPPortInt() + " login:" + mailConfig.getLogin() + " pwd:" + !StringHelper.isEmpty(mailConfig.getPassword()) + ']');
		if (mailConfig.getSMTPPortInt() == 0) { 
			logger.severe("could not send email to port 0.");
			return null;
		} else {
			Transport transport = mailSession.getTransport("smtp");
			
			try {
				transport.connect(mailConfig.getSMTPHost(), mailConfig.getSMTPPortInt(), mailConfig.getLogin(), mailConfig.getPassword());
			} catch (MessagingException e) {
				logger.severe(e.getMessage());
				
				System.out.println("");
				System.out.println("*********************************");
				System.out.println("* ERROR MAIL TRANSPORT  : "+StringHelper.renderTime(new Date()));
				System.out.println("* transport : "+transport);
				System.out.println("* transport connected ? "+transport.isConnected());
				System.out.println("* getSMTPHost : "+mailConfig.getSMTPHost());
				System.out.println("* getSMTPPortInt : "+mailConfig.getSMTPPortInt());
				System.out.println("* getLogin : "+mailConfig.getLogin());
				System.out.println("* getPassword : "+!StringHelper.isEmpty(mailConfig.getPassword()));
				System.out.println("*********************************");
				System.out.println("");
				
				throw e;
			}
			
			return transport;
		}

	}

	public String sendMail(Transport transport, EMail email) throws MessagingException {
		return sendMail(transport, email.getSender(), email.getRecipients(), email.getCcRecipients(), email.getBccRecipients(), email.getSubject(), email.getContent(), email.getTxtContent(), email.isHtml(), email.getAttachments(), email.getUnsubscribeLink(), email.getDkim(), null);
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
	 * @return return a warning message if needed.
	 */
	private String sendMail(Transport transport, InternetAddress sender, List<InternetAddress> recipients, List<InternetAddress> ccRecipients, List<InternetAddress> bccRecipients, String subject, String content, String txtContent, boolean isHTML, Collection<Attachment> attachments, String unsubscribeLink, DKIMBean dkim, String mailId) throws MessagingException {

		String recipientsStr = new LinkedList<InternetAddress>(recipients).toString();
		String warningMessage = null;

		if (sender == null || recipients == null || recipients.size() == 0) {
			throw new IllegalArgumentException("Sender null (sender: " + sender + ") or no recipient: " + recipients);
		}

		Date sendDate = new Date();

		if (!DEBUG) {
			// Session mailSession = Session.getDefaultInstance(props);
			Session mailSession = getMailSession(mailConfig);

			MimeMessage msg = null;
			if (dkim != null) {
				// Create DKIM Signer
				DKIMSigner dkimSigner = null;
				try {
					dkimSigner = new DKIMSigner(dkim.getSigningdomain(), dkim.getSelector(), dkim.getPrivatekey());
					dkimSigner.setIdentity(sender.getAddress());
					dkimSigner.setHeaderCanonicalization(Canonicalization.SIMPLE);
					dkimSigner.setBodyCanonicalization(Canonicalization.RELAXED);
					dkimSigner.setLengthParam(true);
					dkimSigner.setSigningAlgorithm(SigningAlgorithm.SHA1withRSA);
					dkimSigner.setZParam(true);
					msg = new SMTPDKIMMessage(mailSession, dkimSigner);
				} catch (Exception e) {
					e.printStackTrace();
					warningMessage = e.getMessage();

				}
			}
			if (recipients != null) {
				logger.info("Sending mail with subject: " + subject + " to: " + recipients.size() + " recipients: " + recipientsStr + " DKIM?=" + (dkim != null));
			}
			if (msg == null) {
				msg = new MimeMessage(mailSession);
			}
			if (!StringHelper.isEmpty(unsubscribeLink)) {
				msg.setHeader("List-Unsubscribe", unsubscribeLink);
			}
			if (mailId != null) {
				msg.setHeader(MAILING_ID_MAIL_KEY, mailId);
			}
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
				msg.setText(content);
				if (attachments != null) {
					MimeMultipart contentMail = new MimeMultipart("related");
					boolean realAttach = false;
					for (Attachment attach : attachments) {
						if (attach.getData() != null && attach.getData().length > 0) {
							String id = UUID.randomUUID().toString();
							MimeBodyPart attachment = new MimeBodyPart();
							DataSource fds = new ByteArrayDataSource(attach.getData(), ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(attach.getName())));
							attachment.setDataHandler(new DataHandler(fds));
							attachment.setHeader("Content-ID", "<" + id + ">");
							attachment.setFileName(attach.getName());
							contentMail.addBodyPart(attachment);
							realAttach = true;
						}
					}
					if (realAttach) {
						MimeBodyPart wrap = new MimeBodyPart();
						MimeBodyPart bp = new MimeBodyPart();
						bp.setText(content, ContentContext.CHARACTER_ENCODING);
						MimeMultipart cover = new MimeMultipart("alternative");
						cover.addBodyPart(bp);
						wrap.setContent(cover);
						contentMail.addBodyPart(wrap);
						msg.setContent(contentMail);
					}
				}
			}

			/*
			 * if (isHTML) { msg.addHeader("Content-Type",
			 * "text/html; charset=\"" + ContentContext.CHARACTER_ENCODING +
			 * "\""); }
			 */

			msg.saveChanges();

			if (transport == null || !transport.isConnected()) {
				transport = getMailTransport(mailConfig);
				if (transport != null) {
					try {
						transport.sendMessage(msg, msg.getAllRecipients());
					} finally {
						transport.close();
					}
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
		return warningMessage;
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
	public String sendMail(Transport transport, InternetAddress sender, InternetAddress recipient, List<InternetAddress> ccRecipients, List<InternetAddress> bccRecipients, String subject, String content, boolean isHTML, String unsubribeLink, DKIMBean dkinBean) throws MessagingException {
		List<InternetAddress> recipients = null;
		if (recipient != null) {
			recipients = Arrays.asList(recipient);
		}
		return sendMail(transport, sender, recipients, ccRecipients, bccRecipients, subject, content, null, isHTML, null, unsubribeLink, dkinBean, null);
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
		sendMail(transport, sender, recipient, ccRecipientsList, bccRecipientsList, subject, content, isHTML, null, null);
	}

	public void sendMail(Transport transport, InternetAddress sender, InternetAddress recipient, InternetAddress ccRecipient, InternetAddress bccRecipient, String subject, String content, String contentTxt, boolean isHTML, DKIMBean dkimBean) throws MessagingException {
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
		sendMail(transport, sender, recipientsList, ccRecipientsList, bccRecipientsList, subject, content, contentTxt, isHTML, null, null, dkimBean, null);
	}

	public MailConfig getMailConfig() {
		return mailConfig;
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
	public void sendMail(Transport transport, InternetAddress sender, InternetAddress recipient, String subject, String content, boolean isHTML, String unsubribeLink) throws MessagingException {
		sendMail(transport, sender, recipient, (List<InternetAddress>) null, (List<InternetAddress>) null, subject, content, isHTML, unsubribeLink, null);
	}

	public String sendMail(Transport transport, InternetAddress sender, InternetAddress recipient, String subject, String content, boolean isHTML, String unsubribeLink, DKIMBean dkinBean, String mailId) throws MessagingException {
		List<InternetAddress> recipients = null;
		if (recipient != null) {
			recipients = Arrays.asList(recipient);
		}
		return sendMail(transport, sender, recipients, null, null, subject, content, null, isHTML, null, unsubribeLink, dkinBean, mailId);
	}

	public void sendMail(InternetAddress sender, InternetAddress recipient, String subject, String content, boolean isHTML) throws MessagingException {
		sendMail(null, sender, recipient, (List<InternetAddress>) null, (List<InternetAddress>) null, subject, content, isHTML, null, null);
	}
	
	public void sendMail(InternetAddress sender, InternetAddress recipient, String subject, String content, boolean isHTML, DKIMBean dkim) throws MessagingException {
		sendMail(null, sender, recipient, (List<InternetAddress>) null, (List<InternetAddress>) null, subject, content, isHTML, null, dkim);
	}
	
	public void sendMail(GlobalContext globalContext, InternetAddress sender, InternetAddress recipient, String subject, String content, boolean isHTML) throws MessagingException {
		sendMail(null, sender, recipient, (List<InternetAddress>) null, (List<InternetAddress>) null, subject, content, isHTML, globalContext.getUnsubscribeLink(), globalContext.getDKIMBean());
	}

	public static void resetInstance() {
		instance = null;
	}

	public static String messageToText(MimeMultipart part) throws IOException, MessagingException {
		StringBuffer outStr = new StringBuffer();
		final String CRLF = "\r\n";

		for (int i = 0; i < part.getCount(); i++) {
			if (part.getBodyPart(i).getContent() instanceof MimeMultipart) {

				MimeMultipart insideMultipartContent = (MimeMultipart) part.getBodyPart(i).getContent();

				outStr.append("Content-Type: " + part.getContentType() + CRLF + CRLF + CRLF);

				String contentType = part.getContentType();
				if (contentType.contains("\"")) {
					outStr.append("--" + contentType.split("\"")[1] + CRLF);
				}
				Enumeration headers = part.getBodyPart(i).getAllHeaders();
				while (headers.hasMoreElements()) {
					Header header = (Header) headers.nextElement();
					outStr.append(header.getName() + ": " + header.getValue() + CRLF);
				}
				contentType = part.getBodyPart(i).getContentType();
				if (contentType.contains("\"")) {
					outStr.append(CRLF + "--" + contentType.split("\"")[1] + CRLF);
				}
				outStr.append(messageToText(insideMultipartContent) + CRLF);
				contentType = part.getContentType();
				if (contentType.contains("\"")) {
					outStr.append("--" + contentType.split("\"")[1] + "--" + CRLF);
				}

			} else {
				Enumeration headers = part.getBodyPart(i).getAllHeaders();
				while (headers.hasMoreElements()) {
					Header header = (Header) headers.nextElement();
					outStr.append(header.getName() + ": " + header.getValue() + CRLF);
				}
				outStr.append(CRLF + part.getBodyPart(i).getContent());

				String suffix = "";
				if (i == part.getCount() - 1) {
					suffix = "--";
				}

				String contentType = part.getContentType();
				if (contentType.contains("\"")) {
					outStr.append(CRLF + "--" + contentType.split("\"")[1] + suffix + CRLF);
				}

			}
		}
		return outStr.toString();
	}

	public static String messageToDKIMBody(MimeMultipart msg, OutputStream out) throws IOException, MessagingException {
		try {
			if (out == null) {
				out = new ByteArrayOutputStream();
			}
			CRLFOutputStream outCRLF = new CRLFOutputStream(out);
			msg.writeTo(outCRLF);
			if (out instanceof ByteArrayOutputStream) {
				out.close();
				return new String(((ByteArrayOutputStream) out).toByteArray());
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new MessagingException("Exception calculating bodyhash: " + e.getMessage(), e);
		}
	}

	public static String _messageToDKIMBody(MimeMultipart part) throws IOException, MessagingException {
		StringBuffer outStr = new StringBuffer();
		final String CRLF = "\r\n";
		final String SEP = "--boundary";

		for (int i = 0; i < part.getCount(); i++) {

			if (part.getBodyPart(i).getContent() instanceof MimeMultipart) {
				MimeMultipart insideMultipartContent = (MimeMultipart) part.getBodyPart(i).getContent();
				Enumeration headers = part.getBodyPart(i).getAllHeaders();
				/*
				 * while (headers.hasMoreElements()) { Header header =
				 * (Header)headers.nextElement();
				 * outStr.append(header.getName()+": "+header.getValue()+CRLF);
				 * }
				 */
				outStr.append(_messageToDKIMBody(insideMultipartContent) + CRLF);
			} else {
				outStr.append(SEP + CRLF);
				Enumeration headers = part.getBodyPart(i).getAllHeaders();
				while (headers.hasMoreElements()) {
					Header header = (Header) headers.nextElement();
					outStr.append(header.getName() + ": " + header.getValue() + CRLF);
				}
				outStr.append(CRLF + part.getBodyPart(i).getContent() + CRLF);

				if (i == part.getCount() - 1) {
					outStr.append(SEP + "--" + CRLF);
				}

			}
		}
		return outStr.toString();
	}
	
	public static void writeEMLFile(String subject, String body, OutputStream out) throws MessagingException, IOException {		
			Message message = new MimeMessage(Session.getInstance(System.getProperties()));
			message.setSubject(subject);			
			message.setHeader("X-Unsent", "1");
			message.setHeader("Content-Type", "text/html");
			MimeBodyPart wrap = new MimeBodyPart();
			MimeMultipart cover = new MimeMultipart("alternative");
			MimeBodyPart bp = new MimeBodyPart();
			String txtContent = StringHelper.html2txt(body);
			bp.setText(txtContent, ContentContext.CHARACTER_ENCODING);
			cover.addBodyPart(bp);
			bp = new MimeBodyPart();
			bp.setText(body, ContentContext.CHARACTER_ENCODING, "html");
			cover.addBodyPart(bp);
			wrap.setContent(cover);
			MimeMultipart contentMail = new MimeMultipart("related");
			contentMail.addBodyPart(wrap);
			message.setContent(contentMail);
			message.writeTo(out);
			message.setSentDate(new Date());			
	}

	public static void main(String[] args) throws FileNotFoundException, MessagingException, IOException {
		writeEMLFile("test", "<b>coucou</b><br />Je m'appel Patrick.", new FileOutputStream(new File("c:/trans/test_email.eml")));
	}
	
	public static MenuElement getMailTemplateParentPage(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		return content.getNavigation(ctx).searchChildFromName(ctx.getGlobalContext().getStaticConfig().getMailTemplateParent());
	}
	
	public static List<MenuElement> getMailTemplate(ContentContext ctx) throws Exception {		
		MenuElement mailParent = getMailTemplateParentPage(ctx);
		if (mailParent == null) {
			return Collections.EMPTY_LIST;
		} else {
			return mailParent.getChildMenuElements();
		}
	}

}
