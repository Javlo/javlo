package org.javlo.ztatic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Set;

import org.javlo.context.ContentContext;
import org.javlo.data.taxonomy.ITaxonomyContainer;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.image.ImageSize;
import org.javlo.ztatic.StaticInfo.Position;
import org.owasp.encoder.Encode;

public class StaticInfoBean implements ITaxonomyContainer {
	private final ContentContext ctx;
	private final StaticInfo staticInfo;
	private final ImageSize imageSize;
	private final StaticInfoBean folder;
	private String key = null;

	public StaticInfoBean(ContentContext ctx, StaticInfo staticInfo) throws Exception {
		this.ctx = ctx;
		this.staticInfo = staticInfo;
		this.imageSize = staticInfo.getImageSize(ctx);
		if (staticInfo.getFile().isFile()) {
			this.folder = new StaticInfoBean(ctx, StaticInfo.getInstance(ctx, staticInfo.getFile().getParentFile()));
		} else {
			this.folder = null;
		}
	}

	public String getId() {
		return staticInfo.getId(ctx);
	}

	public String getTitle() {
		return staticInfo.getManualTitle(ctx);
	}

	public String getHtmlTitle() {
		return Encode.forHtmlAttribute(staticInfo.getManualTitle(ctx));
	}

	public String getDescription() {
		return staticInfo.getManualDescription(ctx);
	}

	public String getReference() {
		return staticInfo.getReference(ctx);
	}

	public String getLanguage() {
		return staticInfo.getLanguage(ctx);
	}

	public String getHtmlDescription() {
		return Encode.forHtmlAttribute(staticInfo.getManualDescription(ctx));
	}

	public String getCopyright() {
		return staticInfo.getCopyright(ctx);
	}

	public String getHtmlCopyright() {
		return Encode.forHtmlAttribute(staticInfo.getCopyright(ctx));
	}

	public String getLocation() {
		return staticInfo.getManualLocation(ctx);
	}

	public String getHtmlLocation() {
		return Encode.forHtmlAttribute(staticInfo.getManualLocation(ctx));
	}

	public String getAccessToken() {
		return staticInfo.getAccessToken(ctx);
	}

	public String getFullTitle() {
		try {
			return staticInfo.getFullTitle(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getHtmlFullTitle() {
		try {
			return Encode.forHtmlAttribute(staticInfo.getFullTitle(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getSortableDate() {
		return StringHelper.renderSortableTime(staticInfo.getDate(ctx));
	}

	public String getShortDate() {
		try {
			return StringHelper.renderShortDate(ctx, staticInfo.getDate(ctx));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getFullDate() {
		try {
			return StringHelper.renderFullDate(ctx, staticInfo.getDate(ctx));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public StaticInfo getStaticInfo() {
		return staticInfo;
	}

	/**
	 * get manual key (jstl)
	 * 
	 * @return
	 */
	public String getKey() {
		return key;
	}

	/**
	 * set manual key (jstl)
	 * 
	 * @param key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	public StaticInfoBean getFolder() {
		return folder;
	}

	public Position getPosition() {
		return staticInfo.getPosition(ctx);
	}

	public String getName() {
		return staticInfo.getFile().getName();
	}

	public String getURL() throws IOException {
		return URLHelper.createResourceURL(ctx, staticInfo.getFile());
	}

	public int getFocusZoneX() {
		return staticInfo.getFocusZoneX(ctx);
	}

	public int getFocusZoneY() {
		return staticInfo.getFocusZoneY(ctx);
	}

	public boolean isEmptyInfo() {
		if (!StringHelper.isEmpty(getTitle())) {
			return false;
		}
		if (!StringHelper.isEmpty(getDescription())) {
			return false;
		}
		if (!StringHelper.isEmpty(getCopyright())) {
			return false;
		}
		if (!StringHelper.isEmpty(getLocation())) {
			return false;
		}
		return true;
	}

	private String getAllInfo(boolean html) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		String sep = "";
		String baseSep = "<span class=\"sep\">-</span>";
		if (!html) {
			baseSep = " - ";
		}
		String title = getTitle();
		if (!StringHelper.isEmpty(title)) {
			if (html) {
				out.print("<span class=\"title\">" + Encode.forHtml(title) + "</span>");
			} else {
				out.print(title);
			}
			sep = baseSep;
		}
		Date date = staticInfo.getManualDate(ctx);
		if (date != null) {
			if (html) {
				out.print(sep + "<span class=\"date\">" + getShortDate() + "</span>");
			} else {
				out.print(sep + getShortDate());
			}
			sep = baseSep;
		}
		String description = getDescription();
		if (!StringHelper.isEmpty(description)) {
			if (html) {
				out.print(sep + "<span class=\"description\">" + Encode.forHtml(description) + "</span>");
			} else {
				out.print(sep + description);
			}
			sep = baseSep;
		}
		String location = getLocation();
		if (!StringHelper.isEmpty(location)) {
			if (html) {
				out.print(sep + "<span class=\"location\">" + Encode.forHtml(location) + "</span>");
			} else {
				out.print(sep + location);
			}
			sep = baseSep;
		}
		String copyright = getCopyright();
		if (!StringHelper.isEmpty(copyright)) {
			if (html) {
				out.print(sep + "<span class=\"copyright\">" + Encode.forHtml(copyright) + "</span>");
			} else {
				out.print(sep + copyright);
			}
			sep = baseSep;
		}
		out.println();
		out.close();
		String outStr = new String(outStream.toByteArray());
		if (!html) {
			outStr = Encode.forHtmlAttribute(outStr);
		}
		return outStr;
	}

	public String getAllInfoHtml() {
		return getAllInfo(true);
	}

	public String getAllInfoText() {
		return getAllInfo(false);
	}
	
	public ImageSize getImageSize() {
		return imageSize;
	}

	@Override
	public Set<String> getTaxonomy() {
		return staticInfo.getTaxonomy(ctx);
	}
}
