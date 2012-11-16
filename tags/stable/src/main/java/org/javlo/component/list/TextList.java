/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.list;

import java.util.List;

import org.javlo.component.text.Paragraph;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.utils.SuffixPrefix;


/**
 * @author pvandermaesen 
 */
public class TextList extends Paragraph {
	
	public static final String TYPE = "text-list";
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "";
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return "<div "+getSpecialPreviewCssClass(ctx, getStyle(ctx))+getSpecialPreviewCssId(ctx)+"\">"+StringHelper.textToList(getValue(), " ", null, true, globalContext)+"</div>";
	}
	
	@Override
	public boolean isInline() {
		return false;
	}
	
	@Override
	public List<SuffixPrefix> getMarkerList(ContentContext ctx) {
		return getQuotationLanguageMarkerList(ctx);
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return !isEmpty(ctx);
	}

}
