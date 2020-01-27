package org.javlo.component.web2;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringEscapeUtils;
import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.RequestService;

import com.google.gson.Gson;

public class QuizzParty extends AbstractVisualComponent implements IAction {

	public static final String TYPE = "quizz-party";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public int getTitleLevel(ContentContext ctx) {	
		return MIDDLE_LABEL_LEVEL;
	}
	
	@Override
	public String getTextLabel(ContentContext ctx) {
		return StringEscapeUtils.unescapeHtml4(getValue());		
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

	@Override
	public String getFontAwesome() {
		return "question-circle-o";
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		QuizzPartyContext quizz = QuizzPartyContext.getInstance(ctx.getRequest().getSession());
		if (quizz != null) {
			ctx.getRequest().setAttribute("master", quizz.getMasterSessionId().equals(ctx.getRequest().getSession().getId()));
			ctx.getRequest().setAttribute("statusUrl", URLHelper.createActionURL(ctx, getActionGroupName()+".status", ""));
		}
	}

	public static String performCreate(ContentContext ctx, HttpSession session, RequestService rs) throws Exception {
		QuizzPartyContext.getInstance(session, null);
		return null;
	}

	public static String performJoin(ContentContext ctx, HttpSession session, RequestService rs) throws Exception {
		String name = rs.getParameter("name", "").trim();
		if (name.length()<QuizzPartyContext.HASH_SIZE+1) {
			return "min length of name is : "+(QuizzPartyContext.HASH_SIZE+1);
		}
		QuizzPartyContext quizz = QuizzPartyContext.getInstance(session, name);
		if (quizz == null) {
			return "quizz '"+name+"' not found.";
		} else {
			return null;
		}
	}
	
	public static String performStatus(ContentContext ctx, RequestService rs) throws Exception {
		QuizzPartyContext quizz = QuizzPartyContext.getInstance(ctx.getRequest().getSession());
		String json = "[]";
		if (quizz != null) {
			json = new Gson().toJson(quizz);			
		}
		ResourceHelper.writeStringToStream(json, ctx.getResponse().getOutputStream(), ContentContext.CHARACTER_ENCODING);
		ctx.setStopRendering(true);
		return null;
	}
	
	public static String performNext(ContentContext ctx, HttpSession session, RequestService rs) throws Exception {
		QuizzPartyContext quizz = QuizzPartyContext.getInstance(ctx.getRequest().getSession());
		if (quizz != null && quizz.getMasterSessionId().equals(session.getId())) {
			quizz.nextQuestion();
		} else {
			return "security error !";
		}
		return null;
	}
	
	public static String performPrevious(ContentContext ctx, HttpSession session, RequestService rs) throws Exception {
		QuizzPartyContext quizz = QuizzPartyContext.getInstance(ctx.getRequest().getSession());
		if (quizz != null && quizz.getMasterSessionId().equals(session.getId())) {
			quizz.previousQuestion();
		} else {
			return "security error !";
		}
		return null;
	}
	
	public static String performReset(ContentContext ctx, HttpSession session, RequestService rs) throws Exception {
		QuizzPartyContext quizz = QuizzPartyContext.getInstance(ctx.getRequest().getSession());
		if (quizz != null) {
			quizz.reset(session);
		}
		return null;
	}
	
	public static String performVote(ContentContext ctx, HttpSession session, RequestService rs) throws Exception {
		QuizzPartyContext quizz = QuizzPartyContext.getInstance(ctx.getRequest().getSession());
		if (quizz != null) {
			quizz.vote(Integer.parseInt(rs.getParameter("vote", null)), session.getId());
		} else {
			return "quizz not found.";
		}
		return null;
	}
	
}
