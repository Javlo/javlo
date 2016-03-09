package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Stack;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.service.RequestService;

public class Box extends AbstractVisualComponent implements IContainer {

	private static final String TYPE = "box";

	protected String getCloseBoxInputName() {
		return "close_box_" + getId();
	}

	protected String getTitleBoxInputName() {
		return "title_" + getId();
	}

	protected String getFooterBoxInputName() {
		return "footer_" + getId();
	}

	protected String getCSSClass(ContentContext ctx) {
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
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("closeBox", isCloseBox());
		ctx.getRequest().setAttribute("titleBox", getTitle());
		ctx.getRequest().setAttribute("footerBox", getFooter());
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

		out.println("<div class=\"checkbox\">");

		if (getValue().trim().length() == 0) {
			setValue("true");
			if (prevComp != null) {
				setStyle(ctx, prevComp.getStyle(ctx));
			}
			setModify();
		}
		out.println("<label>");
		if (isCloseBox()) {
			if (prevComp != null) {
				setStyle(ctx, prevComp.getStyle(ctx));
			}
			out.println("<input type=\"checkbox\" name=\"" + getCloseBoxInputName() + "\" checked=\"checked\" />");
		} else {
			out.println("<input type=\"checkbox\" name=\"" + getCloseBoxInputName() + "\" />");
		}
		out.println(" close box ?</label></div>");

		if (!isCloseBox()) {
			out.println("<div class=\"form-group\">");
			if (getValue().trim().length() == 0) {
				setValue("true");
				if (prevComp != null) {
					setStyle(ctx, prevComp.getStyle(ctx));
				}
				setModify();
			}
			out.println("<label for=\"" + getTitleBoxInputName() + "\">Title</label>");
			out.println("<input class=\"form-control\" type=\"text\" name=\"" + getTitleBoxInputName() + "\" value=\"" + XHTMLHelper.stringToAttribute(getTitle()) + "\" />");
			out.println("</div>");
			out.println("<div class=\"form-group\">");
			out.println("<label for=\"" + getFooterBoxInputName() + "\">Footer</label>");
			out.println("<input class=\"form-control\" type=\"text\" name=\"" + getFooterBoxInputName() + "\" value=\"" + XHTMLHelper.stringToAttribute(getFooter()) + "\" />");
			out.println("</div>");
		}

		out.close();
		return new String(outStream.toByteArray());
	}

	protected String getTag() {
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
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	protected String getInternalPrefix(ContentContext ctx) {
		return getConfig(ctx).getProperty("html.internal-prefix", "");
	}

	protected String getInternalSuffix(ContentContext ctx) {
		return getConfig(ctx).getProperty("html.internal-suffix", "");
	}
	
	protected Stack<Character> getBoxStack(ContentContext ctx) {
		final String BOX_KEY = "_box_stack";
		Stack<Character> boxStack = (Stack<Character>)ctx.getRequest().getAttribute(BOX_KEY);
		if (boxStack == null) {
			boxStack = new Stack<Character>();
			ctx.getRequest().setAttribute(BOX_KEY, boxStack);
		}
		return boxStack;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		final String BOX_KEY = "_box_counter";
		if (!isCloseBox()) {			
			String suffix = "";
			if (ctx.isAsPreviewMode() && EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession()).isEditPreview()) {				
				Integer boxCounter = (Integer)ctx.getRequest().getAttribute(BOX_KEY);
				if (boxCounter == null) {
					boxCounter = 0;
					ctx.getRequest().setAttribute(BOX_KEY, boxCounter);
				} else {
					boxCounter++;
					ctx.getRequest().setAttribute(BOX_KEY, boxCounter);
				}
				getBoxStack(ctx).push(StringHelper.ALPHABET_UPPER_CASE.charAt(boxCounter%26));
				suffix = "<div " + getPreviewAttributes(ctx) + ">[Open box - "+StringHelper.ALPHABET_UPPER_CASE.charAt(boxCounter%26)+"]</div>";
			}			
			return  getOpenCode(ctx) + suffix + getInternalPrefix(ctx);
		} else {
			String prefix = "";			
			if (ctx.isAsPreviewMode() && EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession()).isEditPreview()) {
				prefix = "<div " + getPreviewAttributes(ctx) + ">[Close box - "+getBoxStack(ctx).pop()+"]</div>";
			}
			return prefix + getInternalSuffix(ctx) + getCloseCode(ctx);
		}
	}

	protected boolean isCloseBox() {
		if (getValue().contains(";")) {
			return StringHelper.isTrue(StringHelper.stringToCollection(getValue(), ";").get(0));
		} else {
			return StringHelper.isTrue(getValue());
		}
	}

	protected String getTitle() {
		if (getValue().contains(";")) {
			return StringHelper.stringToCollection(getValue(), ";").get(1);
		} else {
			return "";
		}
	}

	protected String getFooter() {
		if (getValue().contains(";")) {
			return StringHelper.stringToCollection(getValue(), ";").get(2);
		} else {
			return "";
		}
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		boolean closeBox = requestService.getParameter(getCloseBoxInputName(), null) != null;
		String title = requestService.getParameter(getTitleBoxInputName(), "");
		String footer = requestService.getParameter(getFooterBoxInputName(), "");
		if (closeBox) {
			title = getTitle();
			footer = getFooter();
		}
		String newValue = StringHelper.collectionToString(Arrays.asList(new String[] { "" + closeBox, title, footer }), ";");
		if (!newValue.equals(getValue())) {
			setValue(newValue);
			setModify();
			setNeedRefresh(true);
		}
		return null;
	}

	@Override
	public void setOpen(ContentContext ctx, boolean open) {
		String newValue = StringHelper.collectionToString(Arrays.asList(new String[] { "" + open, getTitle(), getFooter() }), ";");
		setValue(newValue);
	}

	@Override
	public String getOpenCode(ContentContext ctx) {
		return '<' + getTag() + " class=\"" + getCSSClass(ctx) + "\">";
	}

	@Override
	public String getCloseCode(ContentContext ctx) {
		return "</" + getTag() + '>';
	}

	@Override
	public boolean isOpen(ContentContext ctx) {
		return !isCloseBox();
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(AbstractVisualComponent.COMPLEXITY_STANDARD);
	}

	@Override
	public String getHexColor() {
		return IContentVisualComponent.CONTAINER_COLOR;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isDispayEmptyXHTMLCode(ContentContext ctx) throws Exception {
		return false;
	}

}
