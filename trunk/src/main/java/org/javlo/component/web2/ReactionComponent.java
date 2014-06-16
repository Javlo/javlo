/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.web2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.fields.Field;
import org.javlo.fields.FieldFactory;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.CaptchaService;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

/**
 * @author pvandermaesen
 */
public class ReactionComponent extends DynamicComponent implements IAction {

	public static String TYPE = "reaction";

	public static class Reaction {

		public static class OrderCreation implements Comparator<Reaction> {

			private int order = 1;

			public OrderCreation(boolean reverse) {
				if (reverse) {
					order = -1;
				}
			}

			@Override
			public int compare(Reaction reaction1, Reaction reaction2) {
				if (!reaction1.isValidReaction() && reaction2.isValidReaction()) {
					return order;
				} else if (reaction1.isValidReaction() && !reaction2.isValidReaction()) {
					return -order;
				} else {
					return reaction1.getDate().compareTo(reaction2.getDate()) * order;
				}
			}

		}

		private String id = StringHelper.getRandomId();
		private String title = "";
		private String authors = "";
		private String text = "";
		private String email = "";
		private boolean validReaction = false;
		private Date date = new Date();
		private String replyOf = null;
		private String url = null;
		private String pageTitle = null;

		public void fromString(String content) {
			String[] contentArray = StringUtils.splitPreserveAllTokens(content, '|');
			if (contentArray.length > 6) {
				id = contentArray[0];
				setTitle(contentArray[1]);
				setAuthors(contentArray[2]);
				setText(contentArray[3]);
				setEmail(contentArray[4]);
				try {
					setDate(StringHelper.parseSortableTime(contentArray[5]));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				setValidReaction(StringHelper.isTrue(contentArray[6]));
				if (contentArray.length > 7) {
					setReplyOf(contentArray[7]);
				}
			} else {
				logger.warning("bad format reaction  found in : " + content);
			}
		}

		public String getAuthors() {
			return authors;
		}

		public Date getDate() {
			return date;
		}

		public String getEmail() {
			return email;
		}

		public String getId() {
			return id;
		}

		public String getReplyOf() {
			return replyOf;
		}

		public String getText() {
			return text;
		}

		public String getTitle() {
			return title;
		}

		public boolean isValidReaction() {
			return validReaction;
		}

		public void setAuthors(String author) {
			this.authors = author;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public void setReplyOf(String replyOf) {
			this.replyOf = replyOf;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setValidReaction(boolean valid) {
			this.validReaction = valid;
		}

		@Override
		public String toString() {
			return getId() + '|' + getTitle() + '|' + getAuthors() + '|' + getText() + '|' + getEmail() + '|' + StringHelper.renderSortableTime(getDate()) + '|' + isValidReaction() + '|' + StringHelper.neverNull(getReplyOf());
		}

		public String getDisplayableDate() {
			return StringHelper.renderSortableTime((getDate()));
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getPageTitle() {
			return pageTitle;
		}

		public void setPageTitle(String pageTitle) {
			this.pageTitle = pageTitle;
		}

	}

	private static final String REACTIONS_PREFIX_KEY = "reactions-";
	private static final String DELETEED_REACTION_PREFIX_KEY = "del-reactions-";

	private static String getAcceptName(Reaction reaction) {
		return "accept-" + reaction.getId();
	}

	private static String getDeleteName(Reaction reaction) {
		return "delete-" + reaction.getId();
	}

	/** * ACTIONS ** */

	public static final String performAdd(HttpServletRequest request, HttpServletResponse response) throws Exception {

		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentService content = ContentService.getInstance(request);
		IContentVisualComponent comp = content.getComponent(ctx, request.getParameter("comp"));
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		if (requestService.getParameter("fdata", "").length() > 0) {
			String msg = "stay special field empty.";
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
			return "";
		}

		if ((comp != null) && (comp instanceof ReactionComponent)) {
			ReactionComponent reactionComp = (ReactionComponent) comp;
			String reactionId = null;
			if (reactionComp.isReplyAllowed(ctx)) {
				reactionId = requestService.getParameter("reactionId", "").trim();
			}
			List<Field> fields = reactionComp.getViewFields(ctx, reactionId);
			Reaction reaction = new Reaction();
			boolean validReaction = false;

			if (reactionComp.isCaptcha(ctx)) {
				String captcha = requestService.getParameter("captcha", "");
				if (!CaptchaService.getInstance(request.getSession()).getCurrentCaptchaCode().equals(captcha)) {
					I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
					return i18nAccess.getViewText("message.error.bad-captcha");
				}
			}

			reaction.setReplyOf(reactionId);

			for (Field field : fields) {
				field.process(request);
				if (field.getName().equals("title")) {
					if ((field.getValue() != null) && (field.getValue().trim().length() > 0)) {
						reaction.setTitle(field.getValue());
						validReaction = true;
					} else {
						logger.warning("no title in reaction");
						validReaction = false;
					}
				} else if (field.getName().equals("nickname")) {
					if ((field.getValue() != null) && (field.getValue().trim().length() > 0)) {
						reaction.setAuthors(field.getValue());
						validReaction = true;
					} else {
						logger.warning("no nickname in reaction");
						validReaction = false;
					}
				} else if (field.getName().equals("email")) {
					if ((field.getValue() != null) && (field.getValue().trim().length() > 0)) {
						reaction.setEmail(field.getValue());
						validReaction = true;
					} else {
						logger.warning("no email in reaction");
						validReaction = false;
					}
				} else if (field.getName().equals("text")) {
					if ((field.getValue() != null) && (field.getValue().trim().length() > 0)) {
						reaction.setText(field.getValue());
						validReaction = true;
					} else {
						logger.warning("no text in reaction");
						validReaction = false;
					}
				}
			}

			User currentUser = getCurrentUser(ctx);
			if (currentUser != null) {
				reaction.setAuthors(currentUser.getLogin());
			}

			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
			if (validReaction) {
				if (!reactionComp.addReaction(ctx, reaction)) {
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("reaction.added"), GenericMessage.INFO));
				} else {
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("reaction.added-novalidation"), GenericMessage.INFO));
				}
				for (Field field : fields) {
					field.setValue(null);
				}
			} else {
				logger.warning("unvalid reaction.");
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("reaction.unvalid"), GenericMessage.ERROR));
			}
		}

		return null;
	}

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ReactionComponent.class.getName());

	private boolean addReaction(ContentContext ctx, Reaction reaction) throws IOException {

		String email = getEmail(ctx);
		if (email != null && PatternHelper.MAIL_PATTERN.matcher(email).matches()) {
			ContentContext editCtx = new ContentContext(ctx);
			editCtx.setRenderMode(ContentContext.EDIT_MODE);
			String currentURL = URLHelper.createURL(editCtx);
			StringWriter writer = new StringWriter();
			PrintWriter out = new PrintWriter(writer);
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			out.println("new comment add on context : " + globalContext.getContextKey());
			out.println("");
			out.println("title : " + reaction.getTitle());
			out.println("author : " + reaction.getAuthors());
			out.println("text :");
			out.println(reaction.getText());
			out.println("");
			out.println("validation url : " + currentURL);

			String sender = globalContext.getAdministratorEmail();
			if (reaction.getEmail() != null) {
				sender = reaction.getEmail();
			}
			InternetAddress from;

			try {
				from = new InternetAddress(sender);
				InternetAddress to = new InternetAddress(getEmail(ctx));
				MailService.getInstance(StaticConfig.getInstance(ctx.getRequest().getSession())).sendMail(from, to, "comment validation on " + globalContext.getContextKey(), writer.toString(), false);
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}

		} else {
			reaction.setValidReaction(true);
		}

		Collection<Reaction> reactions = getReactions(ctx);
		synchronized (reactions) {
			reactions.add(reaction);
			setReactions(ctx, reactions);
		}
		return reaction.isValidReaction();
	}

	protected String getReactionPrefix(ContentContext ctx) {
		if (isRepeat()) {
			try {
				return REACTIONS_PREFIX_KEY + ctx.getCurrentPage().getId() + '-';
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return REACTIONS_PREFIX_KEY;
		}
	}

	protected static String readPageIdFromKey(String key) {
		if (key == null || key.indexOf('-') < 0) {
			return null;
		}
		key = key.substring(key.indexOf('-') + 1);
		if (key.indexOf('-') < 0) {
			return null;
		}
		return key.substring(0, key.indexOf('-'));
	}

	protected String getDelReactionPrefix(ContentContext ctx) {
		return DELETEED_REACTION_PREFIX_KEY;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println("<div class=\"dynamic-component\">");
		Collection<Field> fields = getFields(ctx);
		for (Field field : fields) {
			if (field != null) {
				out.println(field.getEditXHTMLCode(ctx));
			} else {
				out.println("<div class=\"line\">");
				out.println("field not found : " + field.getType());
				out.println("</div>");
			}
		}

		Collection<Reaction> reactions = getReactions(ctx);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());
		out.println("<fieldset>");
		out.print("<legend>");
		out.println(i18nAccess.getText("global.comment"));
		out.println("</legend>");
		out.println("<div class=\"reactions\">");
		renderEditReactions(out, null, reactions, ctx, i18nAccess);
		out.println("</div></fieldset>");

		out.println("</div>");
		out.close();
		return writer.toString();
	}

	private void renderEditReactions(PrintWriter out, String parentReactionId, Collection<Reaction> reactions, ContentContext ctx, I18nAccess i18nAccess) {
		parentReactionId = StringHelper.neverNull(parentReactionId);
		boolean first = true;
		for (Reaction reaction : reactions) {
			if (!parentReactionId.equals(StringHelper.neverNull(reaction.getReplyOf()))) {
				continue;
			}
			if (first) {
				out.println("<ul>");
			}
			out.println("<li id=\"reaction-" + reaction.getId() + "\" class=\"reaction" + (first ? " first" : "") + "\">");
			out.println("<div class=\"line\">");
			out.print(i18nAccess.getText("global.date") + " : ");
			out.println(StringHelper.renderTime(reaction.getDate()));
			out.println("</div>");
			out.println("<div class=\"line\"><b>");
			out.print(i18nAccess.getText("global.title") + " : </b>");
			out.println(reaction.getTitle());
			out.println("</div>");
			out.println("<div class=\"line\"><b>");
			out.print(i18nAccess.getText("global.author") + " : </b>");
			out.println(reaction.getAuthors());
			out.println("</div>");
			out.println("<div class=\"line\"><b>");
			out.print(i18nAccess.getText("global.content") + " : </b>");
			out.println(reaction.getText());
			out.println("</div>");
			out.println("<div class=\"line\">");
			out.println("<input type=\"checkbox\" id=\"" + getDeleteName(reaction) + "\" name=\"" + getDeleteName(reaction) + "\" /><label for=\"" + getDeleteName(reaction) + "\"> " + i18nAccess.getText("global.delete") + "</label>");
			out.println("</div>");
			if (!reaction.isValidReaction()) {
				out.println("<div class=\"line\">");
				out.println("<input type=\"checkbox\" id=\"" + getAcceptName(reaction) + "\" name=\"" + getAcceptName(reaction) + "\" /> <label for=\"" + getAcceptName(reaction) + "\"> " + i18nAccess.getText("global.accept") + "</label>");
				out.println("</div>");
			}
			renderEditReactions(out, reaction.getId(), reactions, ctx, i18nAccess);
			out.println("</li>");
			first = false;
		}
		if (!first) {
			out.println("</ul>");
		}
	}

	protected String getEmail(ContentContext ctx) {
		Collection<Field> fields;
		try {
			fields = getFields(ctx);
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase("email")) {
					if (StringHelper.isMail(field.getValue())) {
						return field.getValue();
					}
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	protected String getTitle(ContentContext ctx) {
		Collection<Field> fields;
		try {
			fields = getFields(ctx);
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase("title")) {
					return field.getValue();
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	protected boolean isWithTitle(ContentContext ctx) {
		Collection<Field> fields;
		try {
			fields = getFields(ctx);
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase("withTitle")) {
					return StringHelper.isTrue(field.getValue());
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	protected boolean isReplyAllowed(ContentContext ctx) {
		Collection<Field> fields;
		try {
			fields = getFields(ctx);
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase("replyAllowed")) {
					return StringHelper.isTrue(field.getValue());
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	protected boolean isRequestLoginToView(ContentContext ctx) {
		Collection<Field> fields;
		try {
			fields = getFields(ctx);
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase("requestLoginToView")) {
					return StringHelper.isTrue(field.getValue());
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	protected boolean isRequestLoginToAdd(ContentContext ctx) {
		Collection<Field> fields;
		try {
			fields = getFields(ctx);
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase("requestLoginToAdd")) {
					return StringHelper.isTrue(field.getValue());
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	public java.util.List<Field> getFields(ContentContext ctx) throws FileNotFoundException, IOException {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());

		java.util.List<Field> outFields = new LinkedList<Field>();

		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("global.email"), "email", "text", getId()));
		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("global.title"), "title", "text", getId()));
		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("content.reaction.request-login-to-view"), "requestLoginToView", "boolean", getId()));
		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("content.reaction.request-login-to-add"), "requestLoginToAdd", "boolean", getId()));
		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("content.reaction.with-title"), "withTitle", "boolean", getId()));
		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("content.reaction.reply-allowed"), "replyAllowed", "boolean", getId()));

		Collections.sort(outFields, new FieldOrderComparator());
		return outFields;
	}

	@Override
	public String getHexColor() {
		return IContentVisualComponent.WEB2_COLOR;
	}

	@Override
	protected String getInputName(String field) {
		return field + "-" + getId();
	}

	@Override
	public String getKey() {
		return getClass().getName();
	}

	public Collection<Reaction> getAllReactions(ContentContext ctx) {
		Collection<Reaction> outReactions = null;
		try {
			loadViewData(ctx);
			outReactions = new TreeSet<Reaction>(new Reaction.OrderCreation(true));
			if (getViewData(ctx) != null) {
				ContentService content = ContentService.getInstance(ctx.getRequest());
				for (Object key : getViewData(ctx).keySet()) {
					if (key.toString().startsWith(REACTIONS_PREFIX_KEY)) {
						Reaction reaction = new Reaction();
						reaction.fromString("" + getViewData(ctx).getProperty("" + key));
						String pageId = readPageIdFromKey("" + key);
						if (pageId != null) {
							try {
								MenuElement targetPage = content.getNavigation(ctx).searchChildFromId(pageId);
								if (targetPage != null) {
									reaction.setUrl(URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), targetPage));
									reaction.setPageTitle(targetPage.getTitle(ctx));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						outReactions.add(reaction);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outReactions;
	}

	public Collection<Reaction> getReactions(ContentContext ctx) {
		Collection<Reaction> outReactions = null;
		try {
			loadViewData(ctx);
			outReactions = new TreeSet<Reaction>(new Reaction.OrderCreation(true));
			if (getViewData(ctx) != null) {
				ContentService content = ContentService.getInstance(ctx.getRequest());
				for (Object key : getViewData(ctx).keySet()) {
					if (key.toString().startsWith(getReactionPrefix(ctx))) {
						Reaction reaction = new Reaction();
						reaction.fromString("" + getViewData(ctx).getProperty("" + key));
						outReactions.add(reaction);
						String pageId = readPageIdFromKey("" + key);
						if (pageId != null) {
							try {
								MenuElement targetPage = content.getNavigation(ctx).searchChildFromId(pageId);
								if (targetPage != null) {
									reaction.setUrl(URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), targetPage));
									reaction.setPageTitle(targetPage.getTitle(ctx));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outReactions;
	}

	public Collection<Reaction> getDeletedReactions(ContentContext ctx) {
		Collection<Reaction> outReactions = null;
		try {
			loadViewData(ctx);
			outReactions = new TreeSet<Reaction>(new Reaction.OrderCreation(true));
			if (getViewData(ctx) != null) {
				ContentService content = ContentService.getInstance(ctx.getRequest());
				for (Object key : getViewData(ctx).keySet()) {
					if (key.toString().startsWith(getDelReactionPrefix(ctx))) {
						Reaction reaction = new Reaction();
						reaction.fromString("" + getViewData(ctx).getProperty("" + key));
						String pageId = readPageIdFromKey("" + key);
						if (pageId != null) {
							try {
								MenuElement targetPage = content.getNavigation(ctx).searchChildFromId(pageId);
								if (targetPage != null) {
									reaction.setUrl(URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), targetPage));
									reaction.setPageTitle(targetPage.getTitle(ctx));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						outReactions.add(reaction);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outReactions;
	}

	public Collection<Reaction> getAllDeletedReactions(ContentContext ctx) {
		Collection<Reaction> outReactions = null;
		try {
			loadViewData(ctx);
			outReactions = new TreeSet<Reaction>(new Reaction.OrderCreation(true));
			if (getViewData(ctx) != null) {
				ContentService content = ContentService.getInstance(ctx.getRequest());
				for (Object key : getViewData(ctx).keySet()) {
					if (key.toString().startsWith(DELETEED_REACTION_PREFIX_KEY)) {
						Reaction reaction = new Reaction();
						String pageId = readPageIdFromKey("" + key);
						if (pageId != null) {
							try {
								MenuElement targetPage = content.getNavigation(ctx).searchChildFromId(pageId);
								if (targetPage != null) {
									reaction.setUrl(URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), targetPage));
									reaction.setPageTitle(targetPage.getTitle(ctx));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						reaction.fromString("" + getViewData(ctx).getProperty("" + key));
						outReactions.add(reaction);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outReactions;
	}

	public String getReactionUnvalidInputName() {
		return "reaction-unvalid-" + getId();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	private List<Field> getViewField(HttpSession session, String parentReactionId) {
		final String sessionKey = "view-field-" + getId() + "-" + StringHelper.neverNull(parentReactionId);
		List<Field> outViewField = (List<Field>) session.getAttribute(sessionKey);
		if (outViewField == null) {
			outViewField = new LinkedList<Field>();
			session.setAttribute(sessionKey, outViewField);
		}
		return outViewField;
	}

	protected java.util.List<Field> getViewFields(ContentContext ctx, String reactionId) throws FileNotFoundException, IOException {
		String fieldSetId = getId() + "-R-" + StringHelper.neverNull(reactionId);
		List<Field> viewField = getViewField(ctx.getRequest().getSession(), reactionId);
		if (viewField.size() == 0) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			GlobalContext globalContext = ctx.getGlobalContext();

			if (isWithTitle(ctx)) {
				viewField.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, null, i18nAccess.getContentViewText("global.title"), "title", "text", fieldSetId));
			}

			if (!isRequestLoginToAdd(ctx)) {
				viewField.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, null, i18nAccess.getContentViewText("global.nickname"), "nickname", "text", fieldSetId));
			}

			viewField.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, null, i18nAccess.getContentViewText("global.text"), "text", "large-text", fieldSetId));

			Collections.sort(viewField, new FieldOrderComparator());
		}
		for (Field field : viewField) {
			if("nickname".equals(field.getName())) {
				User currentUser = getCurrentUser(ctx);
				if (currentUser != null) {
					field.setReadOnly(true);
					field.setValue(currentUser.getLogin());
				} else {
					field.setReadOnly(false);
				}
			}
		}
		return viewField;
	}

	private static User getCurrentUser(ContentContext ctx) {
		IUserFactory userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		return userFactory.getCurrentUser(ctx.getRequest().getSession());
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String id = "react-" + getId();

		out.println("<div id=\"" + id + "\">");

		User user = getCurrentUser(ctx);

		boolean viewAllowed = isRequestLoginToView(ctx) ? user != null : true;
		boolean requestLoginToAdd = isRequestLoginToAdd(ctx);
		boolean addAllowed = viewAllowed && (requestLoginToAdd ? user != null : true);
		boolean replyAllowed = addAllowed && isReplyAllowed(ctx);

		boolean displayUserInfo = requestLoginToAdd;

		boolean displayTitle = isWithTitle(ctx);

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		if (viewAllowed) {
			Collection<Reaction> reactions = getReactions(ctx);

			String title = getTitle(ctx);
			if (title != null && title.trim().length() > 0) {
				out.println("<h3><span>" + title + "</span></h3>");
			}
			renderReactions(out, id, "", null, reactions, ctx, i18nAccess, displayUserInfo, displayTitle, replyAllowed);
			if (addAllowed) {
				renderSendReactionForm(out, id, null, null, ctx, i18nAccess);
			} else {
				out.println("<p>");
				out.println(i18nAccess.getViewText("reaction.login-to-add"));
				out.println("</p>");
			}
		} else {
			out.println("<p>");
			out.println(i18nAccess.getViewText("reaction.login-to-view"));
			out.println("</p>");
		}
		out.println("</div>");
		out.close();
		return writer.toString();
	}

	private void renderReactions(PrintWriter out, String id, String parentHtmlIdSuffix, 
			String parentReactionId, Collection<Reaction> reactions, 
			ContentContext ctx, I18nAccess i18nAccess, 
			boolean displayUserInfo, boolean displayTitle, boolean displayReply) throws Exception {
		parentReactionId = StringHelper.neverNull(parentReactionId);
		int i = 0;
		boolean first = true;
		for (Reaction reaction : reactions) {
			if (reaction.isValidReaction() && parentReactionId.equals(StringHelper.neverNull(reaction.getReplyOf()))) {
				i++;
				String htmlIdSuffix = parentHtmlIdSuffix + "-" + i;
				if (first) {
					out.println("<ul>");
				}
				out.println("<li id=\"message" + htmlIdSuffix + "\" class=\"comment-entry" + (first ? " first" : "") + "\">");

				out.println("<div class=\"metapost\"><span class=\"authors\">");
				User user;
				if (displayUserInfo) {
					IUserFactory userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
					user = userFactory.getUser(reaction.getAuthors());
					if (user == null) {
						userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
						user = userFactory.getUser(reaction.getAuthors());
					}
				} else {
					user = null;
				}
				String userDisplayName;
				if (user != null) {
					out.println(XHTMLHelper.renderUserData(ctx, user));
					userDisplayName = user.getUserInfo().getLastName() + " " + user.getUserInfo().getFirstName();
				} else {
					userDisplayName = StringHelper.removeTag(reaction.getAuthors());
					out.println(userDisplayName);
				}
				out.println("</span></div>");

				if (displayTitle) {
					out.println("<span class=\"title\">");
					out.println(StringHelper.removeTag(reaction.getTitle()));
					out.println("</span>");
				}

				out.println("<div class=\"text\">");
				if (isAllowHtml(ctx)) {
					out.println(XHTMLHelper.safeHTML(reaction.getText()));
				} else {
					out.println(XHTMLHelper.textToXHTML(StringHelper.removeTag(reaction.getText()), true, ctx.getGlobalContext()));
				}
				out.println("</div>");

				out.println("<div class=\"metapost\"><span class=\"first date\">");
				out.println(StringHelper.renderTime(reaction.getDate()));
				out.println("</span></div>");

				if (displayReply) {
					renderSendReactionForm(out, id, reaction, userDisplayName, ctx, i18nAccess);
				}
				renderReactions(out, id, htmlIdSuffix, reaction.getId(), reactions, ctx, i18nAccess, displayUserInfo, displayTitle, displayReply);
				out.println("</li>");
				first = false;
			}
		}
		if (!first) {
			out.println("</ul>");
		}
	}

	private void renderSendReactionForm(PrintWriter out, String id, Reaction reaction, String replyToUser, ContentContext ctx, I18nAccess i18nAccess) throws Exception {
		out.println("<div class=\"reaction-form\">");
		String reactionIdParam = ctx.getRequest().getParameter("reactionId");
		if ((reactionIdParam == null && reaction == null) || (reactionIdParam != null && reaction != null && reactionIdParam.equals(reaction.getId()))) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			if (messageRepository.getGlobalMessage() != null && messageRepository.getGlobalMessage().getTypeLabel() != null) {
				out.println("<div class=\"message\">");
				out.println("<div class=\"" + messageRepository.getGlobalMessage().getTypeLabel() + "\">" + messageRepository.getGlobalMessage().getMessage() + "</div>");
			out.println("</div>");
			}
		}
		String reactionId = reaction == null ? null : reaction.getId();
		String formTitle = reaction == null ? "" : i18nAccess.getViewText("global.reply-to") + " " + replyToUser;
		out.println("<form id=\"reaction-" + getId() + "\" method=\"post\" action=\"" + URLHelper.createURL(ctx) + "#" + id + "\" class=\"big_form\" title=\"" + formTitle + "\">");
		out.println("<input type=\"hidden\" name=\"webaction\" value=\"reaction.add\" />");
		out.println("<input type=\"hidden\" name=\"comp\" value=\"" + getId() + "\" />");
		out.println("<input type=\"hidden\" name=\"reactionId\" value=\"" + StringHelper.neverNull(reactionId) + "\" />");
		Collection<Field> fields = getViewFields(ctx, reactionId);
		for (Field field : fields) {
			if (field != null) {
				out.println(field.getEditXHTMLCode(ctx));
			} else {
				out.println("<div class=\"line\">");
				out.println("field not found : " + field.getName());
				out.println("</div>");
			}
		}

		if (isCaptcha(ctx)) {
			out.println("<div class=\"line captcha\">");
			InfoBean info = InfoBean.getCurrentInfoBean(ctx);
			out.println("<label for=\"captcha-" + getId() + "\" ><span>" + i18nAccess.getViewText("global.captcha") + "</span><img src=\"" + info.getCaptchaURL() + "\" alt=\"captcha\" /></label>");
			out.println("<input id=\"captcha-" + getId() + "\" type=\"text\" name=\"captcha\" value=\"\" />");
			out.println("</div>");
		}

		out.println("<div style=\"height: 0; width: 0; position: absolute; left: -9999px;\">");
		out.println("<label for=\"info-" + getId() + "\" >stay empty.</label>");
		out.println("<input id=\"info-" + getId() + "\" type=\"text\" name=\"fdata\" value=\"\" />");
		out.println("</div>");

		out.println("<input class=\"button light\" type=\"submit\" name=\"ok\" value=\"" + i18nAccess.getViewText("global.send") + "\" />");
		out.println("</form>");
		out.println("</div>");
	}

	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();
		setProperties(new Properties());
	}

	@Override
	public IContentVisualComponent newInstance(ComponentBean bean, ContentContext newCtx) throws Exception {

		ReactionComponent res = (ReactionComponent) this.clone();
		res.setProperties(getProperties()); // transfert meta-data of
		// dynamiccomponent
		res.init(bean, newCtx);

		return res;
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {

		java.util.List<Field> fieldsName = getFields(ctx);
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		Collection<Reaction> reactions = getReactions(ctx);
		synchronized (reactions) {
			Collection<Reaction> reactionToBeDeleted = new LinkedList<Reaction>();
			for (Reaction reaction : reactions) {
				if (requestService.getParameter(getDeleteName(reaction), null) != null) {
					reactionToBeDeleted.add(reaction);
					addChildrenToDelete(reaction, reactions, reactionToBeDeleted);
				}
				if (requestService.getParameter(getAcceptName(reaction), null) != null) {
					reaction.setValidReaction(true);
					setReactions(ctx, reactions);
					setNeedRefresh(true);
				}
			}
			if (reactionToBeDeleted.size() > 0) {
				for (Reaction reaction : reactionToBeDeleted) {
					deleteReaction(ctx, reaction.getId());
				}
				storeViewData(ctx);
				setNeedRefresh(true);
			}
			NavigationHelper.publishOneComponent(ctx, getId());
		}

		for (Field field : fieldsName) {
			if (field.process(ctx.getRequest())) {
				setModify();
			}
		}
		storeProperties();
	}

	private void addChildrenToDelete(Reaction parent, Collection<Reaction> reactions, Collection<Reaction> reactionToBeDeleted) {
		for (Reaction child : reactions) {
			if (parent.getId().equals(child.getReplyOf())) {
				if (!reactionToBeDeleted.contains(child)) {
					reactionToBeDeleted.add(child);
					addChildrenToDelete(child, reactions, reactionToBeDeleted);
				}
			}
		}
	}

	public void deleteReaction(ContentContext ctx, String id) throws IOException {
		if (getViewData(ctx) != null) {
			for (Object key : getViewData(ctx).keySet()) {
				if (key.toString().startsWith(REACTIONS_PREFIX_KEY)) {
					Reaction reaction = new Reaction();
					reaction.fromString("" + getViewData(ctx).getProperty("" + key));
					if (reaction.getId().equals(id)) {
						getViewData(ctx).remove(key);
						String delKey = ((String) key).replaceFirst(REACTIONS_PREFIX_KEY, DELETEED_REACTION_PREFIX_KEY);
						getViewData(ctx).setProperty(delKey, reaction.toString());
						storeViewData(ctx);
						return;
					}
				}
			}
		}
	}

	public void validReaction(ContentContext ctx, String id) throws IOException {
		Collection<Reaction> reactions = getAllReactions(ctx);
		for (Reaction reaction : reactions) {
			if (reaction.getId().equals(id)) {
				reaction.setValidReaction(true);
				setReactions(ctx, reactions);
			}
		}
	}

	private void setReactions(ContentContext ctx, Collection<Reaction> reactions) throws IOException {
		for (Reaction reaction : reactions) {
			getViewData(ctx).setProperty(getReactionPrefix(ctx) + reaction.getId(), reaction.toString());
		}
		storeViewData(ctx);
	}

	@Override
	public String getActionGroupName() {
		return "reaction";
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isContentTimeCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return AbstractVisualComponent.COMPLEXITY_STANDARD;
	}

	public boolean isCaptcha(ContentContext ctx) {
		return StringHelper.isTrue(getConfig(ctx).getProperty("captcha", null));
	}

	public boolean isAllowHtml(ContentContext ctx) {
		return StringHelper.isTrue(getConfig(ctx).getProperty("allow-html", null));
	}

	public int getReactionSize(ContentContext ctx) {
		return getReactions(ctx).size();
	}

	public static void main(String[] args) {
		String pageid = readPageIdFromKey("prefix-0912309-tralala-troiulou");
		System.out.println("***** ReactionComponent.main : pageid = " + pageid); // TODO:
																					// remove
																					// debug
																					// trace
	}
}