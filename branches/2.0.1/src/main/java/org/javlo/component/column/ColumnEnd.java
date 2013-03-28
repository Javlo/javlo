package org.javlo.component.column;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

public class ColumnEnd extends AbstractVisualComponent {

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		return "&nbsp";
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {		
		ColumnContext context = ColumnContext.getInstance(ctx.getRequest());
		if (context.isOpen()) {
			context.setOpen(false);
			if (context.isWithTable()) {
				return "</td></tr></table>";
			} else {
				return "</div><div class=\"content_clear\">&nbsp;</div>";
			}
		} else {
			return "";
		}
		
	}

	public String getType() {
		return "column-end";
	}

}
