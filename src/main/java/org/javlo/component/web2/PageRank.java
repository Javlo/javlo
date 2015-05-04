/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.web2;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IPageRank;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class PageRank extends AbstractVisualComponent implements IPageRank, IAction {

	public static final String TYPE = "page-rank";

	private static final String VOTES_KEY = "votes";
	private static final String VALUE_KEY = "value";

	private static final boolean DEBUG = false;

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		try {
			MenuElement currentPage = ctx.getCurrentPage();
			if (!currentPage.isRealContent(ctx)) {
				return "";
			}

			if (isFirstElementOfRepeatSequence(ctx)) {
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "<div class=\"" + getType() + "\">";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		try {
			MenuElement currentPage = ctx.getCurrentPage();
			if (!currentPage.isRealContent(ctx)) {
				return "";
			}

			if (isFirstElementOfRepeatSequence(ctx)) {
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "</div>";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getHexColor() {
		return WEB2_COLOR;
	}

	@Override
	public boolean isUnique() {
		return true;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}
	
	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		MenuElement currentPage = ctx.getCurrentPage();
		out.println("<div class=\"line\">");
		out.println("rank value : " + getRankValue(ctx, currentPage.getPath()));
		out.println("vote count : " + getVotes(ctx, currentPage.getPath()));
		out.println("</div>");
		out.close();
		return writer.toString();
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		if (!currentPage.isRealContent(ctx)) {
			return "";
		}

		if (isFirstElementOfRepeatSequence(ctx)) {
			return "";
		}

		boolean voted = false;
		if (ctx.getRequest().getSession().getAttribute(generateKey(currentPage.getPath(), "VOTED")) != null && !DEBUG) {
			voted = true;
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		double pageRank = currentPage.getPageRank(ctx);
		String cssClass = "lowest";
		if (pageRank > 0.1 && pageRank < 0.25) {
			cssClass = "low";
		} else if (pageRank >= 0.25 && pageRank < 0.75) {
			cssClass = "middle";
		} else if (pageRank >= 0.75 && pageRank < 0.9) {
			cssClass = "high";
		} else if (pageRank >= 0.9) {
			cssClass = "higher";
		}
		if (getVotes(ctx, currentPage.getPath()) == 0) {
			cssClass = "middle";
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editContext = EditContext.getInstance(globalContext, ctx.getRequest().getSession());

		if (DEBUG || editContext.isEditPreview()) {
			out.println("<div class=\"debug\">");
			out.println("rank value : " + getRankValue(ctx, currentPage.getPath()));
			out.println("vote count : " + getVotes(ctx, currentPage.getPath()));
			out.println("</div>");
		}

		if (RequestHelper.isCookie(ctx.getRequest(), getId(), "voted") || voted && !DEBUG) {
			out.println("<div class=\"score " + cssClass + "\"><span class=\"result\">" + StringHelper.renderDoubleAsPercentage(currentPage.getPageRank(ctx)) + "</span><span class=\"message\">Thanks for your vote.</span></div>");
		} else {
			Cookie cookie = new Cookie(getId(), "voted");
			cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
			cookie.setPath("/");
			ctx.getResponse().addCookie(cookie);
			// out.println("#votes : "+getVotes());
			// out.println("rank   : "+getRankValue());
			if (getValue() != null && getValue().trim().length() > 0) {
				out.println("<p>" + getValue() + "</p>");
			}
			out.println("<div class=\"score result " + cssClass + "\">" + StringHelper.renderDoubleAsPercentage(currentPage.getPageRank(ctx)) + "</div>");
			out.println("<form id=\"rank-" + getId() + "\">");
			out.println("<input type=\"hidden\" name=\"webaction\" value=\"pagerank.rank\" />");
			out.println("<input type=\"hidden\" name=\"comp-id\" value=\"" + getId() + "\" />");			
			out.println("<button type=\"submit\" class=\"positive btn btn-primary\" name=\"positive\" title=\"yes\"><span class=\"glyphicon glyphicon-thumbs-up\"></span></button>");
			out.println("<button type=\"submit\" class=\"negative btn btn-default\" name=\"negative\" title=\"no\"><span class=\"glyphicon glyphicon-thumbs-down\"></span></button>");
			out.println("</form>");
		}
		out.close();
		return writer.toString();
	}

	private static String generateKey(String path, String prefix) {
		try {
			return prefix + '_' + path;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setVotes(ContentContext ctx, int votes) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		getViewData(ctx).setProperty(generateKey(currentPage.getPath(), VOTES_KEY), "" + votes);
		storeViewData(ctx);
	}

	@Override
	public int getVotes(ContentContext ctx, String path) {
		String votesStr = null;
		try {
			votesStr = getViewData(ctx).getProperty(generateKey(path, VOTES_KEY));
			if (votesStr == null) {
				votesStr = "0";
				getViewData(ctx).setProperty(generateKey(path, VOTES_KEY), votesStr);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Integer.parseInt(votesStr);
	}

	public void setRankValue(ContentContext ctx, int value) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		getViewData(ctx).setProperty(generateKey(currentPage.getPath(), VALUE_KEY), "" + value);
		storeViewData(ctx);
	}

	@Override
	public int getRankValue(ContentContext ctx, String path) {
		String rankStr = null;
		try {
			rankStr = getViewData(ctx).getProperty(generateKey(path, VALUE_KEY));
			if (rankStr == null) {
				rankStr = "0";
				getViewData(ctx).setProperty(generateKey(path, VALUE_KEY), rankStr);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Integer.parseInt(rankStr);
	}

	public static final String performRank(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentService content = ContentService.getInstance(request);
		MenuElement currentPage = ctx.getCurrentPage();
		if (request.getSession().getAttribute(generateKey(currentPage.getPath(), "VOTED")) != null && !DEBUG) {
			return null;
		}
		RequestService requestService = RequestService.getInstance(request);
		String compId = requestService.getParameter("comp-id", null);
		if (compId != null) {
			PageRank comp = (PageRank) content.getComponent(ctx, compId);
			if (comp != null) {
				boolean positive = true;
				if (requestService.getParameter("positive", null) != null) {
					comp.setRankValue(ctx, comp.getRankValue(ctx, currentPage.getPath()) + 1);
					comp.setVotes(ctx, comp.getVotes(ctx, currentPage.getPath()) + 1);
				} else if (requestService.getParameter("negative", null) != null) {
					//comp.setRankValue(ctx, comp.getRankValue(ctx, currentPage.getPath()) - 1);
					comp.setVotes(ctx, comp.getVotes(ctx, currentPage.getPath()) + 1);
					positive = false;
				}
				logger.info("vote on '"+ctx.getCurrentPage().getName()+"' : rank value = "+comp.getRankValue(ctx, currentPage.getPath())+" / "+comp.getVotes(ctx, currentPage.getPath()));
				comp.getPage().releaseCache();
				currentPage.releaseCache();
				comp.storeViewData(ctx);
				ctx.getRequest().getSession().setAttribute(generateKey(currentPage.getPath(), "VOTED"), "true");
				ContentContext absoluteCtx = new ContentContext(ctx);
				absoluteCtx.setAbsoluteURL(true);

				StringWriter writer = new StringWriter();
				PrintWriter out = new PrintWriter(writer);
				GlobalContext globalContext = GlobalContext.getInstance(request);
				out.println("PageRank : " + globalContext.getContextKey());
				out.println("");
				out.println("page : " + URLHelper.createURL(absoluteCtx, currentPage.getPath()));
				out.println("postive : " + positive);
				out.println("rank : " + currentPage.getPageRank(ctx));
				out.println("");
				out.close();
				NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), "vote on " + globalContext.getContextKey(), writer.toString());
			}
		}
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "pagerank";
	}
	
	@Override
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception {
		return getViewXHTMLCode(ctx);
	}

}
