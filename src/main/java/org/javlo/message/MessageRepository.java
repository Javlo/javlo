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
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nMessage;
import org.javlo.service.NotificationService;

/**
 * contain the list of message.
 * 
 * @author pvandermaesen
 */
public class MessageRepository {

	public static final String KEY = MessageRepository.class.getName();

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
			request.setAttribute(KEY, outRep);
		}
		return outRep;
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
	 * set a new global message. if the type of the current message is more important or equal the new message is ignored.
	 * 
	 * @param globalMessage
	 *            a global message
	 */
	public void setGlobalMessage(GenericMessage globalMessage) {
		if (this.globalMessage != null) {
			if (this.globalMessage.getType() > globalMessage.getType()) {
				this.globalMessage = globalMessage;
			}
		} else {
			this.globalMessage = globalMessage;
		}
	}

	/**
	 * set a new global message. if the type of the current message is more important or equal the new message is ignored.
	 * 
	 * @param globalMessage
	 *            a global message
	 */
	public void setGlobalMessageAndNotification(ContentContext ctx, GenericMessage globalMessage) {
		if (this.globalMessage != null) {
			if (this.globalMessage.getType() > globalMessage.getType()) {
				this.globalMessage = globalMessage;
			}
		}
		NotificationService notifService = NotificationService.getInstance(GlobalContext.getInstance(request));
		String url = globalMessage.getURL();
		if (url == null) {
			url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE).getContextForAbsoluteURL());
		}
		notifService.addNotification(globalMessage.getMessage(), url, globalMessage.getType(), ctx.getCurrentUserId());
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
