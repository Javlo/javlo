package org.javlo.component.web2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;
import org.javlo.service.event.Event;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.SortUserOnLabel;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

public class EventRegistration extends AbstractPropertiesComponent implements IAction {
	
	private static final String CANCELED = "canceled";

	private static final String CONFIRMED = "confirmed";

	private static final String QUESTION = "question";

	private static final String CONFIRM = "confirm";

	private static final String CANCEL = "cancel";
	
	private static final String CHANGE = "change";
	
	private static final String RESET = "reset";
	
	private static final String PARTICIPANTS = "participants";
	
	private static final String NO_PARTICIPANT = "noParticipant";
	
	private static final String NOTLOGGED = "notlogged";
	
	private static final String MAILING_BUTTON = "mailingButton";
	
	private static final String TOO_LATE = "tooLate";

	public static final String TYPE = "event-registration";
	
	private static List<String> FIELDS = Arrays.asList(new String[] {CANCEL, CONFIRM, QUESTION,CHANGE, RESET, CONFIRMED, CANCELED, NOTLOGGED, MAILING_BUTTON, TOO_LATE, PARTICIPANTS, NO_PARTICIPANT});

	public EventRegistration() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		if (isUserConfirmed(ctx)) {
			ctx.getRequest().setAttribute(CONFIRMED, true);
		} else if (isUserCanceled(ctx)) {
			ctx.getRequest().setAttribute(CANCELED, true);
		}		
		Event event = getPage().getEvent(ctx);		
		ctx.getRequest().setAttribute("event", event);		
		if (event != null && event.getStart() != null && event.getStart().getTime() < (new Date()).getTime()) {
			ctx.getRequest().setAttribute("closeEvent", event);
		}		
		ctx.getRequest().setAttribute("user", getUser(ctx));		
		ctx.getRequest().setAttribute("participants", getParticipants(ctx));
		
	}

	@Override
	public String getType() {
		return TYPE;
	}

	private final String getUserDataKey(ContentContext ctx, String key) throws Exception {
		return key;
	}
	
	private String getUser(ContentContext ctx) {
		if (ctx.getCurrentUser() == null) {
			return null;
		} else {
			return ctx.getCurrentUser().getLogin();
		}
	}
	
	public synchronized List<String> getConfirmedUser(ContentContext ctx) throws IOException, Exception {
		String confirmedUser = getViewData(ctx).getProperty(getUserDataKey(ctx, CONFIRMED));
		return StringHelper.stringToCollection(confirmedUser, ",");
	}
	
	private synchronized void setConfirmedUser(ContentContext ctx, List<String> users) throws IOException, Exception {
		getViewData(ctx).setProperty(getUserDataKey(ctx, CONFIRMED), StringHelper.collectionToString(users,","));
		storeViewData(ctx);
	}
	
	private synchronized void setCanceledUser(ContentContext ctx, List<String> users) throws IOException, Exception {
		getViewData(ctx).setProperty(getUserDataKey(ctx, CANCELED), StringHelper.collectionToString(users,","));
		storeViewData(ctx);
	}
	
	private synchronized void confirmUser(ContentContext ctx) throws Exception {
		String key = getUserDataKey(ctx, CONFIRMED);
		String login = getUser(ctx);
		getViewData(ctx).setProperty(key,getViewData(ctx).getProperty(key)+','+login);
		storeViewData(ctx);	
	}
	
	private synchronized void resetUser(ContentContext ctx) throws Exception {
		List<String> confirmedUser = getConfirmedUser(ctx);
		String login = getUser(ctx);
		if (confirmedUser.contains(login)) {
			confirmedUser.remove(login);
			setConfirmedUser(ctx, confirmedUser);
		}
		List<String> canceledUser = getCanceledUser(ctx);
		if (canceledUser.contains(login)) {
			canceledUser.remove(login);
			setCanceledUser(ctx, canceledUser);
		}		
	}
	
	
	private synchronized void cancelUser(ContentContext ctx) throws Exception {
		String key = getUserDataKey(ctx, CANCELED);
		String login = getUser(ctx);
		getViewData(ctx).setProperty(key,getViewData(ctx).getProperty(key)+','+login);
		storeViewData(ctx);
	}
	
	public synchronized List<String> getCanceledUser(ContentContext ctx) throws IOException, Exception {
		String confirmedUser = getViewData(ctx).getProperty(getUserDataKey(ctx, CANCELED));
		return StringHelper.stringToCollection(confirmedUser, ",");
	}
	
	public synchronized boolean isUserConfirmed(ContentContext ctx) throws IOException, Exception {
		String login = getUser(ctx);
		return getConfirmedUser(ctx).contains(login); 
	}
	
	public synchronized boolean isUserCanceled(ContentContext ctx) throws IOException, Exception {
		String login = getUser(ctx);
		return getCanceledUser(ctx).contains(login); 
	}
	
	public String getUserLink(ContentContext ctx) throws Exception {
		String userLink = URLHelper.createStaticURL(ctx, URLHelper.mergePath("users-list", StringHelper.createFileName(getPage().getTitle(ctx)+".xlsx")));
		userLink = URLHelper.addParam(userLink, "admin", "true");
		userLink = URLHelper.addParam(userLink, "event", getId());
		return userLink;
	}
	
	@Override
	public String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println(super.getEditXHTMLCode(ctx));	
		
		List<String> confirmedUser = getConfirmedUser(ctx);
		List<String> canceledUser = getCanceledUser(ctx);
		
		out.println("<div class=\"row\">");
		out.println("<div class=\"col-md-3\">");
		out.println("<h3>Confirmed ("+confirmedUser.size()+")</h3>");
		out.println("</div><div class=\"col-md-3\">");
		String userLink = getUserLink(ctx);
		out.println("<a class=\"btn btn-primary\" href=\""+userLink+"\">Download Excel</a>");
		out.println("</div><div class=\"col-md-3\">");
		out.println("<h3>Canceled ("+canceledUser.size()+")</h3>");
		out.println("</div><div class=\"col-md-3\">&nbsp;");
		out.println("</div></div>");
		
		out.close();		
		return new String(outStream.toByteArray());
	}
	
	public static String performConfirm(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		EventRegistration comp = (EventRegistration)ComponentHelper.getComponentFromRequest(ctx);
		if (rs.getParameter("reset", null) != null) {			
			comp.resetUser(ctx);
		} else if (rs.getParameter("confirm", null) != null) {
			comp.confirmUser(ctx);
		} else if (rs.getParameter("cancel", null) != null) {
			comp.cancelUser(ctx);
		}
		return null;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}
	
	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		boolean out = super.initContent(ctx);
		if (isEditOnCreate(ctx)) {
			return out;
		}
		setFieldValue(CANCEL, "cancel");
		setFieldValue(CONFIRM, "confirm");
		setFieldValue(CANCELED, "You will not go to this event.");
		setFieldValue(CONFIRMED, "You will go to this event.");
		setFieldValue(QUESTION, "Will you participate?");
		setFieldValue(CHANGE, "change");
		setFieldValue(RESET, "reset");
		setFieldValue(NOTLOGGED, "You must be logged in to confirm you participation.");
		setFieldValue(MAILING_BUTTON, "Click here to register");
		setFieldValue(TOO_LATE, "You can't register to this event.");
		setFieldValue(PARTICIPANTS, "participants");
		setFieldValue(NO_PARTICIPANT, "not yet participants registered.");
		storeProperties();
		setModify();		
		return out;				
	}
	
	@Override
	public boolean isRepeatable() {
		return false;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}
	
	public List<User> getParticipants(ContentContext ctx) throws Exception {
		List<User> outUsers = new LinkedList<User>();		
		Collection<String> allUsersId = getConfirmedUser(ctx);
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		IUserFactory userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		for (String login : allUsersId) {
			User user = userFactory.getUser(login);
			if (user != null) {
				outUsers.add(user);
			} else {
				user = adminUserFactory.getUser(login);
				if (user != null) {
					outUsers.add(user);
				} else {
					logger.warning("user not found : "+login);
				}
			}
		}
		Collections.sort(outUsers, new SortUserOnLabel());
		return outUsers;
	}

}
