/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.LoremIpsumGenerator;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.module.file.FileAction;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.utils.SuffixPrefix;

/**
 * @author pvandermaesen
 */
public class WysiwygParagraph extends AbstractVisualComponent {

	public static final String TYPE = "wysiwyg-paragraph";
	
	protected String getEditorComplexity(ContentContext ctx) {
		return getConfig(ctx).getProperty("editor-complexity", "light");
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getDebugHeader(ctx));
		finalCode.append(getSpecialInputTag());
		finalCode.append("<textarea class=\"tinymce-light\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\"");
		finalCode.append(" rows=\"25\">");
		
		String hostPrefix = InfoBean.getCurrentInfoBean(ctx).getAbsoluteURLPrefix();		
		finalCode.append(getValue().replace("${info.hostURLPrefix}", hostPrefix));
		finalCode.append("</textarea>");
		
		Map<String, String> filesParams = new HashMap<String, String>();
		String path = FileAction.getPathPrefix(ctx);
		filesParams.put("path", path);
		filesParams.put("webaction", "changeRenderer");
		filesParams.put("page", "meta");
		filesParams.put("select", "_TYPE_");
		filesParams.put(ContentContext.PREVIEW_EDIT_PARAM, "true");
				
		String chooseImageURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);
		finalCode.append("<script type=\"text/javascript\">jQuery(document).ready(loadWysiwyg('#" + getContentName() + "','"+getEditorComplexity(ctx)+"','"+chooseImageURL+"'));</script>");
		return finalCode.toString();
	}

	@Override
	public List<SuffixPrefix> getMarkerList(ContentContext ctx) {
		return null;
	}

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
		ctx.getRequest().setAttribute("text",XHTMLHelper.autoLink(XHTMLHelper.replaceLinks(ctx,XHTMLHelper.replaceJSTLData(ctx, reverserLinkService.replaceLink(ctx, this, getValue()))),globalContext));
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		return (String)ctx.getRequest().getAttribute("text");
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return getValue().trim().length() > 0;
	}

	@Override
	public boolean initContent(ContentContext ctx) {
		if (isEditOnCreate(ctx)) {
			return false;
		}
		setValue("<p>"+LoremIpsumGenerator.getParagraph(120, true, true)+"</p>");
		setModify();
		return true;
	}
	
	/*@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		if (ctx.isAsViewMode() && getValue().contains("</p>")) {
			return "";
		} else {			
			return super.getPrefixViewXHTMLCode(ctx);
		}
	}
	
	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		if (ctx.isAsViewMode() && getValue().contains("</p>")) {
			return "";
		} else {			
			return super.getSuffixViewXHTMLCode(ctx);
		}
	}*/
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {	
		return true;
	}
	
	@Override
	public String performEdit(ContentContext ctx) throws Exception {	
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newContent = requestService.getParameter(getContentName(), null);		
		if (newContent != null) {
			if (!StringHelper.isTrue(getConfig(ctx).getProperty("html", null), false)) {
				newContent = XHTMLHelper.removeEscapeTag(newContent);
			}			
			if (!getValue().equals(newContent)) {
				if (StringHelper.isTrue(getConfig(ctx).getProperty("clean-html", "false"))) {
					newContent = XHTMLHelper.cleanHTML(newContent);					
				}
				setValue(newContent);				
				setModify();
			}
		}
		return null;
	}
	@Override
	public boolean isMirroredByDefault(ContentContext ctx) {	
		return true;
	}
	
}
