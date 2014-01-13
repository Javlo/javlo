package org.javlo.component.image;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;

public class FreeImageWithVisibleLabel extends Image {

	@Override
	public String getType() {		
		return "free-image";
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer res = new StringBuffer();
		if ((getValue() != null) && (getValue().trim().length() > 0)) {
			String fileLink = URLHelper.mergePath(getDirSelected(), getFileName());
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			String url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getImageFolder()+'/'+fileLink).replace('\\', '/');
			res.append("<div class=\"" + getCSSClassName(ctx) + "\">");
			res.append("<div class=\"labeled-image\">");
			res.append("<img src=\"");
			res.append(url);
			res.append("\" title=\"");
			res.append(getLabel());
			res.append("\" alt=\"");
			res.append(getLabel());
			res.append("\" />");
			res.append("<div>"+getLabel()+"</div>");
			res.append("</div></div>");
		} else {
			res.append("&nbsp; <!--IMAGE NOT DEFINED--> ");
		}
		return res.toString();
	}
	
	

}
