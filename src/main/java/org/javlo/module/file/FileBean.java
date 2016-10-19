package org.javlo.module.file;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.bean.Link;
import org.javlo.component.core.IContentVisualComponent;
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
		private int order = 1;
		
		public FileBeanComparator(ContentContext inCtx, int inSort) {
			this(inCtx, inSort, false);
		}

		public FileBeanComparator(ContentContext inCtx, int inSort, boolean desc) {
			ctx = inCtx;
			sort = inSort;
			if (desc) {
				order = -1;
			}
		}

		@Override
		public int compare(FileBean file1, FileBean file2) {
			if (sort == 2) {
				return file1.getStaticInfo().getFile().getName().compareTo(file2.getStaticInfo().getFile().getName())*order;
			} else if (sort == 3) {
				return file1.getStaticInfo().getTitle(ctx).compareTo(file2.getStaticInfo().getTitle(ctx))*order;
			} else if (sort == 4) {
				return -file1.getStaticInfo().getCreationDate(ctx).compareTo(file2.getStaticInfo().getCreationDate(ctx))*order;
			} else {
				return -file1.getStaticInfo().getDate(ctx).compareTo(file2.getStaticInfo().getDate(ctx))*order;
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
				path = StringHelper.cleanPath(path);
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
	
	public boolean isEditable() {
		return StringHelper.isEditable(getName());
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
		if (StringHelper.isImage(getName()) || StringHelper.isPDF(getName())) {
			return URLHelper.createTransformURL(ctx, (staticInfo.isStaticFolder()?globalContext.getStaticConfig().getStaticFolder():"") + staticInfo.getStaticURL(), "list") + "?ts=" + staticInfo.getFile().lastModified();
		} else {
			return URLHelper.getFileTypeURL(ctx, staticInfo.getFile());
		}
	}

	public String getFreeURL() throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (StringHelper.isImage(staticInfo.getFile().getName())) {
			return URLHelper.createTransformURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), null, (staticInfo.isStaticFolder()?globalContext.getStaticConfig().getStaticFolder():"") + staticInfo.getStaticURL(), "free", "${info.templateName}");
		} else {
			return staticInfo.getURL(ctx);
			/*System.out.println("***** FileBean.getFreeURL : file = "+staticInfo.getFile()); //TODO: remove debug trace
			System.out.println("***** FileBean.getFreeURL : staticInfo.getURL(ctx) = "+staticInfo.getURL(ctx)); //TODO: remove debug trace
			System.out.println("***** FileBean.getFreeURL : URLHelper.createStaticURL(ctx, staticInfo.getURL(ctx)) = "+URLHelper.createStaticURL(ctx, staticInfo.getURL(ctx))); //TODO: remove debug trace
			return URLHelper.createStaticURL(ctx, staticInfo.getURL(ctx));*/
		}
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
	
	public boolean isFoundInContent() {
		try {
			return ResourceHelper.isComponentsUseResource(ctx, getURL());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public List<Link> getComponentWithReference() {
		try {
			List<Link> links = new LinkedList<>();
			ContentContext lgCtx = new ContentContext(ctx);
			for (IContentVisualComponent comp : ResourceHelper.getComponentsUseResource(ctx, staticInfo.getStaticURL())) {
				lgCtx.setAllLanguage(comp.getComponentBean().getLanguage());
				String url = URLHelper.createURL(lgCtx, comp.getPage());
				url = URLHelper.addParam(url, "pushcomp", comp.getId());
				url = URLHelper.addParam(url, "area", comp.getArea());
				url = URLHelper.addParam(url, "webaction", "changeArea");				
				url = URLHelper.addParam(url, "module", "content");				
				links.add(new Link(url, comp.getPage().getPath(), comp.getPage().getTitle(ctx)));
			}
			return links;
		} catch (Exception e) {
			e.printStackTrace();			
		}
		return Collections.EMPTY_LIST;
	}

}
