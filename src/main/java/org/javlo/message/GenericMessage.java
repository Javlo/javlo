/*
 * Created on 22-f�vr.-2004
 */
package org.javlo.message;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.helper.XHTMLHelper;

/**
 * @author pvandermaesen object for set a global message.
 */
public class GenericMessage {

	public static final int ERROR = 1;
	public static final int ALERT = 2;
	public static final int INFO = 3;
	public static final int SUCCESS = 5;
	public static final int HELP = 4;

	int type;
	String message = null;
	String key = null;
	String URL = null;

	public static final GenericMessage EMPTY_MESSAGE = new GenericMessage("", null, Integer.MAX_VALUE);

	public GenericMessage(String msg, int newType) {
		this(msg, null, newType);
	}

	public GenericMessage(String msg, int newType, String url) {
		this(msg, null, newType, url);
	}

	public GenericMessage(String msg, String inKey, int newType) {
		this(msg, inKey, newType, null);
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

	/**
	 * @return
	 */
	public String getMessage() {
		if (message == null) {
			return "";
		}
		return XHTMLHelper.escapeXHTML(message); // secure CSS attack
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

	@Override
	public String toString() {
		return getMessage();
	}

}
