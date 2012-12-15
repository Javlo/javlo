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
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
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
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.IUserFactory;
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
				if (!reaction1.isValid() && reaction2.isValid()) {
					return order;
				} else if (reaction1.isValid() && !reaction2.isValid()) {
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
		private boolean valid = false;
		private Date date = new Date();

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
				setValid(StringHelper.isTrue(contentArray[6]));
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

		public String getText() {
			return text;
		}

		public String getTitle() {
			return title;
		}

		public boolean isValid() {
			return valid;
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

		public void setText(String text) {
			this.text = text;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setValid(boolean valid) {
			this.valid = valid;
		}

		@Override
		public String toString() {
			return getId() + '|' + getTitle() + '|' + getAuthors() + '|' + getText() + '|' + getEmail() + '|' + StringHelper.renderSortableTime(getDate()) + '|' + isValid();
		}

		public String getDisplayableDate() {
			return StringHelper.renderSortableTime((getDate()));
		}
	}

	private static final String REACTIONS_PREFIX = "reactions-";

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
			List<Field> fields = reactionComp.getViewFields(ctx);
			Reaction reaction = new Reaction();
			boolean validReaction = false;
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
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
			if (validReaction) {
				if (!reactionComp.addReaction(ctx, reaction)) {
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("reaction.added"), GenericMessage.INFO));
				} else {
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("reaction.added-novalidation"), GenericMessage.INFO));
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
			reaction.setValid(true);
		}

		Collection<Reaction> reactions = getReactions(ctx);
		synchronized (reactions) {
			reactions.add(reaction);
			setReactions(ctx, reactions);
		}
		return reaction.isValid();
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
		String[] reactionId = new String[reactions.size()];
		String[] reactionTitle = new String[reactions.size()];
		int i = 0;
		for (Reaction reaction : reactions) {
			reactionId[i] = reaction.getId();
			reactionTitle[i] = "[" + StringHelper.renderSortableTime(reaction.getDate()) + "] - " + reaction.getTitle() + " - [" + reaction.getAuthors() + "]";
			i++;
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());
		out.println("<fieldset>");
		out.print("<legend>");
		out.println(i18nAccess.getText("global.comment"));
		out.println("</legend>");
		out.println("<div class=\"reactions\">");
		String sep = "";
		for (Reaction reaction : reactions) {
			out.print(sep);
			out.println("<div id=\"reaction-" + reaction.getId() + "\" class=\"reaction\">");
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
			if (!reaction.isValid()) {
				out.println("<div class=\"line\">");
				out.println("<input type=\"checkbox\" id=\"" + getAcceptName(reaction) + "\" name=\"" + getAcceptName(reaction) + "\" /> <label for=\"" + getAcceptName(reaction) + "\"> " + i18nAccess.getText("global.accept") + "</label>");
				out.println("</div>");
			}

			out.println("</div>");
			sep = "<hr />";
		}
		out.println("</div></fieldset>");

		out.println("</div>");
		out.close();
		return writer.toString();
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

	@Override
	public java.util.List<Field> getFields(ContentContext ctx) throws FileNotFoundException, IOException {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());

		java.util.List<Field> outFields = new LinkedList<Field>();

		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), i18nAccess.getText("global.email"), "email", "text", getId()));

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

	public Collection<Reaction> getReactions(ContentContext ctx) {
		Collection<Reaction> outReactions = null;
		try {
			loadViewData(ctx);
			outReactions = new TreeSet<Reaction>(new Reaction.OrderCreation(true));
			if (getViewData(ctx) != null) {
				for (Object rawReaction : getViewData(ctx).values()) {
					Reaction reaction = new Reaction();
					reaction.fromString("" + rawReaction);
					outReactions.add(reaction);
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

	private List<Field> getViewField(HttpSession session) {
		final String sessionKey = "view-field-" + getId();
		List<Field> outViewField = (List<Field>) session.getAttribute(sessionKey);
		if (outViewField == null) {
			outViewField = new LinkedList<Field>();
			session.setAttribute(sessionKey, outViewField);
		}
		return outViewField;
	}

	protected java.util.List<Field> getViewFields(ContentContext ctx) throws FileNotFoundException, IOException {
		List<Field> viewField = getViewField(ctx.getRequest().getSession());
		if (viewField.size() == 0) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

			if (isWithTitle()) {
				viewField.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, null, i18nAccess.getContentViewText("global.title"), "title", "text", getId()));
			}

			IUserFactory userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
			if (userFactory.getCurrentUser(ctx.getRequest().getSession()) != null) {
				// getProperties().setProperty("field.nickname.value", userFactory.getCurrentUser(ctx.getRequest().getSession()).getLogin());
				Field field = FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, null, i18nAccess.getContentViewText("global.nickname"), "nickname", "text", getId());
				// field.setReadOnly(true);
				viewField.add(field);
			} else {
				viewField.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, null, i18nAccess.getContentViewText("global.nickname"), "nickname", "text", getId()));
			}

			viewField.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, null, i18nAccess.getContentViewText("global.text"), "text", "large-text", getId()));

			Collections.sort(viewField, new FieldOrderComparator());
		}
		return viewField;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String id = "react-" + getId();

		out.println("<div id=\"" + id + "\" class=\"" + getType() + "\">");

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		Collection<Reaction> reactions = getReactions(ctx);

		MessageRepository messageRepository = MessageRepository.getInstance(ctx);
		if (messageRepository.getGlobalMessage() != null) {
			out.println("<div class=\"message\">");
			out.println("<div class=\"" + messageRepository.getGlobalMessage().getTypeLabel() + "\">" + messageRepository.getGlobalMessage().getMessage() + "</div>");
			out.println("</div>");
		}

		int i = 0;
		out.println("<ul>");
		for (Reaction reaction : reactions) {
			if (reaction.isValid()) {
				i++;
				out.println("<li id=\"message-" + i + "\" class=\"comment-entry\">");

				out.println("<div class=\"metapost\"><span class=\"authors\">");
				out.println(StringHelper.removeTag(reaction.getAuthors()));
				out.println("</span></div>");

				if (isWithTitle()) {
					out.println("<span class=\"title\">");
					out.println(StringHelper.removeTag(reaction.getTitle()));
					out.println("</span>");
				}

				out.println("<div class=\"text\">");
				out.println(XHTMLHelper.textToXHTML(StringHelper.removeTag(reaction.getText())));
				out.println("</div>");

				out.println("<div class=\"metapost\"><span class=\"first date\">");
				out.println(StringHelper.renderTime(reaction.getDate()));
				out.println("</span></div>");

				out.println("</li>");
			}
		}
		out.println("</ul>");
		out.println("<div class=\"reaction-form\">");
		out.println("<form id=\"reaction-" + getId() + "\" method=\"post\" action=\"" + URLHelper.createURL(ctx) + "#" + id + "\" class=\"big_form\" >");
		out.println("<input type=\"hidden\" name=\"webaction\" value=\"reaction.add\" />");
		out.println("<input type=\"hidden\" name=\"comp\" value=\"" + getId() + "\" />");
		Collection<Field> fields = getViewFields(ctx);
		for (Field field : fields) {
			if (field != null) {
				out.println(field.getEditXHTMLCode(ctx));
			} else {
				out.println("<div class=\"line\">");
				out.println("field not found : " + field.getName());
				out.println("</div>");
			}
		}

		out.println("<div style=\"height: 0; width: 0; position: absolute; left: -9999px;\">");
		out.println("<label for=\"info-" + getId() + "\" >stay empty.</label>");
		out.println("<input id=\"info-" + getId() + "\" type=\"text\" name=\"fdata\" value=\"\" />");
		out.println("</div>");

		out.println("<input class=\"button light\" type=\"submit\" name=\"ok\" value=\"" + i18nAccess.getViewText("global.send") + "\" />");
		out.println("</form>");
		out.println("</div>");
		out.println("</div>");
		out.close();
		return writer.toString();
	}

	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();
		setProperties(new Properties());
	}

	protected boolean isWithTitle() {
		return false;
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
				}
				if (requestService.getParameter(getAcceptName(reaction), null) != null) {
					reaction.setValid(true);
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

	public void deleteReaction(ContentContext ctx, String id) throws IOException {
		getViewData(ctx).remove(REACTIONS_PREFIX + id);
		storeViewData(ctx);
	}

	public void validReaction(ContentContext ctx, String id) throws IOException {
		Collection<Reaction> reactions = getReactions(ctx);
		for (Reaction reaction : reactions) {
			if (reaction.getId().equals(id)) {
				reaction.setValid(true);
				setReactions(ctx, reactions);
			}
		}
	}

	private void setReactions(ContentContext ctx, Collection<Reaction> reactions) throws IOException {
		for (Reaction reaction : reactions) {
			getViewData(ctx).setProperty(REACTIONS_PREFIX + reaction.getId(), reaction.toString());
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
}