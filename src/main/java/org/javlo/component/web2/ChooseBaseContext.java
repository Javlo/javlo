package org.javlo.component.web2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextCreationBean;
import org.javlo.context.GlobalContextFactory;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.ztatic.StaticInfo;

public class ChooseBaseContext extends AbstractPropertiesComponent implements IAction {
	
	private static final String PREFIX = "prefix";
	private static final String SELECT = "select";
	private static final String PREVIEW = "preview";
	private static final String NEXT_PAGE = "next-page";
	
	public static final String TYPE = "choose-base-context";
	
	private static final List<String> FIELDS = new LinkedList(Arrays.asList(new String[] { PREFIX, SELECT, PREVIEW, NEXT_PAGE }));
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		String prefix = getFieldValue(PREFIX);
		Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(ctx.getRequest().getSession().getServletContext());
		out.println("<div class=\"row\">");
		ContentContext ctxGl = new ContentContext(ctx);
		ctxGl.setRenderMode(ContentContext.VIEW_MODE);
		int i=0;
		for (GlobalContext glCtx : allContext) {
			if (StringHelper.isEmpty(prefix) || glCtx.getContextKey().startsWith(prefix)) {
				ctxGl.setForceGlobalContext(glCtx);
				out.println("<div class=\"col-xl-4 col-12 col-md-6\"><div class=\"card\">");
				if (glCtx.isScreenshot(ctxGl)) {
					String url = glCtx.getScreenshortUrl(ctxGl);
					StaticInfo staticInfo = StaticInfo.getInstance(ctxGl, glCtx.getScreenshotFile(ctxGl));
					url = URLHelper.createTransformURL(ctxGl, glCtx.getScreenshotFile(ctxGl), "bloc-4-2");
					out.println("<div class=\"clickable-multimedia\">");
					out.println("<a rel=\"context\" data-initindex=\""+i+"\" data-caption=\"screenshot\" data-height=\""+staticInfo.getImageSize(ctxGl).getHeight()+"\" data-width=\""+staticInfo.getImageSize(ctx).getWidth()+"\" href=\""+glCtx.getScreenshortUrl(ctxGl)+"\">");
					out.println("<img class=\"card-img-top\" src=\""+url+"\" alt=\"screenshot\" />");
					out.println("</a>");
					out.println("</div>");
				}
				out.println("<div class=\"card-body\">");
				out.println("<h5 class=\"card-title\">"+glCtx.getGlobalTitle()+"</h5>");
				String previewURL = URLHelper.createURL(ctxGl, "/");
				out.println("<a href=\""+previewURL+"\" target=\"_blank\" class=\"btn btn-secondary btn-block\">"+getFieldValue(PREVIEW)+"</a>");
				String chooseUrl = URLHelper.createURL(ctx);
				chooseUrl = URLHelper.addParam(chooseUrl, "webaction", getActionGroupName()+".select");
				chooseUrl = URLHelper.addParam(chooseUrl, "context", glCtx.getContextKey());
				chooseUrl = URLHelper.addParam(chooseUrl, IContentVisualComponent.COMP_ID_REQUEST_PARAM, getId());
				GlobalContextCreationBean createBean = GlobalContextCreationBean.getInstance(ctx.getRequest().getSession());
				if (!StringHelper.isEmpty(createBean.getContextKey())) {
					out.println("<a href=\""+chooseUrl+"\" class=\"btn btn-primary btn-block\">"+getFieldValue(SELECT)+"</a>");
				}
				out.println("</div>");
				out.println("</div>");
				out.println("</div>");
				i++;
			}
		}
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_ADMIN;
	}
	
	public static String performSelect(RequestService rs, ContentContext ctx, ContentService content, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		GlobalContextCreationBean createBean = GlobalContextCreationBean.getInstance(ctx.getRequest().getSession());
		ChooseBaseContext comp = (ChooseBaseContext)ComponentHelper.getComponentFromRequest(ctx);
		String refContext = rs.getParameter("context");
		if (refContext == null) {
			return "context not found.";
		}
		if (StringHelper.isEmpty(createBean.getContextKey())) {
			return "bad wizard status, please back to the fist step.";
		} else {
			createBean.setReferenceContext(refContext);
			String nextPage;
			if (!StringHelper.isEmpty(comp.getFieldValue(NEXT_PAGE))) {
				MenuElement targetPage = content.getNavigation(ctx).searchChildFromName(comp.getFieldValue(NEXT_PAGE));
				if (targetPage == null) {
					return "page not found : "+comp.getFieldValue(NEXT_PAGE);
				}
				nextPage = targetPage.getPath();
			} else {
				nextPage = ctx.getCurrentPage().getNextBrother().getPath();
			}
			ctx.setPath(nextPage);
			return null;
		}
	}
	
}

