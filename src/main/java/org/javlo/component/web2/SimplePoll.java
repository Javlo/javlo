/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.web2;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.text.Paragraph;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;

/**
 * @author pvandermaesen
 */
public class SimplePoll extends Paragraph implements IAction {

	private static final String INTERACTIVE = "interactive";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(SimplePoll.class.getName());

	public static final String TYPE = "simple-poll";

	public static final String performVote(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		String vote = requestService.getParameter("response", null);
		String compId = requestService.getParameter("comp-id", null);
		String emptyField = requestService.getParameter("__email", "");
		if (emptyField.length() > 0) {
			logger.warning("empty field not empty : " + emptyField);
		} else {
			if (compId != null) {
				ContentContext ctx = ContentContext.getContentContext(request, response);
				SimplePoll comp = (SimplePoll) ContentService.getInstance(request).getComponent(ctx, compId);
				if (comp != null) {
					if (!comp.allReadyVoted(ctx, false)) {
						comp.addVotes(ctx, vote);
					}
					ctx.getRequest().getSession().setAttribute(comp.getSessionKey(), comp.getSessionKey());
					GlobalContext globalContext = GlobalContext.getInstance(request);
					globalContext.setTimeAttribute(comp.getVoteTimeKey(ctx), comp.getVoteTimeKey(ctx));
					Cookie cookie = new Cookie(comp.getCookieName(), comp.getCookieName());
					cookie.setPath("/");
					cookie.setMaxAge(60 * 24 * 30); // 1 month
					response.addCookie(cookie);
				} else {
					logger.warning("component not found : " + compId);
				}
			}
		}
		return null;
	}

	protected boolean __allReadyVoted(ContentContext ctx, boolean beforeSubmit) {

		/** check submit form **/
		if (beforeSubmit) {
			if (ctx.getRequest().getParameter("response") != null) {
				return true;
			}
		}

		return false;
	}

	public synchronized void addVotes(ContentContext ctx, String code) throws IOException {
		System.out.println("***** SimplePoll.addVotes : code = " + code); // TODO: remove debug trace
		if (code == null) {
			return;
		}
		int currentValue = 0;
		if (getViewData(ctx).getProperty(code) != null) {
			currentValue = Integer.parseInt("" + getViewData(ctx).get(code));
		}
		currentValue++;
		getViewData(ctx).setProperty(code, "" + currentValue);

		storeViewData(ctx);
	}

