package org.javlo.module.file;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.user.AdminUserSecurity;
import org.javlo.ztatic.StaticInfo;
import org.javlo.ztatic.StaticInfo.Position;

public class FileBean {

	public static class FileBeanComparator implements Comparator<FileBean> {

		private final ContentContext ctx;
		private final int sort;

		public FileBeanComparator(ContentContext inCtx, int inSort) {
			ctx = inCtx;
			sort = inSort;
		}

		@Override
		public int compare(FileBean file1, FileBean file2) {
			if (sort == 2) {
				return file1.getStaticInfo().getFile().getName().compareTo(file2.getStaticInfo().getFile().getName());
			} else if (sort == 3) {
				return file1.getStaticInfo().getTitle(ctx).compareTo(file2.getStaticInfo().getTitle(ctx));
			} else if (sort == 4) {
				return -file1.getStaticInfo().getCreationDate(ctx).compareTo(file2.getStaticInfo().getCreationDate(ctx));
			} else {
				return -file1.getStaticInfo().getDate(ctx).compareTo(file2.getStaticInfo().getDate(ctx));
			}
		}

	}

	ContentContext ctx;
	StaticInfo staticInfo;
	Map<String, String> tags;
	Map<String, String> readRoles;

	public FileBean(ContentContext ctx, File file) {
		this.ctx = ctx;
		try {
			this.staticInfo = StaticInfo.getInstance(ctx, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public FileBean(ContentContext ctx, StaticInfo staticInfo) {
		this.ctx = ctx;
		this.staticInfo = staticInfo;
	}

	public String getURL() {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (!isDirectory()) {
			return URLHelper.createResourceURL(ctx, '/' + (staticInfo.isStaticFolder()?globalContext.getStaticConfig().getStaticFolder():"") + staticInfo.getStaticURL());
		} else {
			String currentURL;
			try {
				currentURL = InfoBean.getCurrentInfoBean(ctx).getCurrentURL();
				String path = staticInfo.getStaticURL();
				if (AdminUserSecurity.getInstance().isGod(ctx.getCurrentEditUser())) {
					path = URLHelper.mergePath("/" + (staticInfo.isStaticFolder()?globalContext.getStaticConfig().getStaticFolder():""), staticInfo.getStaticURL());
				}
				return URLHelper.addParam(currentURL, "path", path);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public String getAbsoluteURL() {
		ContentContext ctx = this.ctx.getContextForAbsoluteURL();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (!isDirectory()) {
			return URLHelper.createResourceURL(ctx, '/' + (staticInfo.isStaticFolder()?globalContext.getStaticConfig().getStaticFolder():"") + staticInfo.getStaticURL());
		} else {
			String currentURL;
			try {
				currentURL = InfoBean.getCurrentInfoBean(ctx).getCurrentURL();
				String path = staticInfo.getStaticURL();
				if (AdminUserSecurity.getInstance().isGod(ctx.getCurrentEditUser())) {
					path = URLHelper.mergePath("/" + (staticInfo.isStaticFolder()?globalContext.getStaticConfig().getStaticFolder():""), staticInfo.getStaticURL());
				}
				return URLHelper.addParam(currentURL, "path", path);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public boolean isImage() {
		return StringHelper.isImage(getName());
	}
	
	public boolean isVideo() {
		return StringHelper.isVideo(getName());
	}
	
	public String getFileExtension() {
		return StringHelper.getFileExtension(getName()).toLowerCase();
	}

	public String getType() {
		if (isDirectory()) {
			return "directory";
		} else {
			return StringHelper.getFileExtension(getName()).toLowerCase();
		}
	}

	public String getThumbURL() throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return URLHelper.createTransformURL(ctx, (staticInfo.isStaticFolder()?globalContext.getStaticConfig().getStaticFolder():"") + staticInfo.getStaticURL(), "list") + "?ts=" + staticInfo.getFile().lastModified();
	}

	public String getFreeURL() throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return URLHelper.createTransformURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), null, (staticInfo.isStaticFolder()?globalContext.getStaticConfig().getStaticFolder():"") + staticInfo.getStaticURL(), "free", "${info.templateName}");
	}

	public StaticInfo getStaticInfo() {
		return staticInfo;
	}

	public String getName() {
		return staticInfo.getFile().getName();
	}

	public String getDescription() {
		return staticInfo.getDescription(ctx);
	}

	public String getLocation() {
		return staticInfo.getLocation(ctx);
	}

	public String getDate() {
		return StringHelper.renderTime(staticInfo.getDate(ctx));
	}

	public String getManualDate() {
		return StringHelper.renderTime(staticInfo.getManualDate(ctx));
	}
	
	public String getCreationDate() {
		return StringHelper.renderTime(staticInfo.getCreationDate(ctx));
	}

	public String getTitle() {
		return staticInfo.getTitle(ctx);
	}
	
	public String getCopyright() {
		return staticInfo.getCopyright(ctx);
	}


	public String getId() {
		return getName().replace('.', '_');
	}

	public int getFocusZoneX() {
		return staticInfo.getFocusZoneX(ctx);
	}

	public int getFocusZoneY() {
		return staticInfo.getFocusZoneY(ctx);
	}

	public String getSize() {
		return StringHelper.renderSize(staticInfo.getFile().length());
	}
	
	public Position getPosition() {
		return staticInfo.getPosition(ctx);
	}

	public String getManType() {
		return ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(getName()));
	}

	public Map<String, String> getTags() {
		if (tags == null) {
			tags = new HashMap<String, String>();
			for (String tag : staticInfo.getTags(ctx)) {
				tags.put(tag, tag);
			}
		}
		return tags;
	}
	
	public Map<String, String> getReadRoles() {
		if (readRoles == null) {
			readRoles = new HashMap<String, String>();
			for (String readRole : staticInfo.getReadRoles(ctx)) {
				readRoles.put(readRole, readRole);
			}
		}
		return readRoles;
	}

	public boolean isShared() {
		return staticInfo.isShared(ctx);
	}

	public boolean isDirectory() {
		return staticInfo.getFile().isDirectory();
	}

	public int getPopularity() {
		try {
			return staticInfo.getAccessFromSomeDays(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public String getPath() {
		return staticInfo.getStaticURL();
	}

}
