/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.web2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
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
import org.javlo.helper.*;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.MailConfig;
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
import org.javlo.utils.StructuredProperties;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author pvandermaesen
 */
public class ReactionComponent extends DynamicComponent implements IAction {
	
	public static String TYPE = "reaction";

	private static final String HTTP_POSTED_IDS_ATTRIBUTE_NAME = ReactionComponent.class.getName() + "#HttpPostIds";
	private static final int HTTP_POSTED_IDS_MAX_SIZE = 10000;

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

		String httpPostId = requestService.getParameter("httpPostId", null);
		if (checkAlreadyPostAndRegister(ctx, httpPostId)) {
			return null;
		}

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
				
				field.process(ctx);
				System.out.println(field.getName() + " = "+field.getValue());
				
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
				if (!reactionComp.isWithLink(ctx) && XHTMLHelper.containsLink(reaction.getTitle()+' '+reaction.getText())) {
					return i18nAccess.getViewText("reaction.error.no-link", "Message cound not contains link."); 
				}
				
				if (!reactionComp.addReaction(ctx, reaction)) {
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("reaction.added"), GenericMessage.INFO));
				} else {
					comp.getPage().setModificationDate(new Date());
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

	private static boolean checkAlreadyPostAndRegister(ContentContext ctx, String httpPostId) {
		HttpSession session = ctx.getRequest().getSession();
		Set<String> ids = (Set<String>) session.getAttribute(HTTP_POSTED_IDS_ATTRIBUTE_NAME);
		if (ids == null) {
			ids = new HashSet<String>();
			session.setAttribute(HTTP_POSTED_IDS_ATTRIBUTE_NAME, ids);
		}
		synchronized (ids) {
			if (ids.contains(httpPostId) || ids.size() > HTTP_POSTED_IDS_MAX_SIZE) {
				return true;
			} else {
				ids.add(httpPostId);
				return false;
			}
		}
	}

	public static final String performDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {

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
			String reactionId = requestService.getParameter("reactionId", "").trim();

			User currentUser = getCurrentUser(ctx);

			Reaction currentReaction = null;

			Collection<Reaction> allReactions = reactionComp.getAllReactions(ctx);
			for (Reaction reaction : allReactions) {
				if (reaction.getId().equals(reactionId)) {
					currentReaction = reaction;
					break;
				}
			}

			if (currentReaction != null && reactionComp.isReactionDeletable(ctx, allReactions, currentReaction, currentUser)) {
				reactionComp.deleteReaction(ctx, reactionId, true);
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
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			out.println("new comment add on context : " + globalContext.getContextKey());
			out.println("");
			out.println(i18nAccess.getViewText("reaction.title", "title")+" : " + reaction.getTitle());
			out.println("author : " + reaction.getAuthors());
			out.println(i18nAccess.getViewText("reaction.text", "text")+" : ");
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
				MailService.getInstance(new MailConfig(globalContext, globalContext.getStaticConfig(), null)).sendMail(from, to, "comment validation on " + globalContext.getContextKey(), writer.toString(), false);
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
			out.println(StringHelper.removeTag(reaction.getTitle()));
			out.println("</div>");
			out.println("<div class=\"line\"><b>");
			out.print(i18nAccess.getText("global.author") + " : </b>");
			out.println(StringHelper.removeTag(reaction.getAuthors()));
			out.println("</div>");
			out.println("<div class=\"line\"><b>");
			out.print(i18nAccess.getText("global.content") + " : </b>");
			if (isAllowHtml(ctx)) {
				out.println(XHTMLHelper.safeHTML(reaction.getText()));
			} else {
				out.println(XHTMLHelper.textToXHTML(StringHelper.removeTag(reaction.getText()), true, ctx.getGlobalContext()));
			}
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

		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("global.email"), "email", null,"text", getId()));
		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("global.title"), "title", null,"text", getId()));
		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("content.reaction.request-login-to-view"), "requestLoginToView", null,"boolean", getId()));
		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("content.reaction.request-login-to-add"), "requestLoginToAdd", null,"boolean", getId()));
		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("content.reaction.with-title"), "withTitle", null,"boolean", getId()));
		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("content.reaction.reply-allowed"), "replyAllowed", null,"boolean", getId()));

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

	public synchronized Collection<Reaction> getReactions(ContentContext ctx) {
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
				viewField.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, null, i18nAccess.getContentViewText("global.title"), "title", null,"text", fieldSetId));
			}

			if (!isRequestLoginToAdd(ctx)) {
				viewField.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, null, i18nAccess.getContentViewText("global.nickname"), "nickname", null,"text", fieldSetId));
			}

			viewField.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, null, i18nAccess.getContentViewText("global.text"), i18nAccess.getViewText("reaction.add"), "text", "large-text", fieldSetId));

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
		return userFactory.getCurrentUser(ctx.getGlobalContext(), ctx.getRequest().getSession());
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String id = "react-" + getId();

		out.println("<div id=\"reactions\"><div id=\"" + id + "\">");

		User currentUser = getCurrentUser(ctx);

		boolean userLogged = currentUser != null;
		boolean viewAllowed = isRequestLoginToView(ctx) ? userLogged : true;
		boolean requestLoginToAdd = isRequestLoginToAdd(ctx);
		boolean addAllowed = viewAllowed && (requestLoginToAdd ? userLogged : true);
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
			if (addAllowed) {
				if (!ctx.isAsPageMode()) {
					renderSendReactionForm(ctx.getCurrentUser(), out, id, null, null, ctx, i18nAccess);
				}
			} else {
				out.println("<p>");
				out.println(i18nAccess.getViewText("reaction.login-to-add"));
				out.println("</p>");
			}
			if (ctx.isAsPageMode()) {
				renderReactionsForMailing(out, id, "", null, reactions, currentUser, ctx, i18nAccess, displayUserInfo, displayTitle, replyAllowed);
			} else {
				renderReactions(out, id, "", null, reactions, currentUser, ctx, i18nAccess, displayUserInfo, displayTitle, replyAllowed);
			}
		} else {
			out.println("<p>");
			out.println(i18nAccess.getViewText("reaction.login-to-view"));
			out.println("</p>");
		}
		out.println("</div></div>");
		out.close();
		return writer.toString();
	}

	private void renderReactions(PrintWriter out, String id, String parentHtmlIdSuffix, 
			String parentReactionId, Collection<Reaction> reactions, 
			User currentUser, ContentContext ctx, I18nAccess i18nAccess,
			boolean displayUserInfo, boolean displayTitle, boolean displayReply) throws Exception {
		parentReactionId = StringHelper.neverNull(parentReactionId);
		int i = 0;
		boolean first = true;
		for (Reaction reaction : reactions) {
			if (reaction.isValidReaction() && parentReactionId.equals(StringHelper.neverNull(reaction.getReplyOf()))) {
				i++;
				String htmlIdSuffix = parentHtmlIdSuffix + "-" + i;
				if (first) {
					out.println("<div class=\"messagelist\"><ul>");
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
				out.println("</span><span class=\"first date\">");
				out.println(StringHelper.renderTime(reaction.getDate()));
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
				out.println("</div><div class=\"actions\">");

				if (currentUser != null && isReactionDeletable(ctx, reactions, reaction, currentUser) && !ctx.isAsPageMode()) {
					renderDeleteReactionForm(out, htmlIdSuffix, reaction, ctx, i18nAccess);
				}

				if (displayReply && !ctx.isAsPageMode()) {
					renderSendReactionForm(user, out, id, reaction, userDisplayName, ctx, i18nAccess);
				}
				out.println("</div>");
				renderReactions(out, id, htmlIdSuffix, reaction.getId(), reactions, currentUser, ctx, i18nAccess, displayUserInfo, displayTitle, displayReply);
				out.println("</li>");
				first = false;
			}
		}
		if (!first) {
			out.println("</ul></div>");
		}
	}
	
	private void renderReactionsForMailing(PrintWriter out, String id, String parentHtmlIdSuffix, 
			String parentReactionId, Collection<Reaction> reactions, 
			User currentUser, ContentContext ctx, I18nAccess i18nAccess,
			boolean displayUserInfo, boolean displayTitle, boolean displayReply) throws Exception {
		parentReactionId = StringHelper.neverNull(parentReactionId);
		int i = 0;
		boolean first = true;
		for (Reaction reaction : reactions) {
			if (reaction.isValidReaction() && parentReactionId.equals(StringHelper.neverNull(reaction.getReplyOf()))) {
				i++;
				String htmlIdSuffix = parentHtmlIdSuffix + "-" + i;
				if (first) {
					out.println("<table style=\"width: 100%\" class=\"messagelist\">");
				}
				out.println("<tr id=\"message" + htmlIdSuffix + "\" class=\"comment-entry" + (first ? " first" : "") + "\">");

				out.println("<td class=\"authors\" width=\"80\" style=\"width: 80px;\">");
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
				out.println("<img src=\""+URLHelper.createAvatarUrl(ctx, user.getUserInfo())+"\" />");
				out.println("</td><td><p>");
				if (user != null) {
					out.println("<div class=\"username\">"+user.getLabel()+"</div>");
				}
				out.println("<div class=\"first date\">"+StringHelper.renderTime(reaction.getDate())+"</div>");		

				if (displayTitle) {
					out.println("<div style=\"font-weight: bold\" class=\"title\">");
					out.println(StringHelper.removeTag(reaction.getTitle()));
					out.println("</div>");
				}

				out.println("<p class=\"text\">");
				if (isAllowHtml(ctx)) {
					out.println(XHTMLHelper.safeHTML(reaction.getText()));
				} else {
					out.println(XHTMLHelper.textToXHTML(StringHelper.removeTag(reaction.getText()), true, ctx.getGlobalContext()));
				}
				out.println("</p></p>");
				out.println("</td></tr><tr><td>&nbsp;</td><td>");
				renderReactionsForMailing(out, id, htmlIdSuffix, reaction.getId(), reactions, currentUser, ctx, i18nAccess, displayUserInfo, displayTitle, displayReply);
				out.println("</td></tr>");
				first = false;
			}
		}
		if (!first) {
			out.println("</table>");
		}
	}

	private boolean isReactionDeletable(ContentContext ctx, Collection<Reaction> allReactions, Reaction currentReaction, User currentUser) {
		if (currentUser != null && currentReaction.getAuthors() != null && currentReaction.getAuthors().equals(currentUser.getLogin())) {
			for (Reaction reaction : allReactions) {
				if (currentReaction.getId().equals(reaction.getReplyOf())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private void renderDeleteReactionForm(PrintWriter out, String id, Reaction reaction, ContentContext ctx, I18nAccess i18nAccess) throws Exception {
		out.println("<div class=\"reaction-delete-form\">");
		String reactionId = reaction.getId();
		out.println("<form id=\"reaction-" + getId() + "\" method=\"post\" action=\"" + URLHelper.createURL(ctx) + "#" + id + "\" class=\"inline_form\" >");
		out.println("<input type=\"hidden\" name=\"webaction\" value=\"reaction.delete\" />");
		out.println("<input type=\"hidden\" name=\"comp\" value=\"" + getId() + "\" />");
		out.println("<input type=\"hidden\" name=\"reactionId\" value=\"" + StringHelper.neverNull(reactionId) + "\" />");
		out.println("<div style=\"height: 0; width: 0; position: absolute; left: -9999px;\">");
		out.println("<label for=\"info-" + getId() + "\" >stay empty.</label>");
		out.println("<input id=\"info-" + getId() + "\" type=\"text\" name=\"fdata\" value=\"\" />");
		out.println("</div>");
		out.println("<input class=\"button light needconfirm btn btn-default btn-secondary btn-delete btn-sm\" type=\"submit\" name=\"ok\" value=\"" + i18nAccess.getViewText("global.delete") + "\" />");
		out.println("</form>");
		out.println("</div>");
	}

	private void renderSendReactionForm(User user, PrintWriter out, String id, Reaction reaction, String replyToUser, ContentContext ctx, I18nAccess i18nAccess) throws Exception {
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
		out.println("<input type=\"hidden\" name=\"httpPostId\" value=\"" + StringHelper.getRandomId() + "\" />");
		out.println("<input type=\"hidden\" name=\"reactionId\" value=\"" + StringHelper.neverNull(reactionId) + "\" />");
		if (user != null && !StringHelper.isEmpty(user.getUserInfo().getAvatarURL())) {
			out.println("<figure class=\"avatar\"><img src=\""+user.getUserInfo().getAvatarURL()+"\" alt=\""+user.getName()+"\" /></figure>");
		} else {
			out.println("<figure class=\"avatar empty\"><span></span></figure>");
		}
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

		out.println("<div class=\"actions\">");
		out.println("<input class=\"button light btn btn-default btn-sm btn-secondary\" type=\"submit\" name=\"ok\" value=\"" + i18nAccess.getViewText("global.send") + "\" />");
		out.println("</div>");
		out.println("</form>");
		out.println("</div>");
	}

	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();
		setProperties(new StructuredProperties());
	}

	@Override
	public IContentVisualComponent newInstance(ComponentBean bean, ContentContext newCtx, MenuElement page) throws Exception {

		ReactionComponent res = (ReactionComponent) this.clone();
		res.setProperties(getProperties()); // transfert meta-data of
		// dynamiccomponent
		res.setPage(page);
		res.init(bean, newCtx);

		return res;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {

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
					deleteReaction(ctx, reaction.getId(),true);
				}
				storeViewData(ctx);
				setNeedRefresh(true);
			}
			NavigationHelper.publishOneComponent(ctx, getId());
		}

		for (Field field : fieldsName) {
			if (field.process(ctx)) {
				setModify();
			}
		}
		storeProperties();
		return null;
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

	public void deleteReaction(ContentContext ctx, String id, boolean store) throws IOException {
		if (getViewData(ctx) != null) {
			for (Object key : getViewData(ctx).keySet()) {
				if (key.toString().startsWith(REACTIONS_PREFIX_KEY)) {
					Reaction reaction = new Reaction();
					reaction.fromString("" + getViewData(ctx).getProperty("" + key));
					if (reaction.getId().equals(id)) {
						getViewData(ctx).remove(key);
						String delKey = ((String) key).replaceFirst(REACTIONS_PREFIX_KEY, DELETEED_REACTION_PREFIX_KEY);
						getViewData(ctx).setProperty(delKey, reaction.toString());
						if (store) {
							storeViewData(ctx);
						}
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

	protected boolean isWithLink(ContentContext ctx) {
		return StringHelper.isTrue(getConfig(ctx).getProperty("with-link",null),true);
	}

	public int getReactionSize(ContentContext ctx) {
		return getReactions(ctx).size();
	}
	
	public static void main(String[] args) {
		System.out.println("*** XHTMLHelper.containsLink(reaction.getTitle()+' '+reaction.getText() = "+XHTMLHelper.containsLink(""+' '+"c'est top"));
	}
	
	@Override
	public String getFontAwesome() {	
		return "comments";
	}
}