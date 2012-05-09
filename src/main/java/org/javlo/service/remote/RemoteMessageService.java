package org.javlo.service.remote;

import java.util.Collection;
import java.util.LinkedList;

import org.javlo.context.GlobalContext;
import org.javlo.utils.TimeMap;

public class RemoteMessageService {

	private TimeMap<String, RemoteMessage> messages = new TimeMap<String, RemoteMessage>(60 * 30); // message stay active 30 min

	public static RemoteMessageService getInstance(GlobalContext globalContext) {
		RemoteMessageService outService = (RemoteMessageService) globalContext.getAttribute(RemoteMessageService.class.getName());
		if (outService == null) {
			outService = new RemoteMessageService();
			globalContext.setAttribute(RemoteMessageService.class.getName(), outService);
		}
		return outService;
	}

	public Collection<RemoteMessage> getMessages(String clientId) {
		Collection<RemoteMessage> outMessages = new LinkedList<RemoteMessage>();
		for (RemoteMessage remoteMessage : messages.values()) {
			if (clientId == null || !remoteMessage.isDelivered(clientId)) {
				outMessages.add(remoteMessage);
				remoteMessage.addDeleveredClient(clientId);
			}
		}
		return outMessages;
	}

	public void addMessage(int level, int type, String message) {
		RemoteMessage remoteMessage = new RemoteMessage();
		remoteMessage.setLevel(level);
		remoteMessage.setType(type);
		remoteMessage.setMessage(message);
		messages.put(remoteMessage.getId(), remoteMessage);
	}

	public void addI18nMessage(int type, String message) {
		RemoteMessage remoteMessage = new RemoteMessage();
		remoteMessage.setType(type);
		remoteMessage.setMessageKey(message);
	}

}
