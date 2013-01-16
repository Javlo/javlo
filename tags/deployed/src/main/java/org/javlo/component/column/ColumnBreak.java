package org.javlo.component.column;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

public class ColumnBreak extends AbstractVisualComponent {

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		return "&nbsp";
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {		
		ColumnContext context = ColumnContext.getInstance(ctx.getRequest());
		if (context.isOpen()) {
			context.setCount(context.getCount()+1);			
			if (context.isWithTable()) {
				return "</td><td class=\"col"+(context.getCount()-1)+"\">";
			} else {
				return "</div><div class=\"col"+(context.getCount()-1)+"\">";
			}
		} else {
			context.setCount(2);
			context.setOpen(true);
			if (context.isWithTable()) {
				return "<table class=\"col\"><tr><td class=\"col1 first\">";
			} else {
				return "<div class=\"col1 first\">";
			}
		}
		
	}

	public String getType() {
		return "column-break";
	}

}
