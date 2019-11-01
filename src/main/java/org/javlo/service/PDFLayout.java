package org.javlo.service;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.javlo.helper.StringHelper;
import org.javlo.utils.JSONMap;

public class PDFLayout {

	public static final String REQUEST_KEY = "jv_pdfLayout";

	public static final String KEY = "pdfLayout";

	public static final String LANDSCAPE = "landscape";
	public static final String PORTRAIT = "portrait";

	private String height = "27.67cm";
	private String width = "19cm";
	private String backgroundSize = width;
	private String pageSize = "A4";
	private String fontFamily = "Verdana, Helvetica, sans-serif";
	private String fontSize = "11px";
	private String marginTop = "1cm";
	private String marginBottom = "1cm";
	private String marginLeft = "1cm";
	private String marginRight = "1cm";
	private String orientation = PORTRAIT;

	private String backgroundImage = null;

	public static final PDFLayout getInstance(HttpServletRequest request) {
		PDFLayout outPDFLayout = (PDFLayout) request.getAttribute(KEY);
		if (outPDFLayout == null) {
			outPDFLayout = new PDFLayout();
			request.setAttribute(KEY, outPDFLayout);
		}
		return outPDFLayout;
	}

	public static final PDFLayout getInstance(String data) throws IllegalAccessException, InvocationTargetException {
		PDFLayout outPDFLayout = new PDFLayout();
		outPDFLayout.setValues(data);
		return outPDFLayout;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getWidth() {
		return width;
	}

	public String getContainerWidth() {
		String ext = null;
		if (getWidth().endsWith("px") && getMarginLeft().endsWith("px") && getMarginRight().endsWith("px")) {
			ext = "px";
		}
		if (getWidth().endsWith("%") && getMarginLeft().endsWith("%") && getMarginRight().endsWith("%")) {
			ext = "%";
		}
		if (getWidth().endsWith("cm") && getMarginLeft().endsWith("cm") && getMarginRight().endsWith("cm")){
			ext = "cm";
		}
		if (ext != null) {
			return (Integer.parseInt(getWidth().replace(ext, ""))-Integer.parseInt(getMarginLeft().replace(ext, "")))-Integer.parseInt(getMarginRight().replace(ext, ""))+ext;
		} else {
			return "auto";
		}
		
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getPageSize() {
		return pageSize;
	}

	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public String getBackgroundImage() {
		return backgroundImage;
	}

	public void setBackgroundImage(String backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	public String getMarginTop() {
		return marginTop;
	}

	public void setMarginTop(String marginTop) {
		this.marginTop = marginTop;
	}

	public String getMarginBottom() {
		return marginBottom;
	}

	public void setMarginBottom(String marginBottom) {
		this.marginBottom = marginBottom;
	}

	public String getMarginLeft() {
		return marginLeft;
	}

	public void setMarginLeft(String marginLeft) {
		this.marginLeft = marginLeft;
	}

	public String getMarginRight() {
		return marginRight;
	}

	public void setMarginRight(String marginRight) {
		this.marginRight = marginRight;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public static String getLandscape() {
		return LANDSCAPE;
	}

	public static String getPortrait() {
		return PORTRAIT;
	}

	public String store() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
	}

	public void setValues(String jsonString) throws IllegalAccessException, InvocationTargetException {
		if (!StringHelper.isEmpty(jsonString)) {
			JSONMap map = (JSONMap) JSONMap.parse(jsonString);
			BeanUtils.copyProperties(this, map);
		}
	}

	public String getBackgroundSize() {
		return backgroundSize;
	}

	public void setBackgroundSize(String backgroundSize) {
		this.backgroundSize = backgroundSize;
	}

}
