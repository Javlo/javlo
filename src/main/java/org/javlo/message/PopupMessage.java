package org.javlo.message;

import java.net.URL;

import javax.servlet.http.HttpSession;

import org.javlo.helper.NetHelper;
import org.javlo.helper.XHTMLHelper;


public class PopupMessage {
	
	private static String KEY = PopupMessage.class.getName();
	
	private String title;
	private String body;
	
	public static PopupMessage getInstance(HttpSession session) {
		PopupMessage instance = (PopupMessage)session.getAttribute(KEY);
		session.removeAttribute(KEY);
		return instance;
	}
	
	public static void setPopupMessage (HttpSession session, String inTitle, String inBody) {
		PopupMessage instance = new PopupMessage();
		instance.title = inTitle;
		instance.body = inBody;
		session.setAttribute(KEY, instance);		
	}
	
	public static void setPopupMessage (HttpSession session, URL url) throws Exception {
		String body = NetHelper.readPage(url, false, null);
		String title = XHTMLHelper.extractTitle(body);
		setPopupMessage(session, title, XHTMLHelper.extractBody(body));
	}

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}

}
