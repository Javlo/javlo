package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.image.ImageBackground;
import org.javlo.component.image.ImageBean;
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

	protected String getIdBoxInputName() {
		return "id_" + getId();
	}

	protected String getLayoutBoxInputName() {
		return "layout_" + getId();
	}

	protected String getColorsBoxInputName() {
		return "colors_" + getId();
	}

	protected String getFooterBoxInputName() {
		return "footer_" + getId();
	}

	protected String getCSSClass(ContentContext ctx) {
		if (getComponentCssClass(ctx) == null || getComponentCssClass(ctx).trim().length() == 0) {
			return getType().toLowerCase();
		} else {
			return getType().toLowerCase() + " " + getComponentCssClass(ctx);
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
		ImageBackground bg = getBackgroundImage(ctx);
		if (bg != null) {
			ctx.getRequest().setAttribute("backgroundImage", new ImageBean(ctx, bg, "background"));
		} else {
			ctx.getRequest().setAttribute("backgroundImage", null);
		}
	}

	private ImageBackground getBackgroundImage(ContentContext ctx) {
		if (isOpen(ctx)) {
			IContentVisualComponent comp = getNextComponent();
			while (comp != null && !comp.getType().equals(getType())) {
				if (comp.getType().equals(ImageBackground.TYPE)) {
					ImageBackground bg = (ImageBackground) comp;
					if (bg.isForContainer()) {
						return bg;
					}
				}
				comp = comp.getNextComponent();
			}
		}
		return null;
	}

	@Override
	public Box getOpenComponent(ContentContext ctx) {
		int depth = 0;
		if (!isCloseBox()) {
			return this;
		} else {
			IContentVisualComponent comp = getPreviousComponent();
			IContentVisualComponent previousComp = null;
			while (comp != null && !(previousComp != null)) {
				if (comp.getType().equals(getType())) {
					if (!((IContainer) comp).isOpen(ctx)) {
						depth++;
					} else {
						if (depth == 0) {
							return (Box) comp;
						} else {
							depth--;
						}
					}
				}
				comp = comp.getPreviousComponent();
			}
		}
		return null;
	}

	@Override
	public Box getCloseComponent(ContentContext ctx) {
		int depth = 0;
		if (isCloseBox()) {
			return this;
		} else {
			IContentVisualComponent comp = getNextComponent();
			IContentVisualComponent nextComp = null;
			while (comp != null && !(nextComp != null)) {
				if (comp.getType().equals(getType())) {
					if (((IContainer) comp).isOpen(ctx)) {
						depth++;
					} else {
						if (depth == 0) {
							return (Box) comp;
						} else {
							depth--;
						}
					}
				}
				comp = comp.getNextComponent();
			}
		}
		return null;
	}

	public String getHtmlId() {
		if (!StringHelper.isEmpty(getManualId())) {
			return getManualId();
		} else {
			return getType() + getId();
		}
	}

	protected Collection<String> getLayouts() {
		return Collections.EMPTY_LIST;
	}

	protected Collection<String> getColors() {
		return Collections.EMPTY_LIST;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		IContentVisualComponent prevComp = getOpenComponent(ctx);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		out.println("<div class=\"checkbox\">");

		if (getValue().trim().length() == 0) {
			setValue("true");
			if (prevComp != null) {
				setStyle(ctx, prevComp.getComponentCssClass(ctx));
			}
			setModify();
		}
		out.println("<label>");
		if (isCloseBox()) {
			if (prevComp != null) {
				setStyle(ctx, prevComp.getComponentCssClass(ctx));
			}
			out.println("<input type=\"checkbox\" name=\"" + getCloseBoxInputName() + "\" checked=\"checked\" />");
		} else {
			out.println("<input type=\"checkbox\" name=\"" + getCloseBoxInputName() + "\" />");
		}
		out.println(" close " + getType() + " ?</label></div>");

		if (!isCloseBox()) {
			if (getValue().trim().length() == 0) {
				setValue("true");
				if (prevComp != null) {
					setStyle(ctx, prevComp.getComponentCssClass(ctx));
				}
				setModify();
			}
			
			out.println("<div class=\"row\"><div class=\"col-md-6\">");
			
			out.println("<div class=\"form-group\">");
			out.println("<label for=\"" + getTitleBoxInputName() + "\">Title</label>");
			out.println("<input class=\"form-control\" type=\"text\" name=\"" + getTitleBoxInputName() + "\" value=\"" + XHTMLHelper.stringToAttribute(getTitle()) + "\" />");
			out.println("</div>");

			out.println("<div class=\"form-group\">");
			out.println("<label for=\"" + getIdBoxInputName() + "\">id</label>");
			out.println("<input class=\"form-control\" type=\"text\" name=\"" + getIdBoxInputName() + "\" value=\"" + XHTMLHelper.stringToAttribute(getManualId()) + "\" />");
			out.println("</div>");

			out.println("</div><div class=\"col-md-6\">");

			if (getLayouts().size() > 0) {
				out.println("<div class=\"form-group\">");
				out.println("<label for=\"" + getLayoutBoxInputName() + "\">parallax</label>");
				// out.println("<input class=\"form-control\" type=\"checkbox\" name=\"" +
				// getParallaxBoxInputName() + "\" "+(isParallax()?"checked=\"checked\"')":"")+"
				// />");
				out.println(XHTMLHelper.getInputOneSelect(getLayoutBoxInputName(), getLayouts(), getContainerLayout(), "form-control"));
				out.println("</div>");
			}

			if (getColors().size() > 0) {
				out.println("<div class=\"form-group\">");
				out.println("<label for=\"" + getColorsBoxInputName() + "\">color</label>");
				out.println(XHTMLHelper.getInputOneSelect(getColorsBoxInputName(), getColors(), getContainerColor(), "form-control"));
				out.println("</div>");
			}
			
			out.println("</div></div>");
		} else {
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
	//
	// @Override
	// protected String getColomnablePrefix(ContentContext ctx) {
	// if (!isCloseBox()) {
	// return super.getColomnablePrefix(ctx);
	// } else {
	// Box box = getOpenComponent(ctx);
	// if (box != null) {
	// return box.getColomnablePrefix(ctx);
	// }
	// }
	// return "";
	// }

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		if (!isCloseBox()) {
			return getColomnablePrefix(ctx);
		} else {
			return "";
		}
	}

	// @Override
	// public String getSuffixViewXHTMLCode(ContentContext ctx) {
	// if (isCloseBox()) {
	// Box parentBox = getOpenComponent(ctx);
	// if (parentBox != null) {
	// return parentBox.getColomnableSuffix(ctx);
	// }
	// }
	// return "";
	// }

	@Override
	protected String getColumn(ContentContext ctx) {
		if (isOpen(ctx)) {
			return super.getColumn(ctx);
		} else {
			return "";
		}
	}

	@Override
	protected String getForcedPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	protected String getForcedSuffixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	protected boolean isBoxCloseRow(ContentContext ctx) {
		int max = getColumnMaxSize(ctx);
		IContentVisualComponent next = getNextComponent();
		while (next != null && !next.getType().equals(getType())) {
			next = next.getNextComponent();
		}
		boolean close = false;
		/* auto */
		if (getColumnSize() == 0) {
			ctx.setColumnableSize(0, ctx.getColumnableDepth());
			return true;
		}
		ctx.setColumnableSize(ctx.getColumnableSize(ctx.getColumnableDepth()) + getColumnSize(), ctx.getColumnableDepth());
		if (next != null) {
			if (ctx.getColumnableSize(ctx.getColumnableDepth()) + next.getColumnSize() > max || next.getColumnSize() < 0 || !next.isColumnable(ctx)) {
				close = true;
				ctx.setColumnableSize(0, ctx.getColumnableDepth());
			}
		} else {
			close = true;
			ctx.setColumnableSize(0, ctx.getColumnableDepth());
		}
		return close;
	}

	// @Override
	// protected boolean isCloseRow(ContentContext ctx) {
	// if (!isCloseBox()) {
	// Box closeBox = getCloseComponent(ctx);
	// if (closeBox != null) {
	// return closeBox.isCloseRow(ctx);
	// }
	// }
	// return isBoxCloseRow(ctx);
	// }
	//
	// @Override
	// protected boolean isOpenRow(ContentContext ctx) {
	// if (isCloseBox()) {
	// Box openBox = getOpenComponent(ctx);
	// if (openBox != null) {
	// return openBox.isOpenRow(ctx);
	// }
	// }
	// return isBoxCloseRow(ctx);
	// }

	protected String getInternalPrefix(ContentContext ctx) {
		String parent = "";
		if (!StringHelper.isEmpty(getTitle())) {
			parent = "<h" + (ctx.getTitleDepth() + 1) + " class=\"title\">" + getTitle() + "</h" + (ctx.getTitleDepth() + 1) + ">";
		}
		return getConfig(ctx).getProperty("html.internal-prefix", "") + parent;
	}

	protected String getInternalSuffix(ContentContext ctx) {
		String parent = "";
		if (!StringHelper.isEmpty(getFooter())) {
			parent = "<div class=\"footer\">" + getFooter() + "</div>";
		}
		return parent + getConfig(ctx).getProperty("html.internal-suffix", "");
	}

	protected Stack<Character> getBoxStack(ContentContext ctx) {
		final String BOX_KEY = getType() + "_stack_char";
		Stack<Character> boxStack = (Stack<Character>) ctx.getRequest().getAttribute(BOX_KEY);
		if (boxStack == null) {
			boxStack = new Stack<Character>();
			ctx.getRequest().setAttribute(BOX_KEY, boxStack);
		}
		return boxStack;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		final String BOX_KEY = getType() + "_stack_int";
		if (!isCloseBox()) {
			String suffix = "";
			if (ctx.isEditPreview()) {
				Integer boxCounter = (Integer) ctx.getRequest().getAttribute(BOX_KEY);
				if (boxCounter == null) {
					boxCounter = 0;
					ctx.getRequest().setAttribute(BOX_KEY, boxCounter);
				} else {
					boxCounter++;
					ctx.getRequest().setAttribute(BOX_KEY, boxCounter);
				}
				getBoxStack(ctx).push(StringHelper.ALPHABET_UPPER_CASE.charAt(boxCounter % 26));
				suffix = "<div " + getPreviewAttributes(ctx) + ">[Open " + getType() + " - " + StringHelper.ALPHABET_UPPER_CASE.charAt(boxCounter % 26) + " - " + getStyle() + "]</div>";
			}
			return getOpenCode(ctx) + suffix + getInternalPrefix(ctx);
		} else {
			String prefix = "";
			if (ctx.isAsPreviewMode() && EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession()).isPreviewEditionMode()) {
				if (!getBoxStack(ctx).isEmpty()) {
					prefix = "<div " + getPreviewAttributes(ctx) + ">[Close " + getType() + " - " + getBoxStack(ctx).pop() + "]</div>";
				}
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

	public String getTitle() {
		if (getValue().contains(";")) {
			return StringHelper.stringToCollection(getValue(), ";").get(1);
		} else {
			return "";
		}
	}

	public String getContainerLayout() {
		List<String> values = StringHelper.stringToCollection(getValue(), ";");
		if (values.size() > 4) {
			return StringHelper.stringToCollection(getValue(), ";").get(4);
		} else {
			return "";
		}
	}
	
	public String getContainerColor() {
		List<String> values = StringHelper.stringToCollection(getValue(), ";");
		if (values.size() > 5) {
			return StringHelper.stringToCollection(getValue(), ";").get(5);
		} else {
			return "";
		}
	}

	protected String getManualId() {
		List<String> values = StringHelper.stringToCollection(getValue(), ";");
		if (values.size() > 3) {
			return StringHelper.stringToCollection(getValue(), ";").get(3);
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
		performColumnable(ctx);
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		boolean closeBox = requestService.getParameter(getCloseBoxInputName(), null) != null;
		String title = requestService.getParameter(getTitleBoxInputName(), "");
		String manualId = requestService.getParameter(getIdBoxInputName(), "");
		String footer = requestService.getParameter(getFooterBoxInputName(), "");
		String layout = requestService.getParameter(getLayoutBoxInputName(), "");
		String color = requestService.getParameter(getColorsBoxInputName(), "");
		if (closeBox) {
			title = getTitle();
		} else {
			footer = getFooter();
		}
		String newValue = StringHelper.collectionToString(Arrays.asList(new String[] { "" + closeBox, title, footer, manualId, layout, color }), ";");
		if (!newValue.equals(getValue())) {
			setValue(newValue);
			setModify();
			setNeedRefresh(true);
		}
		return null;
	}

	@Override
	public void setOpen(ContentContext ctx, boolean open) {
		String newValue = StringHelper.collectionToString(Arrays.asList(new String[] { "" + !open, getTitle(), getFooter() }), ";");
		setValue(newValue);
	}

	@Override
	public String getOpenCode(ContentContext ctx) {
		return '<' + getTag() + " class=\"" + getCSSClass(ctx) + "\" style=\"" + getCSSStyle(ctx) + "\">";
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
	public boolean isDispayEmptyXHTMLCode(ContentContext ctx) throws Exception {
		return false;
	}

	@Override
	public String getFontAwesome() {
		return "square-o";
	}

	@Override
	protected boolean getColumnableDefaultValue() {
		return true;
	}

}
