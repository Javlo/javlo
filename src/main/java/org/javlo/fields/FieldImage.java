package org.javlo.fields;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.module.file.FileBean;
import org.javlo.ztatic.StaticInfo;
import org.javlo.ztatic.StaticInfoBean;

public class FieldImage extends FieldFile {
	
	public class ImageBean extends FieldBean {
		
		private String filter = getFilter();

		public ImageBean(ContentContext ctx) {
			super(ctx);
		}
		
		public String getPreviewURL() {
			if ( FieldImage.this.getCurrentFile() == null || FieldImage.this.getCurrentFile().trim().length() == 0) {
				return null;
			}
			String relativePath = URLHelper.mergePath(FieldImage.this.getFileTypeFolder(), FieldImage.this.getCurrentFolder());
			String fileURL = URLHelper.mergePath(relativePath, FieldImage.this.getCurrentFile());
			try {
				return URLHelper.createTransformURL(ctx, '/' + fileURL, getImageFilter());
			} catch (Exception e) {			
				e.printStackTrace();
				return null;
			}
		}
		
		public String getResourceURL() {
			if ( FieldImage.this.getCurrentFile() == null || FieldImage.this.getCurrentFile().trim().length() == 0) {
				return null;
			}
			String relativePath = URLHelper.mergePath(FieldImage.this.getFileTypeFolder(), FieldImage.this.getCurrentFolder());
			String fileURL = URLHelper.mergePath(relativePath, FieldImage.this.getCurrentFile());
			try {
				return URLHelper.createResourceURL(ctx, '/' + fileURL);
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

		public String getImageFilter() {
			return filter;
		}

		public void setImageFilter(String filter) {
			this.filter = filter;
		}
		
		public String getViewXHTMLCode() throws Exception {
			return FieldImage.this.getViewXHTMLCode(ctx, getImageFilter());
		}
		
		public StaticInfo getStaticInfo() throws Exception {
			String relativePath = URLHelper.mergePath(FieldImage.this.getFileTypeFolder(), FieldImage.this.getCurrentFolder());
			String fileURL = URLHelper.mergePath(relativePath, FieldImage.this.getCurrentFile());
			File file = new File(URLHelper.mergePath(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), fileURL)));
			return StaticInfo.getInstance(ctx, file);
		}
		
		public StaticInfoBean getStaticInfoBean() throws Exception {
			String relativePath = URLHelper.mergePath(FieldImage.this.getFileTypeFolder(), FieldImage.this.getCurrentFolder());
			String fileURL = URLHelper.mergePath(relativePath, FieldImage.this.getCurrentFile());
			File file = new File(URLHelper.mergePath(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), fileURL)));
			return new StaticInfoBean (ctx, StaticInfo.getInstance(ctx, file));
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
		return !isLight();
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {		
		return getViewXHTMLCode(ctx, getFilter());
	}
	
	protected String getViewXHTMLCode(ContentContext ctx, String filter) throws Exception {
		
		String refCode = referenceViewCode(ctx);
		if (refCode != null) {
			return refCode;
		}
		
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
				String target="";
				if (ctx.getGlobalContext().isOpenExternalLinkAsPopup(getCurrentLink())) {
					target = " target=\"_blank\"";
				}
				out.println("<a href=\"" + getCurrentLink() + "\"" + target+'>');
			}
			
			out.println("<img src=\"" + getPreviewURL(ctx, filter) + "\" alt=\"" + getCurrentLabel() + "\"/>");
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
	
	public String getPreviewURL(ContentContext ctx, String filter) throws Exception {
		String fileURL = getFileURL(ctx);
		if (StringHelper.isImage(getCurrentFile())) {
			return URLHelper.createTransformURL(ctx, '/' + fileURL, filter);
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
	
	protected String getPreviewZoneId() {
		return "picture-zone-" + getId();
	}
	
	protected boolean isFromShared(ContentContext ctx, String filename) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return filename.startsWith(staticConfig.getShareDataFolderKey());
	}
	
	@Override
	protected String getRelativeFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getImageFolder();
	}
	
	@Override
	protected String getPreviewCode(ContentContext ctx, boolean title) throws Exception {
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		
		out.println("<div class=\"focus-zone\">");
		out.println("<div id=\"" + getPreviewZoneId() + "\" class=\"list-container\">");
		
		String relativePath = URLHelper.mergePath(getFileTypeFolder(), getCurrentFolder());		
		String fileURL = URLHelper.mergePath(relativePath, getCurrentFile());
		URLHelper.createTransformURL(ctx, '/' + fileURL, "icon");

		String url;
		FileBean file = new FileBean(ctx, getStaticInfo(ctx));
		if (getCurrentFile() != null && getCurrentFile().trim().length() > 0) {
			url = URLHelper.createTransformURL(ctx, '/' + fileURL, "list-sm");
			url = URLHelper.addParam(url, "hash", file.getVersionHash());
			if (isFromShared(ctx, fileURL)) {
				out.println("<img src=\"" + url + "\" />&nbsp;");
			} else if (!isFromShared(ctx, fileURL)) {				
				out.println("<div class=\"focus-image-wrapper\"><img src=\"" + url + "\" />");
				out.println("<div class=\"focus-point\">x</div>");
				out.println("<input class=\"posx\" type=\"hidden\" name=\"posx-" + file.getId() + "\" value=\"" + file.getFocusZoneX() + "\" />");
				out.println("<input class=\"posy\" type=\"hidden\" name=\"posy-" + file.getId() + "\" value=\"" + file.getFocusZoneY() + "\" />");
				out.println("<input class=\"path\" type=\"hidden\" name=\"image_path-" + file.getId() + "\" value=\"" + URLHelper.mergePath(getRelativeFileDirectory(ctx), getCurrentFolder()) + "\" /></div>&nbsp;");
				out.println("<script type=\"text/javascript\">initFocusPoint();</script>");
			}
		} 
		out.println("</div></div>");
		
	
	out.close();
	return new String(outStream.toByteArray());
	}

	
	@Override
	public String getType() {
		return "image";
	}

}
