/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import java.util.HashMap;
import java.util.Map;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.module.file.FileAction;

/**
 * @author pvandermaesen
 */
public class PageCss extends AbstractVisualComponent {

	public static final String TYPE = "page-css";

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_NONE;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_ADMIN);
	}

	@Override
	public String getIcon() {
		return "bi bi-filetype-scss";
	}
	
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getDebugHeader(ctx));
		finalCode.append(getSpecialInputTag());
		finalCode.append("<textarea class=\"form-control text-editor\" id=\"" + getContentName() + "\" data-mode=\"text/x-scss\" data-ext=\"css\" name=\"" + getContentName() + "\"");
		finalCode.append(" rows=\"" + (countLine() + 1) + "\">");
		finalCode.append(StringHelper.indentScss(getValue()));
		finalCode.append("</textarea>");
		if (getEditorComplexity(ctx) != null) {
			Map<String, String> filesParams = new HashMap<String, String>();
			String path = FileAction.getPathPrefix(ctx);
			filesParams.put("path", path);
			filesParams.put("webaction", "changeRenderer");
			filesParams.put("page", "meta");
			filesParams.put("select", "_TYPE_");
			filesParams.put(ContentContext.PREVIEW_EDIT_PARAM, "true");

			String chooseImageURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);
			finalCode.append("<script type=\"text/javascript\">jQuery(document).ready(loadWysiwyg('#" + getContentName() + "','" + getEditorComplexity(ctx) + "','" + chooseImageURL + "'));</script>");
		}
		return finalCode.toString();
	}

}
