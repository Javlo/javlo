/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.thread.AbstractThread;
import org.javlo.thread.ReadURLThread;
import org.javlo.thread.ThreadManager;

/**
 * @author pvandermaesen
 */
public class SmartExternalLinkImportation extends AbstractVisualComponent {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(SmartExternalLinkImportation.class.getName());

	protected static final String TITLE_KEY = "target.title";

	protected static final String DESCRIPTION_KEY = "target.description";

	protected static final String IMAGE_URI_KEY = "target.image-uri";

	protected static final String IMAGE_KEY = "target.image";

	protected static final String RESPONSE_KEY = "target.response";

	protected static final String VALID_CONNECTION_KEY = "target.valid";

	private static final String STYLE_PRIORITY = SmartExternalLink.STYLE_PRIORITY;

	private static final String STYLE_NORMAL = SmartExternalLink.STYLE_NORMAL;

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println(getSpecialInputTag());

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String title = i18nAccess.getText("component.link.link-list");
			out.println("<div class=\"edit\">");
			out.print("<label for=\"" + getContentName() + "\">" + title + "</label>");
			out.print(" : ");
			out.println(XHTMLHelper.getTextArea(getContentName(), getValue()));
			out.println("</div>");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return writer.toString();
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		return getStyleList(ctx);
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { STYLE_NORMAL, STYLE_PRIORITY };
	}

	@Override
	public String getType() {
		return "smart-external-link-importation";
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ThreadManager threadManager = ThreadManager.getInstance(ctx.getRequest().getSession().getServletContext());
		if (threadManager.countThread() == 0) {
			Calendar cal = Calendar.getInstance();
			cal.roll(Calendar.DAY_OF_YEAR, -7);
			Calendar pageModification = Calendar.getInstance();
			pageModification.setTime(getPage().getModificationDate());
			if (pageModification.before(cal)) {
				importLinks(ctx);
			}
		}
		return "";
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		return 0;
	}

	private void importLinks(ContentContext ctx) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();

		ContentContext PreviewCtx = ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE);

		ComponentBean[] components = currentPage.getContent();
		ComponentBean comp = null;
		String parentId = "0";
		// the next SmartExternalLink is insered just before the first
		// SmartExternalLink, at the end of list of comp if not found.

		boolean highPriority = STYLE_PRIORITY.equals(getStyle(ctx));

		if (components.length > 0) {
			comp = components[0];
			for (int i = 0; (i < components.length) && ((!comp.getType().equals(SmartExternalLink.TYPE)) || (!highPriority && comp.getStyle().equals(SmartExternalLink.STYLE_PRIORITY))); i++) {
				parentId = comp.getId();
				comp = components[i];
			}
			if (!comp.getType().equals(SmartExternalLink.TYPE)) {
				parentId = comp.getId();
			}
			if (parentId.equals("0") && comp != null) {
				parentId = comp.getId();
			}
		}

		int countImport = 0;
		if (isHTMLPage()) {
			String pageContent = getValue();
			List<URL> extLinks = NetHelper.getExternalLinks(pageContent);
			for (URL url : extLinks) {
				parentId = currentPage.prepareAddContent(ctx.getRequestContentLanguage(), parentId, SmartExternalLink.TYPE, getStyle(ctx), ComplexPropertiesLink.LINK_KEY + "=" + url, ctx.getCurrentEditUser());
				countImport++;
			}
		} else {
			StringReader stringReader = new StringReader(getValue());
			BufferedReader read = new BufferedReader(stringReader);
			String link = read.readLine();
			while ((link != null) && (link.trim().length() > 0)) {
				String pageContent;
				if (!link.startsWith("POST:")) {
					pageContent = NetHelper.readPageGet(new URL(link));
				} else {
					URL url = new URL(link.substring("POST:".length()));
					pageContent = NetHelper.readPage(url);
				}
				if (pageContent != null) {
				List<URL> extLinks = StringHelper.searchLinks(pageContent);
				logger.info("links found : "+extLinks.size());
				for (URL url : extLinks) {
					parentId = currentPage.prepareAddContent(ctx.getRequestContentLanguage(), parentId, SmartExternalLink.TYPE, getStyle(ctx), ComplexPropertiesLink.LINK_KEY + "=" + url, ctx.getCurrentEditUser());
				}				
				countImport++;
				} else {
					logger.warning("error read link : "+link);
				}
				link = read.readLine();
			}
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		globalContext.sendMailToAdministrator("Page updated : " + currentPage.getTitle(ctx), "import " + countImport + " link id : " + URLHelper.createAbsoluteViewURL(ctx, currentPage.getPath()));

		/** thread to read this page **/
		ContentContext absContext = new ContentContext(ctx);
		absContext.setAbsoluteURL(true);
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());

		ReadURLThread readURLThread = (ReadURLThread) AbstractThread.createInstance(staticConfig.getThreadFolder(), ReadURLThread.class);
		readURLThread.setData(new URL(URLHelper.createURL(absContext, absContext.getPath())), false, 1000 * 60 * 3); // read page in 3 minutes
		readURLThread.store();
		readURLThread = (ReadURLThread) AbstractThread.createInstance(staticConfig.getThreadFolder(), ReadURLThread.class);
		readURLThread.setData(new URL(URLHelper.createURL(absContext, absContext.getPath())), true, 1000 * 60 * 15); // read pictures in 15 minutes
		readURLThread.store();
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {

		/* set default cookies manager */
		// CookieHandler.setDefault(new CookieManager());
		super.init(bean, newContext);

	}

	/**
	 * if you put directly html code with same link this method must be return true. If this method return false, the content is a list of URL to page with final url in the targeted page.
	 * 
	 * @return
	 */
	protected boolean isHTMLPage() {
		return false;
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {

		super.performEdit(ctx);

		if (isModify()) {
			importLinks(ctx);
		}
	}

}
