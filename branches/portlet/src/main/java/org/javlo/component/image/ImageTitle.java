/*
 * Created on 29-janv.-2004
 */
package org.javlo.component.image;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.ContentManager;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;


/**
 * @author pvandermaesen
 */
public class ImageTitle extends Image {

	static final String SUBTITLE_KEY = "subtitle-image";

	static final String MENU_LABEL_KEY = "menu-label";
	
	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "style1", "style2", "style3" };
	}
	
	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		String left = "style 1";
		String right = "style 2";
		String center = "style 3";
		return new String[] { left, right, center };
	}


	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "style";
	}

	@Override
	public String getType() {
		return "image-title";
	}

	@Override
	public boolean isLabel() {
		return true;
	}
	
	public String getTitle() {
		StringReader reader = new StringReader(getLabel());
		BufferedReader bufReader = new BufferedReader(reader);
		String out="";
		try {
			out = StringHelper.neverNull(bufReader.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return out;
	}

	public String getSubTitle() {
		StringReader reader = new StringReader(getLabel());
		BufferedReader bufReader = new BufferedReader(reader);
		String out="";
		try {
			bufReader.readLine();
			String readLine = bufReader.readLine();
			String br = "";
			while (readLine != null) {
				out = out+br+StringHelper.neverNull(readLine);
				br = "<br />";
				readLine = bufReader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return out;
	}
	
	@Override
	protected String getImageLabelTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.image-title.title");
	}

	public void setSubTitle(String subTitle) {
		properties.setProperty(SUBTITLE_KEY, subTitle);
	}

	public void setMenuLabel(String menuLabel) {
		properties.setProperty(MENU_LABEL_KEY, menuLabel);
	}

	public String getMenuLabel() {
		return properties.getProperty(MENU_LABEL_KEY, "");
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer res = new StringBuffer();
		if ((getValue() != null) && (getValue().trim().length() > 0)) {
			if ((getFileName() != null) && (getFileName().trim().length() > 0)) {
				StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
				String fileLink = URLHelper.mergePath(getDirSelected(), getFileName());
				String url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getImageFolder()+'/'+fileLink).replace('\\', '/');
				res.append("<h1 class=\""+getStyle(ctx)+"\" style=\"background-image: url('"+url+"')\">");
			} else {
				res.append("<h1>");
			}
				res.append(getTitle());
				String subTitle = getSubTitle();
				if (subTitle.trim().length() > 0) {
					res.append("<span class=\"description\">");
					res.append(subTitle);
					res.append("</span>");
				}
				res.append("</h1>");
		} else {
			res.append("&nbsp; <!--IMAGE NOT DEFINED--> ");
		}
		return res.toString();
	}

	String getLabelTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.image-title.title");
	}

	String getLabelSubTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.image-title.subtitle");
	}

	String getSubTitleXHTMLInputName() {
		return getId() + ID_SEPARATOR + "subtitle_name";
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		String subTitle = ContentManager.getParameterValue(ctx.getRequest(), getSubTitleXHTMLInputName(), null);
		if (subTitle != null) {
			if ((!subTitle.equals(getSubTitle()))) {
				setSubTitle(subTitle);
				setModify();
			}
			super.performEdit(ctx);
		}
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getTextLabel()
	 */
	@Override
	public String getTextTitle() {
		return getLabel();
	}

	@Override
	public String getHexColor() {
		return META_COLOR;
	}
	
	@Override
	public int getTitleLevel(ContentContext ctx) {
		return 1;
	}


}