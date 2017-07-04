package org.javlo.mailing;

import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

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
	private int port=110;

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
			pop3Props.setProperty("mail.pop3.port", ""+port);
			pop3Props.setProperty("mail.pop3.socketFactory.port", ""+port);
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
			logger.warning("error connect POP3 : "+host+':'+port+" SSL:"+ssl+" user:"+username+" pwd:"+!StringHelper.isEmpty(password));
		}
	}

	public void openFolder(String folderName) throws Exception {
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

}
