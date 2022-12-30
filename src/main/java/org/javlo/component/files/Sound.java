/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvandermaesen
 */
public class Sound extends AbstractFileComponent implements IReverseLinkComponent {

	public static final String TYPE = "sound";

	private static final String HIDDEN = "hidden";

	@Override
	public String createFileURL(ContentContext ctx, String inURL) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String url = ElementaryURLHelper.createStaticURL(ctx, staticConfig.getFileFolder() + '/' + inURL).replace('\\', '/');

		return url;
	}

	@Override
	protected String getDeleteTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-file.delete-file");
	}

	@Override
	public String getFileDirectory(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = ElementaryURLHelper.mergePath(globalContext.getDataFolder(), getRelativeFileDirectory(ctx));
		return folder;
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	@Override
	protected String getImageChangeTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-file.change");
	}

	@Override
	protected String getImageUploadTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-file.add");
	}

	@Override
	public String getLinkText(ContentContext ctx) {
		return getLabel();
	}

	@Override
	public String getLinkURL(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		if (staticConfig == null) {
			return "";
		}
		String url = ElementaryURLHelper.mergePath(getDirSelected(ctx), getFileName(ctx));
		url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + url);
		return url;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return super.getPrefixViewXHTMLCode(ctx) + "<div class=\"" + getType() + ' ' + StringHelper.neverNull(StringHelper.getFileExtension(getFileName(ctx))) + "\">";
	}

	@Override
	protected String getRelativeFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getFileFolder();
	}
	
	private String getInputNamePageSummary() {
		return getInputName("page-summary");
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception { 
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		out.println("<div class=\"form-group\">");
		out.println("<label for=\""+getInputNamePageSummary()+"\">"+i18nAccess.getText("sound.summary")+"</label>");
		out.println("<input class=\"form-control\" type=\"text\" value=\""+getDefaultSummaryPage()+"\" name=\""+getInputNamePageSummary()+"\" />");
		out.println("</div>");
		out.println(super.getEditXHTMLCode(ctx));
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		if (super.getStyleList(ctx).length > 0) {
			return super.getStyleLabelList(ctx);
		} else {
			I18nAccess i18nAccess;
			try {
				i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				return new String[] { "standard", i18nAccess.getText("global.hidden") };
			} catch (Exception e) {
				e.printStackTrace();
			}
			return getStyleList(ctx);
		}
	}

	@Override
	public List<File> getFiles(ContentContext ctx) {
		List<File> files = new LinkedList<File>();
		File file = getFile(ctx);
		if (file != null) {
			files.add(file);
		}
		return files;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		if (super.getStyleList(ctx).length > 0) {
			return super.getStyleList(ctx);
		} else {
			return new String[] { "standard", HIDDEN };
		}
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "</div>" + super.getSuffixViewXHTMLCode(ctx);
	}

	/*
	 * @see org.javlo.itf.IContentVisualComponent#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getURL(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String fileLink = URLHelper.mergePath(getDirSelected(ctx), getFileName(ctx));
		return URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + fileLink).replace('\\', '/');
	}
	
	public static final String SESSION_SUMMARY_PAGE_KEY = "sound_summary_page";

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		
		String summaryPage = (String)ctx.getRequest().getSession().getAttribute(SESSION_SUMMARY_PAGE_KEY);
		if (summaryPage == null) {
			ctx.getRequest().getSession().setAttribute(SESSION_SUMMARY_PAGE_KEY, getDefaultSummaryPage());
			summaryPage = (String)ctx.getRequest().getSession().getAttribute(SESSION_SUMMARY_PAGE_KEY);
		}		
		
		if ((getValue() != null) && (getValue().trim().length() > 0)) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			String url = ElementaryURLHelper.mergePath(getDirSelected(ctx), getFileName(ctx));
			url = URLHelper.createMediaURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + url);
			ctx.getRequest().setAttribute("type", ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(url)));
			ctx.getRequest().setAttribute("url", url);
			ctx.getRequest().setAttribute("infoHTML", XHTMLHelper.renderStaticInfo(ctx, getStaticInfo(ctx)));
			RequestService rs = RequestService.getInstance(ctx.getRequest());
			String summary = rs.getParameter("summary");
			if (summary != null) {
				ContentService content = ContentService.getInstance(ctx.getRequest());
				MenuElement sumPage = content.getNavigation(ctx).searchChildFromName(summary);
				if (sumPage != null) {
					Map<String,String> params = new HashMap<String,String>();
					params.put("only-area", "content");
					ctx.getRequest().setAttribute("summaryUrl", URLHelper.createURL(ctx, sumPage, params));
				}
			}
		}
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		if (getComponentCssClass(ctx).equals(HIDDEN)) {
			return "";
		}
		StringBuffer res = new StringBuffer();
		if ((getValue() != null) && (getValue().trim().length() > 0)) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			String url = ElementaryURLHelper.mergePath(getDirSelected(ctx), getFileName(ctx));
			url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + url);
			StaticInfo info = getStaticInfo(ctx);
			res.append(XHTMLHelper.renderStaticInfo(ctx, info));
			res.append("<audio controls><source src=\""+url+"\" preload=\"auto\" type=\""+ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(url))+"\" />Your browser does not support the audio element.</audio>");
		} else {
			res.append("&nbsp; <!--FILE NOT DEFINED--> ");
		}
		return res.toString();
	}

	@Override
	public boolean isInline() {
		return true;
	}

	@Override
	public boolean isListable() {
		return true;
	}

	@Override
	public boolean isOnlyThisPage() {
		return properties.getProperty(REVERSE_LINK_KEY, "none").equals(ReverseLinkService.ONLY_THIS_PAGE);
	}

	@Override
	public boolean isOnlyPreviousComponent() {
		return properties.getProperty(REVERSE_LINK_KEY, "none").equals(ReverseLinkService.ONLY_PREVIOUS_COMPONENT);
	}

	@Override
	public boolean isReverseLink() {
		return super.isReverseLink();
	}

	@Override
	public boolean isWithDescription() {
		return false;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}

	@Override
	public int getPopularity(ContentContext ctx) {
		StaticInfo staticInfo;
		try {
			staticInfo = StaticInfo.getInstance(ctx, getFile(ctx));
			if (staticInfo != null) {
				return staticInfo.getAccessFromSomeDays(ctx);
			}
		} catch (Exception e) {
		}
		return 0;
	}

	@Override
	public String getListGroup() {
		return "link";
	}

	@Override
	public boolean isUploadOnDrop() {
		return true;
	}

	@Override
	public String getFirstPrefix(ContentContext ctx) {
		if (!isList(ctx)) {
			return getConfig(ctx).getProperty("prefix.first", "");
		} else {
			String cssClass = "";
			if (getComponentCssClass(ctx) != null && getComponentCssClass(ctx).trim().length() > 0) {
				cssClass = ' ' + getComponentCssClass(ctx);
			}
			if (getListClass(ctx) != null) {
				cssClass = cssClass + ' ' + getListClass(ctx);
			}
			return "<" + getListTag(ctx) + " class=\"" + getType() + cssClass + ' ' + getCurrentRenderer(ctx) + "\">";
		}
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
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
	
	@Override
	protected String getMainFolder(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getFileFolderName();
	}
	
	@Override
	public String getFontAwesome() {
		return "microphone";
	}
	
	public String getDefaultSummaryPage() {
		return properties.getProperty("default-summary-page");
	}
	
	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		String defaultSummary = rs.getParameter(getInputNamePageSummary());
		if (defaultSummary != null) {
			properties.setProperty("default-summary-page", defaultSummary);
			storeProperties();
			setModify();
		}
		return super.performEdit(ctx);
	}

}
