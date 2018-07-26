package org.javlo.component.web2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.actions.IAction;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;
import org.javlo.service.messaging.ChatService;
import org.javlo.service.messaging.Message;
import org.javlo.service.messaging.MessageBean;
import org.javlo.service.messaging.Room;
import org.javlo.user.User;
import org.javlo.utils.JSONMap;

public class ChatComponent extends AbstractPropertiesComponent implements IAction {
	
	private static final String TITLE = "title";

	private static final String REPLY_DEFAULT = "replyDefault";

	private static final String REPLY_LABEL = "replyLabel";

	private static final String BOT_NAME = "botName";

	private static final String FIRST_SENTENCE = "firstSentence";

	public static final String TYPE = "chat";
	
	private static final List<String> FIELDS = new LinkedList(Arrays.asList(new String[] { TITLE, REPLY_LABEL, REPLY_DEFAULT, FIRST_SENTENCE, BOT_NAME}));
	
	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	@Override
	public String getType() {
		return TYPE;
	}

    @Override
    public void prepareView(ContentContext ctx) throws Exception {    
    	super.prepareView(ctx);
    	ChatService chatService = ChatService.getInstance(ctx.getRequest().getSession());
    }

	@Override
	public String getActionGroupName() {
		return TYPE;
	}
	
	public static String performRooms(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, User user) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (user  == null || !ctx.isAjax()) {
			return "no access to chat rooms";
		}		
		ChatService messagingService = ChatService.getInstance(ctx.getRequest().getSession());
		ctx.getAjaxData().put("rooms", messagingService.getRooms(user.getUserInfo()));
		return null;
	}
	
	@Override
	public String getFontAwesome() {	
		return "comments";
	}
	
	public static String performGetmessages(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, User user) throws Exception {
		Map<String, Object> outMap = new HashMap<String, Object>();
		ChatService chatService = ChatService.getInstance(ctx.getRequest().getSession());
		Room room = chatService.getSessionRoom(ctx);
		ChatComponent comp = (ChatComponent)ComponentHelper.getComponentFromRequest(ctx);
		if (room.getMessages().size() == 0 && !StringHelper.isEmpty(comp.getFieldValue(FIRST_SENTENCE))) {
			room.addMessages(comp.getFieldValue(BOT_NAME), comp.getFieldValue(FIRST_SENTENCE));
		}
		List<MessageBean> messages = new LinkedList<MessageBean>();
		for (Message msg : room.getMessages()) {
			messages.add(new MessageBean(ctx, msg));
		}
		outMap.put("messages", messages);
		ctx.setSpecificJson(JSONMap.JSON.toJson(outMap));
		return null;
	}
	
	public static String performAddmessage(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, User user) throws Exception {
		ChatService chatService = ChatService.getInstance(ctx.getRequest().getSession());
		ChatComponent comp = (ChatComponent)ComponentHelper.getComponentFromRequest(ctx);
		Room room = chatService.getSessionRoom(ctx);
		String msg = StringHelper.removeTag(rs.getParameter("msg"));
		if (!StringHelper.isEmpty(msg)) {
			room.addMessages(ctx.getCurrentUserIdNeverNull(), msg);
			if (!StringHelper.isEmpty(comp.getFieldValue(REPLY_DEFAULT))) {
				Thread.sleep(100);
				room.addMessages(comp.getFieldValue(BOT_NAME), comp.getFieldValue(REPLY_DEFAULT).replace("#LATEST", msg));
			}
		}
		return null;
	}

	
}

