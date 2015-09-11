/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.links;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

/**
 * @author pvandermaesen
 */
public class AnchorComponent extends AbstractVisualComponent {

	@Override
	public String getType() {
		return "anchor";
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "<span id=\"" + getValue() + "\" " + getSpecialPreviewCssClass(ctx, getStyle(ctx)) + getSpecialPreviewCssId(ctx) + "></span>";
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	private int countAnchor(ContentContext ctx, MenuElement elem, String anchor) throws Exception {

		int count = 0;

		ContentElementList content = elem.getContent(ctx);

		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (comp.getType().equals("anchor")) {
				if ((getValue() != null) && (comp.getValue(ctx).equals(anchor))) {
					count++;
				}
			}
		}

		return count;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		String anchor = getValue();

		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement elem = content.getNavigation(ctx);

		int count = countAnchor(ctx, elem, anchor);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		if (count > 1) {
			setMessage(new GenericMessage(i18nAccess.getText("content.anchor.message.same-name", new String[][] { { "count", "" + count } }), GenericMessage.ERROR));
		}

		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());
		finalCode.append("<div id=\"" + getValue() + "\"><input style=\"width: 100%;\" type=\"text\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\" value=\"" + getValue() + "\"/></div>");
		return finalCode.toString();
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		String msg = super.performEdit(ctx);
		if (isModify()) {
			setNeedRefresh(true);
		}
		return msg;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

}
