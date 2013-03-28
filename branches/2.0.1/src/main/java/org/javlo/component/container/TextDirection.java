package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class TextDirection extends Box {

	private static final String TYPE = "text-direction";

	@Override
	protected String getCSSClass(ContentContext ctx) {
		if (getStyle(ctx) == null || getStyle(ctx).trim().length() == 0) {
			return "text-direction";
		} else {
			return "text-direction " + getStyle(ctx);
		}
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "rtl", "ltr" };
	}

	@Override
	public String getStyle() {
		return StringHelper.neverNull(super.getStyle(), "rtl");
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		IContentVisualComponent comp = getPreviousComponent();
		IContentVisualComponent prevComp = null;
		while (comp != null && !(prevComp != null)) {
			if (comp instanceof TextDirection) {
				prevComp = comp;
			}
			comp = comp.getPreviousComponent();
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"line\">");

		if (getValue().trim().length() == 0) {
			setValue("true");
			if (prevComp != null) {
				setStyle(ctx, prevComp.getStyle(ctx));
			}
			setModify();
		}
		out.println("<label for=\"" + getCloseBoxInputName() + "\">close box ?</label>");
		if (isCloseBox()) {
			if (prevComp != null) {
				setStyle(ctx, prevComp.getStyle(ctx));
			}
			out.println("<input type=\"checkbox\" name=\"" + getCloseBoxInputName() + "\" checked=\"checked\" />");
		} else {
			out.println("<input type=\"checkbox\" name=\"" + getCloseBoxInputName() + "\" />");
		}

		out.println("</div>");

		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (!isCloseBox()) {
			return '<' + getTag() + " dir=\"" + getStyle() + "\" class=\"" + getCSSClass(ctx) + "\">" + getInternalPrefix(ctx);
		} else {
			return getInternalSuffix(ctx) + "</" + getTag() + '>';
		}
	}

}
