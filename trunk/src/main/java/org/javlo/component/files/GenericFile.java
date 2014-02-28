/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.mailing.MailingAction;
import org.javlo.service.ReverseLinkService;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvandermaesen
 */
public class GenericFile extends AbstractFileComponent implements IReverseLinkComponent {

	public static final String TYPE = "file";

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
		String url = ElementaryURLHelper.mergePath(getDirSelected(), getFileName());
		url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + url);
		return url;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return super.getPrefixViewXHTMLCode(ctx) + "<div class=\"" + getType() + ' ' + StringHelper.neverNull(StringHelper.getFileExtension(getFileName())) + "\">";
	}

	@Override
	protected String getPreviewCode(ContentContext ctx) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		if ((getValue() != null) && (getValue().length() > 0)) {
			out.print(getViewXHTMLCode(ctx));
		} else {
			out.println("&nbsp;");
		}
		return res.toString();
	}

	@Override
	protected String getRelativeFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getFileFolder();
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		I18nAccess i18nAccess;
		try {
			i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return new String[] { "standard", i18nAccess.getText("global.hidden") };
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getStyleList(ctx);
	}

	public File getFile(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String fullName = ElementaryURLHelper.mergePath(getDirSelected(), getFileName());
		fullName = ElementaryURLHelper.mergePath(staticConfig.getFileFolder(), fullName);
		fullName = ElementaryURLHelper.mergePath(globalContext.getDataFolder(), fullName);
		return new File(fullName);
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "standard", HIDDEN };
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

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		if (getStyle(ctx).equals(HIDDEN)) {
			return "";
		}

		StringBuffer res = new StringBuffer();
		if ((getValue() != null) && (getValue().trim().length() > 0)) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());

			String url = ElementaryURLHelper.mergePath(getDirSelected(), getFileName());
			url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + url);

			String rel = "";			
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String openAsPopup = "";
			if (globalContext.isOpenFileAsPopup()) {
				openAsPopup = "target=\"_blank\" ";
			}

			res.append("<a " + getSpecialPreviewCssClass(ctx, getStyle(ctx)) + getSpecialPreviewCssId(ctx) + " " + rel + openAsPopup + "href=\"");
			if (ctx.getRenderMode() != ContentContext.PAGE_MODE) {
				res.append(StringHelper.toXMLAttribute(url));
			} else {
				ContentContext viewCtx = new ContentContext(ctx);
				viewCtx.setRenderMode(ContentContext.VIEW_MODE);
				res.append(StringHelper.toXMLAttribute(url) + "?" + MailingAction.MAILING_FEEDBACK_PARAM_NAME + "=##data##");
			}
			res.append("\">");
			/*
			 * if (XHTMLHelper.getFileIcone(ctx, getFileName()).length() > 0) { res.append(XHTMLHelper.getFileIcone(ctx, getFileName()) + "&nbsp;"); }
			 */
			if (getLabel().trim().length() == 0) {
				res.append(getFileName());
			} else {
				res.append(textToXHTML(getLabel()));
			}
			String fullName = ElementaryURLHelper.mergePath(getDirSelected(), getFileName());
			fullName = ElementaryURLHelper.mergePath(staticConfig.getFileFolder(), fullName);

			fullName = ElementaryURLHelper.mergePath(globalContext.getDataFolder(), fullName);
			res.append(" <span class=\"info\">(<span class=\"format\">" + StringHelper.getFileExtension(getFileName()) + "</span> <span class=\"size\">" + StringHelper.getFileSize(fullName) + "</span>)</span></a>");
			if ((getDescription().trim().length() > 0) && (ctx.getRenderMode() != ContentContext.EDIT_MODE)) { /*
																												 * not set description when EDIT_MODE ( see getPreviewCode ( ) method
																												 */
				res.append("<span class=\"description\">" + getDescription() + "</span>");
			}
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
		return true;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return getValue().trim().length() > 0;
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

}
