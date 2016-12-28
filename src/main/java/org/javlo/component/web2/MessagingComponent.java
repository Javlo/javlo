package org.javlo.component.web2;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;
import org.javlo.service.messaging.ViewMessagingService;
import org.javlo.user.User;

public class MessagingComponent extends AbstractVisualComponent implements IAction {
	
	public static final String TYPE = "messaging";

	public MessagingComponent() {}

	@Override
	public String getType() {
		return TYPE;
	}

    @Override
    public void prepareView(ContentContext ctx) throws Exception {    
    	super.prepareView(ctx);
    	ViewMessagingService messagingService = ViewMessagingService.getInstance(ctx.getRequest().getSession());
    }

	@Override
	public String getActionGroupName() {
		return TYPE;
	}
	
	public static String performRooms(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, User user) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		System.out.println("***** MessagingComponent.performRooms : PERFROM ROOMS"); //TODO: remove debug trace
		if (user  == null || !ctx.isAjax()) {
			return "no access to chat rooms";
		}
		ViewMessagingService messagingService = ViewMessagingService.getInstance(ctx.getRequest().getSession());
		ctx.getAjaxData().put("rooms", messagingService.getRooms(user.getUserInfo()));
		return null;
	}
}

