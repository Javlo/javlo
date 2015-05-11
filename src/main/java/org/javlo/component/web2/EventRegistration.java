package org.javlo.component.web2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

public class EventRegistration extends AbstractVisualComponent implements IAction {
	
	public static final String TYPE = "event-registration";

	public EventRegistration() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getType() {
		return TYPE;
	}

	private final String getUserDataKey(ContentContext ctx, String key) throws Exception {
		return ctx.getCurrentPage().getId()+'-'+key;
	}
	
	private List<String> getConfirmedUser(ContentContext ctx) throws IOException, Exception {
		String confirmedUser = getViewData(ctx).getProperty(getUserDataKey(ctx, "confirmed"));
		return StringHelper.stringToCollection(confirmedUser, ",");
	}
	
	@Override
	public String getEditText(ContentContext ctx, String key) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"row\">");		
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	public static String performConfirm(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		System.out.println("***** EventRegistration.performConfirm : START"); //TODO: remove debug trace
		return null;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}

}
