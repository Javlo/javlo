/*
 * Created on 22-fevr.-2004
 */
package org.javlo.message;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.javlo.helper.StringHelper;
import org.owasp.encoder.Encode;

/**
 * @author pvandermaesen object for set a global message.
 */
public class GenericMessage {

	public static final int ERROR = 1;
	public static final int ALERT = 2;
	public static final int INFO = 3;
	public static final int HELP = 4;
	public static final int SUCCESS = 5;

	int type;
	String message = null;
	String cleanMessage = null;
	String key = null;
	String URL = null;
	boolean reverseLinkCreate = false;
	boolean displayed = false;

	public static final GenericMessage EMPTY_MESSAGE = new GenericMessage("", null, 0);

	/***
	 * create generic message from raw
	 * 
	 * @param rawMessage
	 */
	public GenericMessage(String rawMessage) {
		List<String> data = StringHelper.stringToCollection(rawMessage, ",");
		type = Integer.parseInt(data.get(0));
		message = data.get(1);
		if (data.get(2).length() > 0) {
			key = data.get(2);
		}
		if (data.get(3).length() > 0) {
			URL = data.get(3);
		}
	}

	public GenericMessage(String msg, int newType) {
		this(msg, null, newType);
	}

	public GenericMessage(String msg, int newType, String url) {
		this(msg, null, newType, url);
	}

	public GenericMessage(String msg, String inKey, int newType) {
		this(msg, inKey, newType, null);
	}
	
	public GenericMessage(GenericMessage msg) {
		message = msg.message;
		key = msg.key;
		type = msg.type;
		URL = msg.URL;
	}

	public GenericMessage(String msg, String inKey, int newType, String inURL) {
		message = msg;
		key = inKey;
		type = newType;
		URL = inURL;
	}

	public String getKey() {
		return key;
	}
	
	@Deprecated
	public String getText() {
		return getMessage();
	}

	/**
	 * @return
	 */
	public String getMessage() {
		if (message == null) {
			return "";
		}	
		if (cleanMessage != null) {
			return cleanMessage;
		}
		cleanMessage = Encode.forHtmlContent(message);	
		return  cleanMessage; // secure CSS attack
	}
	
	/**
	 * @return
	 */
	public String getMessageDisplay() {		
		setDisplayed(true);
		return getMessage(); // secure CSS attack
	}
	
	public boolean isNeedDisplay() {
		return !isDisplayed() && !StringHelper.isEmpty(message);
	}
	
	public boolean isDisplayed() {
		return displayed;
	}
	
	public void setDisplayed(boolean displayed) {
		this.displayed = displayed;
	}
	
	public String getCleanMessage() {
		return cleanMessage;
	}
	
	public void setCleanMessage(String cleanMessage) {
		this.cleanMessage = cleanMessage;
	}

	/**
	 * @return
	 */
	public int getType() {
		return type;
	}

	public static String getTypeLabel(int type) {
		switch (type) {
		case ERROR:
			return "error";

		case INFO:
			return "info";

		case HELP:
			return "help";

		case ALERT:
			return "alert";

		case SUCCESS:
			return "success";

		default:
			return null;
		}
	}
	
	public static String getBootstrapIcon(int type) {
		switch (type) {
		case ERROR:
			return "bi bi-exclamation-triangle";

		case INFO:
			return "bi bi-info-circle";

		case HELP:
			return "bi bi-question-circle";

		case ALERT:
			return "bi bi-exclamation-circle";

		case SUCCESS:
			return "bi bi-check-lg";

		default:
			return null;
		}
	}

	public String getBootstrapType() {
		switch (type) {
		case ERROR:
			return "danger";

		case INFO:
			return "info";

		case HELP:
			return "info";

		case ALERT:
			return "warning";

		case SUCCESS:
			return "success";

		default:
			return null;
		}

	}

	public String getTypeLabel() {
		return getTypeLabel(type);
	}

	public String getURL() {
		return URL;
	}

	/**
	 * return XHTML code for the current message list
	 * 
	 * @return
	 */
	public String getXhtml() {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.print("<div class=\"message " + getTypeLabel() + "\"><span>");
			out.print(getMessage());
			out.println("</span></div>");
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	public String getRawMessage() {
		return StringHelper.collectionToString(Arrays.asList(new String[] { "" + type, getMessage(), StringHelper.neverNull(key), StringHelper.neverNull(URL) }), ",");
	}

	@Override
	public String toString() {
		return getMessage();
	}

}