	protected boolean allReadyVoted(ContentContext ctx, boolean beforeSubmit) {

		/** check submit form **/
		if (beforeSubmit) {
			if (ctx.getRequest().getParameter("response") != null) {
				return true;
			}
		}

		/** check session **/
		if (ctx.getRequest().getSession().getAttribute(getSessionKey()) != null) {
			return true;
		}

		/** check ip **/
		String voteTimeKey = getVoteTimeKey(ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (globalContext.getTimeAttribute(voteTimeKey) != null) {
			return true;
		}

		/** check cookie **/
		Cookie[] cookies = ctx.getRequest().getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(getCookieName())) {
					return true;
				}
			}
		}

		return false;
	}

	/*****************
	 * ACTIONS
	 *****************/

	@Override
	public String getActionGroupName() {
		return "poll";
	}

	private String getCookieName() {
		return getSessionKey();
	}

	protected String getCurrentResult(ContentContext ctx) throws IOException {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		List<String> responses = StringHelper.textToList(getResponses());
		Iterator<String> results = StringHelper.textToList(getResult()).iterator();
		int index = 1;
		for (String response : responses) {
			String id = "id_" + StringHelper.getRandomId();
			double vote;
			if (!getStyle(ctx).equalsIgnoreCase(INTERACTIVE)) {
				vote = Integer.parseInt(results.next());
			} else {
				vote = getVotes(ctx, "" + index);
			}
			String percent = StringHelper.renderDoubleAsPercentage((vote / getVotesCount(ctx)));
			int percentValue = Integer.parseInt(percent.replace("%", "").trim());
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			if (AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession()).getCurrentUser(ctx.getRequest().getSession()) != null) { // if admin logged
				if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
					percent = percent + " (" + Math.round(vote) + '/' + getVotesCount(ctx) + ')';
				} else if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
					percent = "" + Math.round(vote) + '/' + getVotesCount(ctx);
				}
			}
			String resultBar = "<div class=\"result-bar\"><span class=\"bar\" style=\"width: " + percentValue + "px;\" ></span></div>";
			out.println("<div class=\"line\"><label for=\"" + id + "\">" + response + "</label>" + resultBar + percent + "</div>");
			index++;
		}
		out.close();
		return writer.toString();
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		finalCode.append(getSpecialInputTag());
		finalCode.append("<div class=\"line\">");
		finalCode.append("<label for=\"" + getSeparatorInputName() + "\">" + i18nAccess.getText("content.simple-poll.question") + " :</label>");
		finalCode.append("<input id=\"" + getSeparatorInputName() + "\" name=\"" + getSeparatorInputName() + "\" value=\"" + getQuestion() + "\" />");
		finalCode.append("</div>");
		finalCode.append("<textarea id=\"" + getContentName() + "\" name=\"" + getContentName() + "\"");
		finalCode.append(" rows=\"" + (countLine() + 1) + "\" onkeyup=\"javascript:resizeTextArea($('" + getContentName() + "'));\">");
		finalCode.append(getResponses());
		finalCode.append("</textarea>");
		finalCode.append("<fieldset>");
		finalCode.append("<legend>" + i18nAccess.getText("content.simple-poll.current-result") + "</legend>");
		finalCode.append(getCurrentResult(ctx));
		finalCode.append("</fieldset>");
		return finalCode.toString();
	}

	public String getQuestion() {
		String value = getValue();
		if (value.indexOf('#') > -1) {
			try {
				value = value.substring(0, value.indexOf('#'));
			} catch (Throwable e) {
				return "";
			}
			return value;
		} else {
			return "";
		}
	}

	protected String getResponses() {
		String value = getValue();
		if (value.indexOf('#') > -1) {
			try {
				value = value.substring(value.indexOf('#') + 1);
				value = value.substring(0, value.indexOf('#'));
			} catch (Throwable e) {
				return "";
			}
		}
		return value;
	}

	protected String getResult() {
		String value = "";
		if (getValue().indexOf('#') > -1) {
			try {
				value = getValue();
				value = value.substring(value.indexOf('#') + 1);
				value = value.substring(value.indexOf('#') + 1);
			} catch (Throwable e) {
				return "";
			}
		}
		return value;
	}

	public String getSeparatorInputName() {
		return "separator-" + getId();
	}

	private String getSessionKey() {
		return "poll_" + getId();
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {

		String archived = "archived";
		String interactive = INTERACTIVE;

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			archived = i18nAccess.getText("content.simple-poll.archived");
			interactive = i18nAccess.getText("content.simple-poll.interactive");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new String[] { interactive, archived };
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { INTERACTIVE, "archived" };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println("<form id=\"poll-" + getId() + "\" method=\"post\">");
		String cssClass = "";
		if (allReadyVoted(ctx, true) || !getStyle(ctx).equalsIgnoreCase(INTERACTIVE)) {
			cssClass = " class=\"result\"";
		}
		out.println("<fieldset" + cssClass + ">");
		out.println("<legend>" + getQuestion() + "</legend>");
		out.println("<div style=\"position: absolute; left: -5647px; left: -8432px;\">");
		String fakeFieldId = StringHelper.getRandomId();
		out.println("<label for=\"" + fakeFieldId + "\">stay empty</label>");
		out.println("<input id=\"" + fakeFieldId + "\" type=\"text\" name=\"__email\" value=\"\" />");
		out.println("</div>");
		if (!allReadyVoted(ctx, true) && getStyle(ctx).equalsIgnoreCase(INTERACTIVE)) {
			out.println("<input type=\"hidden\" name=\"webaction\" value=\"poll.vote\" />");
			out.println("<input type=\"hidden\" name=\"comp-id\" value=\"" + getId() + "\" />");
			List<String> responses = StringHelper.textToList(getResponses());
			int index = 1;
			for (String response : responses) {
				String id = "id_" + StringHelper.getRandomId();
				out.println("<div class=\"line\"><input type=\"radio\" name=\"response\" id=\"" + id + "\" value=\"" + index + "\" /><label for=\"" + id + "\">" + response + "</label></div>");
				index++;
			}
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			out.println("<div class=\"line\">");
			out.println("<input type=\"submit\" value=\"" + i18nAccess.getViewText("global.ok") + "\" />");
			out.println("</div>");
		} else {
			out.println(getCurrentResult(ctx));
		}

		out.println("</fieldset>");
		out.println("</form>");

		out.close();
		return writer.toString();
	}

	protected synchronized int getVotes(ContentContext ctx, String code) throws IOException {
		if (getViewData(ctx).getProperty(code) == null) {
			return 0;
		}
		return Integer.parseInt("" + getViewData(ctx).getProperty(code));
	}

	protected synchronized int getAllVotesCount(ContentContext ctx) throws IOException {
		List<String> responses = StringHelper.textToList(getResponses());
		int index = 1;
		int totalVote = 0;
		for (String response : responses) {
			totalVote = totalVote + getVotes(ctx, "" + index);
			index++;
		}
		return totalVote;
	}

	protected synchronized int getVotesCount(ContentContext ctx) throws IOException {
		if (!getStyle(ctx).equalsIgnoreCase(INTERACTIVE)) {
			Iterator<String> results = StringHelper.textToList(getResult()).iterator();
			int intValue = 0;
			while (results.hasNext()) {
				intValue += Integer.parseInt(results.next());
			}
			return intValue;
		} else {
			return Math.max(getAllVotesCount(ctx), 1);
		}
	}

	private String getVoteTimeKey(ContentContext ctx) {
		return ctx.getRequest().getRemoteAddr() + "_" + getId();
	}

	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		loadViewData(ctx);
	}

	@Override
	public boolean isInline() {
		return false;
	}

	@Override
	protected void onStyleChange(ContentContext ctx) {
		try {
			if (getStyle(ctx).equalsIgnoreCase(INTERACTIVE)) {
				List<String> responses = StringHelper.textToList(getResponses());
				if (getResult().trim().length() > 0) {
					List<String> results = StringHelper.textToList(getResult());
					Iterator<String> resultsIt = results.iterator();
					int index = 1;
					for (String response : responses) {
						String result = resultsIt.next();
						getViewData(ctx).setProperty("" + index, result);
						index++;
					}
					try {
						storeViewData(ctx);
					} catch (IOException e) {
						e.printStackTrace();
					}
					setModify();
				}
			} else {
				List<String> responses = StringHelper.textToList(getResponses());
				StringWriter writer = new StringWriter();
				PrintWriter out = new PrintWriter(writer);
				int index = 1;
				for (String response : responses) {
					out.println(getVotes(ctx, "" + index));
					index++;
				}
				out.close();
				String newContent = getQuestion() + '#' + getResponses() + '#' + writer.toString();
				if (!getComponentBean().getValue().equals(newContent)) {
					getComponentBean().setValue(newContent);
				}
				setModify();
			}
			super.onStyleChange(ctx);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newContent = requestService.getParameter(getContentName(), null);
		String question = requestService.getParameter(getSeparatorInputName(), "");

		if (newContent != null) {
			if (question.length() > 0) {
				newContent = question + '#' + newContent + '#' + getResult();
			}
			if (!getComponentBean().getValue().equals(newContent)) {
				getComponentBean().setValue(newContent);
				setModify();
			}
		}
	}
}
