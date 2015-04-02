package org.javlo.component.title;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.ComponentLayout;
import org.javlo.component.core.ISubTitle;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class Heading extends AbstractPropertiesComponent implements ISubTitle {

	private static final String TYPE = "heading";

	private static final String DEPTH = "depth";
	private static final String TEXT = "text";
	private static final String LINK = "link";
	private static final List<String> FIELDS = new LinkedList<String>(Arrays.asList(new String[] { DEPTH, TEXT, LINK }));

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		out.println("<div class=\"form-group\"><label>" + i18nAccess.getText("content.heading.depth", "depth")+ "</label>");
		int depth = getDepth(ctx);
		for (int i = 1; i < 7; i++) {
			out.println("<label class=\"radio-inline\">");
			out.println("<input type=\"radio\" value=\""+i+"\" name=\""+getInputName(DEPTH)+"\""+(depth==i?" checked=\"checked\"":"")+"/>"+i+"</label>");
		}
		out.println("</select></div>");
		out.println("<div class=\"form-group\">");
		out.println("<label for=\"" + getInputName(TEXT) + "\">" + i18nAccess.getText("content.header.text", "text") + "</label>");
		out.println("<input class=\"form-control\" type=\"text\" id=\"" + getInputName(TEXT) + "\" name=\"" + getInputName(TEXT) + "\" value=\"" + getTextTitle(ctx) + "\" >");
		out.println("</div>");

		out.println("<div class=\"form-group\">");
		out.println("<label for=\"" + getInputName(LINK) + "\">" + i18nAccess.getText("content.header.link", "link") + "</label>");
		out.println("<input class=\"form-control\" type=\"text\" id=\"" + getInputName(LINK) + "\" name=\"" + getInputName(LINK) + "\" value=\"" + getFieldValue(LINK) + "\" >");
		out.println("</div>");

		out.close();
		return new String(outStream.toByteArray());
	}

	public int getDepth(ContentContext ctx) {
		String depthValue = getFieldValue(DEPTH); 
		if (depthValue == null || depthValue.length() != 1) {
			if (ctx != null) {
				try {
					if (ctx.getCurrentPage().isRealContent(ctx)) {
						setFieldValue(DEPTH, "2");
						return 2;
					} else {
						setFieldValue(DEPTH, "1");
						return 1;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return 0;
		} else {
			switch (depthValue.charAt(0)) {
			case '1':
				return 1;
			case '2':
				return 2;
			case '3':
				return 3;
			case '4':
				return 4;
			case '5':
				return 5;
			case '6':
				return 6;
			default:
				return 0;
			}
		}
	}

	@Override
	public String getTextTitle(ContentContext ctx) {
		return getFieldValue(TEXT);
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		String link = getFieldValue(LINK);
		String target = "";
		if (link != null && link.trim().length() > 0) {
			if (ctx.getGlobalContext().isOpenExternalLinkAsPopup(link)) {
				target = " target=\"_blank\"";
			}
			if (!link.contains("://")) {
				if (link.contains(".")) {
					link = "http://"+link;
				} else {
					ContentService content = ContentService.getInstance(ctx.getRequest());
					MenuElement targetPage = content.getNavigation(ctx).searchChildFromName(link);
					if (targetPage != null) {
						link = URLHelper.createURL(ctx, targetPage);
					}
				}
			}
		} else {
			link = null;
		}
		
		String style="";		
		if (link != null) {
			if (getBackgroundColor() != null && getBackgroundColor().length() > 2) {
				style = "border: 1px "+getBackgroundColor()+" solid; ";
			}
			if (getTextColor() != null && getTextColor().length() > 2) {
				style = style + "color:"+getTextColor()+";";
			}
			if (style.length()>0) {
				style=" style=\""+style+"\"";
			}
			return "<a"+style+" href=\""+link+"\""+target+">"+getTextTitle(ctx)+"</a>";
		} else {
			if (getBackgroundColor() != null && getBackgroundColor().length() > 2) {
				style = " style=\"border: 1px "+getBackgroundColor()+" solid; \"";
			}
			String tag = "span";
			if (ctx.getGlobalContext().isMailingPlatform()) {
				tag = "div";
			}
			return "<"+tag+" class=\"inside-wrapper\""+style+">"+getTextTitle(ctx)+"</"+tag+">";
		}
	}

	@Override
	protected String getTag(ContentContext ctx) {
		int depth = getDepth(ctx);
		if (depth == 0) {
			return "div";
		} else {
			return "h" + depth;
		}
	}

	@Override
	public boolean isListable() {
		return false;
	}

	@Override
	public int getSearchLevel() {
		if (getDepth(null) == 1) {
			return SEARCH_LEVEL_HIGH;
		} else {
			return SEARCH_LEVEL_MIDDLE;
		}
	}

	@Override
	public String getHexColor() {
		return META_COLOR;
	}
	
	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();
		if (getLayout() == null) {
			getComponentBean().setLayout(new ComponentLayout(""));
		}
	}

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		setFieldValue(TEXT, i18nAccess.getText("content.heading"));
		storeProperties();
		return true;
	}

	@Override
	public String getSubTitle(ContentContext ctx) {
		if (getDepth(ctx) != 1) {
			return getTextTitle(ctx);
		}
		return null;
	}

	@Override
	public int getSubTitleLevel(ContentContext ctx) {
		return getDepth(ctx);
	}
	
	@Override
	public int getLabelLevel(ContentContext ctx) {
		if (getDepth(ctx) == 1) { 
			return HIGH_LABEL_LEVEL;
		} else if (getDepth(ctx) > 1) { 
			return MIDDLE_LABEL_LEVEL-getDepth(ctx);
		} else {
			return 0;
		}
	}
	
	public String getXHTMLId(ContentContext ctx) {
		final String suffix = "_st_";
		if (ctx.getRequest().getAttribute(suffix + getId()) != null) {
			return (String) ctx.getRequest().getAttribute(suffix + getId());
		}
		String htmlID = StringHelper.createFileName(getTextTitle(ctx)).replace('-', '_');
		if (htmlID.trim().length() == 0) {
			htmlID = "empty";
		}
		htmlID = "H_" + htmlID;
		while (ctx.getRequest().getAttribute(suffix + htmlID) != null) {
			htmlID = htmlID + "_bis";
		}
		ctx.getRequest().setAttribute(suffix + htmlID, "");
		ctx.getRequest().setAttribute(suffix + getId(), htmlID);
		return htmlID;
	}
	
	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		String cssClass = "";
		if (isBackgroundColored()) {
			cssClass = cssClass + " "+COLORED_WRAPPER_CLASS;
		}
		if (EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession()).isEditPreview()) {
			return '<'+getTag(ctx)+' ' + getSpecialPreviewCssClass(ctx, cssClass) + getSpecialPreviewCssId(ctx) + " "+getInlineStyle(ctx)+">";
		} else {
			return '<'+getTag(ctx)+' ' + " id=\""+getXHTMLId(ctx)+"\" "+getInlineStyle(ctx)+">";
		}
	}
	
	protected String getInlineStyle(ContentContext ctx) {
		String inlineStyle = "";
		if (getBackgroundColor() != null && getBackgroundColor().length() > 2) {
			inlineStyle = " overflow: hidden; border: 1px "+getBackgroundColor()+" solid; background-color: " + getBackgroundColor() + ';';
		}
		if (getTextColor() != null && getTextColor().length() > 2) {
			inlineStyle = inlineStyle + " color: " + getTextColor() + ';';
		}
		if (getLayout() != null) {
			inlineStyle = inlineStyle + ' ' + getLayout().getStyle();
		}
		inlineStyle = inlineStyle.trim();
		if (inlineStyle.length() > 0) {
			inlineStyle = " style=\"" + inlineStyle + "\"";
		}
		return inlineStyle;
	}
	
	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "</"+getTag(ctx)+'>';
	}
	

}
