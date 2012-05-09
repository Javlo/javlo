package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;

public class FieldImage extends FieldFile {

	protected String getFilter() {
		return properties.getProperty("field." + getUnicName() + ".image.filter", "standard");
	}

	protected boolean isDisplayLabel() {
		return StringHelper.isTrue(properties.getProperty("field." + getUnicName() + ".image.label", "true"));
	}

	protected String getFileTypeFolder() {
		return getStaticConfig().getImageFolder();
	}

	@Override
	protected boolean isWithLink() {
		return true;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		
		if (!isViewDisplayed()) {
			return "";
		}
		
		if (getCurrentFile() == null || getCurrentFile().trim().length() == 0) {
			return "";
		}

		String relativePath = URLHelper.mergePath(getFileTypeFolder(), getCurrentFolder());
		if (getCurrentFile() != null) {
			String fileURL = URLHelper.mergePath(relativePath, getCurrentFile());

			String img;
			if (StringHelper.isImage(getCurrentFile())) {
				img = URLHelper.createTransformURL(ctx, '/' + fileURL, getFilter());
			} else {
				img = XHTMLHelper.getFileBigIcone(ctx, fileURL);
			}

			if (getCurrentLink() != null && getCurrentLink().trim().length() > 0) {
				out.println("<a href=\"" + getCurrentLink() + "\">");
			}
			out.println("<img src=\"" + img + "\" alt=\"" + getCurrentLabel() + "\"/>");
			if (isDisplayLabel()) {
				out.println("<span class=\"label\">" + getCurrentLabel() + "</span>");
			}
			if (getCurrentLink() != null && getCurrentLink().trim().length() > 0) {
				out.println("</a>");
			}
		}

		out.close();
		return writer.toString();
	}

	@Override
	public String getType() {
		return "image";
	}

}
