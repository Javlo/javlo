package org.javlo.fields;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.ztatic.StaticInfo;

public class FieldSound extends FieldFile {
	
	public class SoundBean extends FieldBean {
		
		public SoundBean(ContentContext ctx) {
			super(ctx);
		}
		
		public String getResourceURL() {
			return FieldSound.this.getRessourceURL(ctx);
		}
		
		public String getLink() {
			return FieldSound.this.getCurrentLink();
		}
		
		public String getAlt() {
			return FieldSound.this.getCurrentLabel();
		}

		public String getViewXHTMLCode() throws Exception {
			return FieldSound.this.getViewListXHTMLCode(ctx);
		}
		
		public StaticInfo getStaticInfo() throws Exception {
			String relativePath = URLHelper.mergePath(FieldSound.this.getFileTypeFolder(), FieldSound.this.getCurrentFolder());
			String fileURL = URLHelper.mergePath(relativePath, FieldSound.this.getCurrentFile());
			File file = new File(URLHelper.mergePath(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), fileURL)));
			return StaticInfo.getInstance(ctx, file);
		}
		
	}

	protected String getFilter() {
		return properties.getProperty("field." + getUnicName() + ".image.filter", "standard");
	}
	
	protected boolean isDisplayLabel() {
		return StringHelper.isTrue(properties.getProperty("field." + getUnicName() + ".image.label", "true"));
	}

	@Override
	protected boolean isWithLink() {
		return !isLight();
	}
	
	@Override
	public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {
		String refCode = referenceViewCode(ctx);
		if (refCode != null) {
			return refCode;
		}		
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		String url = getRessourceURL(ctx);
		if (!StringHelper.isEmpty(url)) {						
			out.println("<audio controls><source src=\""+url+"\" preload=\"auto\" type=\""+ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(url))+"\" />Your browser does not support the audio element.</audio>");
		} else {
			out.println("&nbsp; <!--FILE NOT DEFINED--> ");
		}
		out.close();
		return writer.toString();
	}
	
	protected String getPreviewCode(ContentContext ctx, boolean title) throws Exception {
		if ((getValue() != null && getValue().trim().length() == 0) || getCurrentFile() == null || getCurrentFile().trim().length() == 0) {
			return "";
		}		
		return getDisplayValue(ctx, ctx.getLocale());
	}
	
	public String getFileURL(ContentContext ctx) {
		String relativePath = URLHelper.mergePath(getFileTypeFolder(), getCurrentFolder());
		return URLHelper.mergePath(relativePath, getCurrentFile());
	}
	
	protected FieldBean newFieldBean(ContentContext ctx) {
		return new SoundBean(ctx);
	}
	
	@Override
	public String getType() {
		return "sound";
	}

}
