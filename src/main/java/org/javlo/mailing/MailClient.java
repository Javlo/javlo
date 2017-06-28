package org.javlo.mailing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import com.sun.mail.pop3.POP3SSLStore;

public class MailClient {

	private Session session = null;
	private Store store = null;
	private String username, password;
	private Folder folder;

	public MailClient() {

	}

	public void setUserPass(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public void connect() throws Exception {

		String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

		Properties pop3Props = new Properties();

		pop3Props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
		pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false");
		pop3Props.setProperty("mail.pop3.port", "995");
		pop3Props.setProperty("mail.pop3.socketFactory.port", "995");

		URLName url = new URLName("pop3", "pop.gmail.com", 995, "", username, password);

		session = Session.getInstance(pop3Props, null);
		store = new POP3SSLStore(session, url);
		store.connect();

	}

	public void openFolder(String folderName) throws Exception {
		//folder = store.getDefaultFolder();
		folder = store.getFolder(folderName);
		if (folder == null) {
			throw new Exception("Invalid folder");
		}
		folder.open(Folder.READ_ONLY);
	}

	public void closeFolder() throws Exception {
		folder.close(false);
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

	public String getMessageText(int messageNo) throws Exception {
		Message m = null;
		try {
			m = folder.getMessage(messageNo);
			return dumpPart(m);
		} catch (IndexOutOfBoundsException iex) {
			System.out.println("Message number out of range");
			return null;
		}
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

	
	public static String dumpPart(Part p) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		if (p instanceof Message) {
			out.println(dumpEnvelope((Message) p));
		}

		String ct = p.getContentType();
		try {
			out.println("CONTENT-TYPE: " + (new ContentType(ct)).toString());
		} catch (ParseException pex) {
			out.println("BAD CONTENT-TYPE: " + ct);
		}

		/*
		 * Using isMimeType to determine the content type avoids fetching the
		 * actual content data until we need it.
		 */
		if (p.isMimeType("text/plain")) {
			out.println("This is plain text");
			out.println("---------------------------");			
		} else {

			// just a separator
			out.println("---------------------------");

		}
		out.close();
		return new String(outStream.toByteArray());

	}

	public static String dumpEnvelope(Message m) throws Exception {
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println(" ");
		Address[] a;
		// FROM
		if ((a = m.getFrom()) != null) {
			for (int j = 0; j < a.length; j++)
				out.println("FROM: " + a[j].toString());
		}

		// TO
		if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
			for (int j = 0; j < a.length; j++) {
				out.println("TO: " + a[j].toString());
			}
		}

		// SUBJECT
		out.println("SUBJECT: " + m.getSubject());		
		// DATE
		Date d = m.getSentDate();
		out.println("SendDate: " + (d != null ? d.toString() : "UNKNOWN"));
		out.close();
		return new String(outStream.toByteArray());
	}

	static String indentStr = "                                               ";
	static int level = 0;

	public static void main(String[] args) throws Exception {
		MailClient mailClient = new MailClient();		
		mailClient.setUserPass("pvandermaesen@noctis.be", "ogqygvyhhzzisedf");
		mailClient.connect();
		mailClient.openFolder("INBOX");
		
//		Message msg = mailClient.getMessage(mailClient.getMessageCount());
		System.out.println("##### MailClient.main : #new msg = "+mailClient.getNewMessageCount()); //TODO: remove debug trace
		Message[] messages = mailClient.getMessages(); 
		System.out.println("##### MailClient.main : #msg = "+messages.length); //TODO: remove debug trace
		/*for (Message msg : messages) {
			System.out.println("subject : " + msg.getSubject());
			System.out.println("date : "+StringHelper.renderTime(msg.getSentDate()));			
		}*/
		mailClient.disconnect();
	}

}
