package org.javlo.mailing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.SharedByteArrayInputStream;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

import com.sun.mail.pop3.POP3SSLStore;

public class MailClient {

	private static Logger logger = Logger.getLogger(MailClient.class.getName());

	private Session session = null;
	private Store store = null;
	private String username, password;
	private Folder folder;
	boolean ssl = false;
	private String host = null;;
	private int port = 110;

	public MailClient(String host, int port, boolean ssl, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.ssl = ssl;
	}

	public void connect() throws Exception {
		Properties pop3Props = System.getProperties();
		if (ssl) {
			pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false");
			pop3Props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			pop3Props.setProperty("mail.pop3.port", "" + port);
			pop3Props.setProperty("mail.pop3.socketFactory.port", "" + port);
			session = Session.getInstance(pop3Props, null);
			URLName url = new URLName("pop3", host, port, "", username, password);
			store = new POP3SSLStore(session, url);
			store.connect();
		} else {
			session = Session.getDefaultInstance(pop3Props);
			store = session.getStore("pop3");
			store.connect(host, username, password);
		}
		if (!store.isConnected()) {
			logger.warning("error connect POP3 : " + host + ':' + port + " SSL:" + ssl + " user:" + username + " pwd:" + !StringHelper.isEmpty(password));
		}
	}

	public static String getTextFromMessage(Message message) throws MessagingException, IOException {
		String result = "";
		if (message.isMimeType("text/plain")) {
			result = message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			result = getTextFromMimeMultipart(mimeMultipart);
		} else {
			result = "BAD MESSAGE TYPE : "+message.getContentType();
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("[HEADER]");
		Enumeration headers = message.getAllHeaders();
		while (headers.hasMoreElements()) {
			javax.mail.Header header = (javax.mail.Header) headers.nextElement();
			out.println(header.getName() + "=" + header.getValue());
		}
		out.println("[/HEADER]");
		out.close();
		return new String(outStream.toByteArray()) + result;
	}

	private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);		
		int count = mimeMultipart.getCount();		
		out.println("[PREAMBULE]");
		out.println(mimeMultipart.getPreamble());
		out.println("[/PREAMBULE]");		
		for (int i = 0; i < count; i++) {			
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);			
			out.println("[DESCRIPTION]");
			out.println(bodyPart.getDescription());
			out.println("[/DESCRIPTION]");
			if (!StringHelper.isEmpty(bodyPart.getDisposition())) {
				out.println("[DISPOSITION]");
				out.println(bodyPart.getDisposition());
				out.println("[/DISPOSITION]");
			}
			out.println("[HEADER]");			
			Enumeration headers = bodyPart.getAllHeaders();
			while (headers.hasMoreElements()) {
				javax.mail.Header header = (javax.mail.Header) headers.nextElement();
				out.println(header.getName() + "=" + header.getValue());
			}
			out.println("[/HEADER]");			
			if (bodyPart.isMimeType("text/plain")) {
				out.println("[text/plain]");
				out.println(bodyPart.getContent());
			} else if (bodyPart.isMimeType("text/html")) {
				String html = (String) bodyPart.getContent();
				out.println("[text/html]");
				out.println(html);
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				out.println("[MimeMultipart]");
				out.println(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
			} else {
				out.println("["+bodyPart.getContentType()+"]");
				if (bodyPart.getContent() instanceof MimeMessage) {
					MimeMessage msg = (MimeMessage)bodyPart.getContent();
					ByteArrayOutputStream outStr = new ByteArrayOutputStream();
					ResourceHelper.writeStreamToStream(msg.getInputStream(), outStr);					
					out.println(new String(outStr.toByteArray()));
				} else if (bodyPart.getContent() instanceof SharedByteArrayInputStream) {
					SharedByteArrayInputStream msg = (SharedByteArrayInputStream)bodyPart.getContent();
					ByteArrayOutputStream outStr = new ByteArrayOutputStream();
					ResourceHelper.writeStreamToStream(msg, outStr);
					out.println(new String(outStr.toByteArray()));
				} else {				
					out.println("BAD Content type >> "+bodyPart.getContent());
				}
			}
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	public void openFolder(String folderName) throws Exception {
		folder = store.getFolder(folderName);
		if (folder == null) {
			throw new Exception("Invalid folder");
		}
		folder.open(Folder.READ_WRITE);
	}

	public void close() {
		try {
			if (folder != null) {
				folder.close(true);
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		try {
			if (store != null) {
				store.close();
			}		
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public int getMessageCount() throws Exception {
		return folder.getMessageCount();
	}

	public int getNewMessageCount() throws Exception {
		return folder.getNewMessageCount();
	}

	public void disconnect() throws Exception {
		store.close();
	}

	public Message[] getMessages() throws Exception {
		try {
			return folder.getMessages();
		} catch (IndexOutOfBoundsException iex) {
			System.out.println("Message number out of range");
			return null;
		}
	}

	public Message getMessage(int messageNo) throws Exception {
		try {
			return folder.getMessage(messageNo);
		} catch (IndexOutOfBoundsException iex) {
			System.out.println("Message number out of range");
			return null;
		}
	}
	
	public static String getMailingId(Message msg) throws MessagingException, IOException {
		String text = getTextFromMessage(msg);
		final String ID_PREFIX = "Mailing-ID:[";
		int idIndex = text.indexOf(ID_PREFIX)+ID_PREFIX.length();
		int closeIndex = text.substring(idIndex).indexOf("]")+idIndex;
		if (closeIndex-idIndex >3 && closeIndex-idIndex < 64) {
			return text.substring(idIndex, closeIndex);
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		//MailClient mailClient = new MailClient("ssl0.ovh.net", 110, false, "noreply@javlo.org", "k379k379k379");
		MailClient mailClient = new MailClient("SSL0.OVH.NET", 993, false, "communication@preventionsuicide.be", "LECPS030");
		mailClient.connect();
		mailClient.openFolder("inbox");
		System.out.println("#msg = " + mailClient.getMessageCount());
		for (Message msg : mailClient.getMessages()) {
//			msg.setFlag(Flags.Flag.DELETED, true);
			
			
//			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//			PrintStream out = System.out;
//			out.println("[HEADER]");
//			Enumeration headers = msg.getAllHeaders();
//			while (headers.hasMoreElements()) {
//				javax.mail.Header header = (javax.mail.Header) headers.nextElement();
//				out.println(header.getName() + "=" + header.getValue());
//			}
//			out.println("[/HEADER]");
//			out.close();
			
			System.out.println("------------------------------------");
			System.out.println("subject > "+msg.getSubject()) ;
//			System.out.println(">>>>>>>> "+getMailingId(msg)) ;
			System.out.println("------------------------------------");
		}
		mailClient.close();
	}
}