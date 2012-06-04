/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.image;

import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;

/**
 * @author pvandermaesen
 */
public abstract class FilterImage extends Image {

	protected abstract String getFilter(ContentContext ctx);

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "image-left", "image-right", "image-center" };
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		String left = "left";
		String right = "right";
		String center = "center";
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			left = i18n.getText("global.left");
			right = i18n.getText("global.right");
			center = i18n.getText("global.center");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[] { left, right, center };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "position";
	}

	@Override
	public String getCSSClassName(ContentContext ctx) {
		return getStyle(ctx);
	}
	
	/**
	 * true if we can click on image for open in big resolution
	 * 
	 * @return
	 */
	protected boolean isClickable() {
		return true;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer res = new StringBuffer();
		
		String label = getLabel();
		if (label.trim().length() == 0) {
			label = getDescription();
		}
		
		if (!isEmpty(ctx)) {
			String fileLink = URLHelper.mergePath(getDirSelected(), getFileName());
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			String thumbURL = URLHelper.createTransformURL(ctx, getPage(), staticConfig.getImageFolder() + '/' + fileLink, getFilter(ctx)).replace('\\', '/');
			String url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getImageFolder() + '/' + fileLink).replace('\\', '/');
			res.append("<div "+getSpecialPreviewCssClass(ctx, getCSSClassName(ctx) + " "+getFilter(ctx))+getSpecialPreviewCssId(ctx)+">");
			res.append("<div class=\"labeled-image\">");
			if (isClickable()) {
				res.append("<a href=\"");
				res.append(url);
				res.append("\">");
			}
			res.append("<img src=\"");
			res.append(thumbURL);
			res.append("\" title=\"");
			res.append(StringHelper.removeTag(label));
			res.append("\" alt=\"");
			res.append(StringHelper.removeTag(label));
			res.append("\" />");
			if (isClickable()) {
				res.append("</a>");
			}
			res.append("<div>" + StringHelper.CR2BR(label) + "</div>");
			res.append("</div></div>");
		} else {
			res.append("<!--IMAGE NOT DEFINED--> ");
		}
		return res.toString();
	}

}
