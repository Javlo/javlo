package org.javlo.message;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nMessage;
import org.javlo.service.NotificationService;
import org.javlo.service.RequestService;

/**
 * contain the list of message.
 * 
 * @author pvandermaesen
 */
public class MessageRepository {

	public static final String KEY = "messages";

	private static final String PARAMETER_NAME = "_msg";

	private final Collection<GenericMessage> messagesWithoutKey = new LinkedList<GenericMessage>();
	private final Map<String, GenericMessage> messagesWithKey = new HashMap<String, GenericMessage>();

	private final HttpServletRequest request;

	private GenericMessage globalMessage = GenericMessage.EMPTY_MESSAGE;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MessageRepository.class.getName());

	private MessageRepository(HttpServletRequest request) {
		this.request = request;
	}

	public static final MessageRepository getInstance(ContentContext inCtx) {
		return getInstance(inCtx.getRequest());
	}

	public static final MessageRepository getInstance(HttpServletRequest request) {
		MessageRepository outRep = (MessageRepository) request.getAttribute(KEY);
		if (outRep == null) {
			outRep = new MessageRepository(request);
			RequestService requestService = RequestService.getInstance(request);			
			
			if (requestService.getParameter(PARAMETER_NAME, null) != null) {
				outRep.setGlobalMessage(new GenericMessage(requestService.getParameter(PARAMETER_NAME, null)));
			}
			request.setAttribute(KEY, outRep);
		}
		return outRep;
	}

	/**
	 * forward the main globalMessage to the next request (for forwarding).
	 * 
	 * @param url
	 *            a url to page
	 * @return the same url with globalmessage as parameter
	 */
	public String forwardMessage(String url) {
		if (globalMessage != null) {
			return URLHelper.addParam(url, PARAMETER_NAME, getRawGlobalMessage());
		} else {
			return url;
		}
	}

	public String getRawGlobalMessage() {
		if (globalMessage != null && globalMessage != GenericMessage.EMPTY_MESSAGE) {
			return StringHelper.toHTMLAttribute(globalMessage.getRawMessage());
		} else {
			return null;
		}
	}

	public String getParameterName() {
		return PARAMETER_NAME;
	}

	public void addMessage(GenericMessage msg) {
		if (msg.getKey() == null) {
			messagesWithoutKey.add(msg);
		} else {
			messagesWithKey.put(msg.getKey(), msg);
		}
	}

	public Collection<GenericMessage> outAllMessages() {
		Collection<GenericMessage> outAllMessages = new LinkedList<GenericMessage>();
		outAllMessages.addAll(messagesWithoutKey);
		outAllMessages.addAll(messagesWithKey.values());
		return outAllMessages;
	}

	public GenericMessage getMessage(String key) {
		GenericMessage outMsg = messagesWithKey.get(key);
		if (outMsg == null) {
			outMsg = GenericMessage.EMPTY_MESSAGE;
		}
		return outMsg;
	}

	public I18nMessage getI18nMessage(String key) throws FileNotFoundException, IOException, ConfigurationException {
		return new I18nMessage(getMessage(key), request);
	}

	public GenericMessage getGlobalMessage() {
		return globalMessage;
	}

	/**
	 * set a new global message. if the type of the current message is more
	 * important or equal the new message is ignored.
	 * 
	 * @param globalMessage
	 *            a global message
	 */
	public void setGlobalMessage(GenericMessage globalMessage) {
		if (this.globalMessage != null) {
			if (this.globalMessage.getType() == 0 || globalMessage.getType() < this.globalMessage.getType()) {
				this.globalMessage = globalMessage;
			}
		} else {
			this.globalMessage = globalMessage;
		}
	}
	
	/**
	 * set a new global message. if the type of the current message is more
	 * important or equal the new message is ignored.
	 * 
	 * @param globalMessage
	 *            a global message
	 */
	public void setGlobalMessageForced(GenericMessage globalMessage) {
		this.globalMessage = globalMessage;
	}

	/**
	 * set a new global message. if the type of the current message is more
	 * important or equal the new message is ignored.
	 * 
	 * @param globalMessage
	 *            a global message
	 */
	public void setGlobalMessageAndNotificationToAll(ContentContext ctx, GenericMessage globalMessage, boolean admin) {
		if (this.globalMessage != null) {
			if (this.globalMessage.getType() == 0 || globalMessage.getType() < this.globalMessage.getType()) {
				this.globalMessage = globalMessage;
			}
		}
		NotificationService notifService = NotificationService.getInstance(GlobalContext.getInstance(request));
		String url = globalMessage.getURL();
		if (url == null) {
			url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE).getContextForAbsoluteURL());
		}
		notifService.addNotification(globalMessage.getMessage(), url, globalMessage.getType(), ctx.getCurrentUserId(), null, admin);
		notifService.notifExternalService(ctx,globalMessage.getMessage(),globalMessage.getType(),url,ctx.getCurrentUserId(),admin);
	}
	
	public void setGlobalMessageAndNotification(ContentContext ctx, GenericMessage globalMessage) {
		setGlobalMessageAndNotification(ctx,globalMessage, false);
	}

	/**
	 * set a new global message. if the type of the current message is more
	 * important or equal the new message is ignored.
	 * 
	 * @param globalMessage
	 *            a global message
	 */
	public void setGlobalMessageAndNotification(ContentContext ctx, GenericMessage globalMessage, boolean admin) {
		if (this.globalMessage != null) {
			if (this.globalMessage.getType() == 0 || globalMessage.getType() < this.globalMessage.getType()) {
				this.globalMessage = globalMessage;
			}
		}
		NotificationService notifService = NotificationService.getInstance(GlobalContext.getInstance(request));
		String url = globalMessage.getURL();
		if (url == null) {
			url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE).getContextForAbsoluteURL());
		}
		notifService.addNotification(globalMessage.getMessage(), url, globalMessage.getType(), ctx.getCurrentUserId(),admin);		
	}

	public void clearGlobalMessage() {
		this.globalMessage = GenericMessage.EMPTY_MESSAGE;
	}

	public boolean haveMessages() {
		return outAllMessages().size() > 0;
	}

	public boolean haveGlobalMessage() {
		return globalMessage != GenericMessage.EMPTY_MESSAGE;
	}

}
