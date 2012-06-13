package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;

public class Box extends AbstractVisualComponent implements IContainer {

	private static final String TYPE = "box";

	private String getCloseBoxInputName() {
		return "close_box_" + getId();
	}

	private String getCSSClass(ContentContext ctx) {
		if (getStyle(ctx) == null || getStyle(ctx).trim().length() == 0) {
			return "box";
		} else {
			return "box " + getStyle(ctx);
		}
	}
	
	@Override
	public String[] getStyleList(ContentContext ctx) {
		if (isOpen(ctx)) {
			return super.getStyleList(ctx);
		} else {
			return new String[0];
		}
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		IContentVisualComponent comp = getPreviousComponent();
		IContentVisualComponent prevComp = null;
		while (comp != null && !(prevComp != null)) {
			if (comp instanceof Box) {
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

	private String getTag() {
		return "div";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	public String getSufixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	protected String getInternalPrefix(ContentContext ctx) {
		return getConfig(ctx).getProperty("html.internal-prefix", "");
	}

	protected String getInternalSuffix(ContentContext ctx) {
		return getConfig(ctx).getProperty("html.internal-suffix", "");
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (!isCloseBox()) {
			return '<' + getTag() + " class=\"" + getCSSClass(ctx) + "\">" + getInternalPrefix(ctx);
		} else {
			return getInternalSuffix(ctx) + "</" + getTag() + '>';
		}
	}

	private boolean isCloseBox() {
		return StringHelper.isTrue(getValue());
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		boolean closeBox = requestService.getParameter(getCloseBoxInputName(), null) != null;
		if (closeBox ^ isCloseBox()) {
			setValue("" + closeBox);
			setModify();
			setNeedRefresh(true);
		}
	}

	@Override
	public String getOpenCode(ContentContext ctx) {
		if (isOpen(ctx)) {
			return OPEN_CONTAINER_CODE;
		} else {
			return "";
		}
	}

	@Override
	public String getCloseCode(ContentContext ctx) {
		if (isOpen(ctx)) {
			return "";
		} else {
			return CLOSE_CONTAINER_CODE;
		}
	}

	@Override
	public boolean isOpen(ContentContext ctx) {
		return !isCloseBox();
	}

}
