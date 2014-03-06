package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;

public class FieldImage extends FieldFile {
	
	public class ImageBean extends FieldBean {

		public ImageBean(ContentContext ctx) {
			super(ctx);
		}
		
		public String getPreviewURL() {
			String relativePath = URLHelper.mergePath(FieldImage.this.getFileTypeFolder(), FieldImage.this.getCurrentFolder());
			String fileURL = URLHelper.mergePath(relativePath, FieldImage.this.getCurrentFile());
			try {
				return URLHelper.createTransformURL(ctx, '/' + fileURL, getFilter());
			} catch (Exception e) {			
				e.printStackTrace();
				return null;
			}
		}
		
		public String getLink() {
			return FieldImage.this.getCurrentLink();
		}
		
		public String getAlt() {
			return FieldImage.this.getCurrentLabel();
		}
		
	}

	protected String getFilter() {
		return properties.getProperty("field." + getUnicName() + ".image.filter", "standard");
	}

	protected boolean isDisplayLabel() {
		return StringHelper.isTrue(properties.getProperty("field." + getUnicName() + ".image.label", "true"));
	}

	@Override
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

		if (getCurrentFile() != null && getCurrentFile().trim().length() > 0) {
			if (getCurrentLink() != null && getCurrentLink().trim().length() > 0) {
				out.println("<a href=\"" + getCurrentLink() + "\">");
			}
			out.println("<img src=\"" + getPreviewURL(ctx) + "\" alt=\"" + getCurrentLabel() + "\"/>");
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
	
	public String getPreviewURL(ContentContext ctx) throws Exception {
		String fileURL = getFileURL(ctx);
		if (StringHelper.isImage(getCurrentFile())) {
			return URLHelper.createTransformURL(ctx, '/' + fileURL, getFilter());
		} else {
			return XHTMLHelper.getFileBigIcone(ctx, fileURL);
		}		
	}
	
	public String getFileURL(ContentContext ctx) {
		String relativePath = URLHelper.mergePath(getFileTypeFolder(), getCurrentFolder());
		return URLHelper.mergePath(relativePath, getCurrentFile());
	}
	
	protected FieldBean newFieldBean(ContentContext ctx) {
		return new ImageBean(ctx);
	}
	
	@Override
	public String getType() {
		return "image";
	}

}
