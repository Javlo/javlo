package org.javlo.module.file;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javlo.bean.Link;
import org.javlo.comparator.LanguageListSorter;
import org.javlo.comparator.LanguageListSorter.ILanguage;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.data.taxonomy.ITaxonomyContainer;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.user.AdminUserSecurity;
import org.javlo.ztatic.StaticInfo;
import org.javlo.ztatic.StaticInfo.Position;
import org.owasp.encoder.Encode;

public class FileBean implements ILanguage, ITaxonomyContainer {

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
	StaticInfo staticInfo = null;
	Map<String, String> tags;
	Map<String, String> readRoles;
	private List<FileBean> translation = Collections.emptyList();
	private String beanLanguage;

	public FileBean(ContentContext ctx, File file) {
		this.ctx = ctx;
		try {
			this.staticInfo = StaticInfo.getInstance(ctx, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public FileBean(ContentContext ctx, File file, String beanLanguage) {
		this.ctx = ctx;
		try {
			this.staticInfo = StaticInfo.getInstance(ctx, file);
			this.beanLanguage = beanLanguage;
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
		if (StringHelper.isImage(getName()) || StringHelper.isPDF(getName()) || StringHelper.getFileExtension(getName()).equalsIgnoreCase("mp4")) {
			String fileURL = URLHelper.createTransformURL(ctx, (staticInfo.isStaticFolder()?globalContext.getStaticConfig().getStaticFolder():"") + staticInfo.getStaticURL(), "list");
			fileURL = URLHelper.addParam(fileURL, "ts", ""+staticInfo.getFile().lastModified());
			return  fileURL;
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
		if (staticInfo == StaticInfo.EMPTY_INSTANCE) {
			return null;
		} else {
			return staticInfo;
		}
	}

	public String getName() {
		if (staticInfo == null) {
			return null;
		}
		return staticInfo.getFile().getName();
	}

	public String getDescription() {
		if (staticInfo == null) {
			return null;
		}
		return staticInfo.getManualDescription(ctx);
	}
	
	public String getReference() {
		if (staticInfo == null) {
			return null;
		}
		return staticInfo.getReference(ctx);
	}
	
	public String getLanguage() {
		if (staticInfo == null) {
			return null;
		}
		return staticInfo.getLanguage(ctx);
	}

	public String getLocation() {
		if (staticInfo == null) {
			return null;
		}
		return staticInfo.getLocation(ctx);
	}

	public String getDate() {
		if (staticInfo == null) {
			return null;
		}
		return StringHelper.renderTime(staticInfo.getDate(ctx));
	}

	public String getManualDate() {
		if (staticInfo == null) {
			return null;
		}
		return StringHelper.renderTime(staticInfo.getManualDate(ctx));
	}
	
	public String getCreationDate() {
		if (staticInfo == null) {
			return null;
		}
		return StringHelper.renderTime(staticInfo.getCreationDate(ctx));
	}

	public String getTitle() {
		if (staticInfo == null) {
			return null;
		}
		return staticInfo.getTitle(ctx);
	}
	
	public String getCopyright() {
		if (staticInfo == null) {
			return null;
		}
		return staticInfo.getCopyright(ctx);
	}

	public String getId() {
		if (getName() == null) {
			return null;
		}
		return Encode.forHtmlAttribute(getName().replace('.', '_'));
	}

	public int getFocusZoneX() {
		if (staticInfo == null) {
			return -1;
		}
		return staticInfo.getFocusZoneX(ctx);
	}

	public int getFocusZoneY() {
		if (staticInfo == null) {
			return -1;
		}
		return staticInfo.getFocusZoneY(ctx);
	}

	public String getSize() {
		if (staticInfo == null) {
			return null;
		}
		return StringHelper.renderSize(staticInfo.getFile().length());
	}
	
	public Position getPosition() {
		if (staticInfo == null) {
			return null;
		}
		return staticInfo.getPosition(ctx);
	}

	public String getManType() {
		return ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(getName()));
	}

	public Map<String, String> getTags() {
		if (staticInfo == null) {
			return null;
		}
		if (tags == null) {
			tags = new HashMap<String, String>();
			for (String tag : staticInfo.getTags(ctx)) {
				tags.put(tag, tag);
			}
		}
		return tags;
	}
	
	public Map<String, String> getReadRoles() {
		if (staticInfo == null) {
			return null;
		}
		if (readRoles == null) {
			readRoles = new HashMap<String, String>();
			for (String readRole : staticInfo.getReadRoles(ctx)) {
				readRoles.put(readRole, readRole);
			}
		}
		return readRoles;
	}

	public boolean isShared() {
		if (staticInfo == null) {
			return false;
		}
		return staticInfo.isShared(ctx);
	}

	public boolean isDirectory() {
		if (staticInfo == null) {
			return false;
		}
		return staticInfo.getFile().isDirectory();
	}

	public int getPopularity() {
		if (staticInfo == null) {
			return -1;
		}
		try {
			return staticInfo.getAccessFromSomeDays(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public String getPath() {
		if (staticInfo == null) {
			return null;
		}
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
		if (staticInfo == null) {
			return null;
		}
		try {
			List<Link> links = new LinkedList<Link>();
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
	
	public void addTranslation(FileBean fileBean) {
		if (staticInfo == null) {
			return;
		}
		if (!StringHelper.isEmpty(fileBean.getLanguage())) {
			if (translation == Collections.EMPTY_LIST) {
				translation = new LinkedList<FileBean>();
			}	
			translation.add(fileBean);
			if (fileBean.getLanguage().equals(getBeanLanguage())) {
			    staticInfo=fileBean.staticInfo;
			}
		}	
		if (staticInfo == null) {
			staticInfo = StaticInfo.EMPTY_INSTANCE;
		}
	}
	
	public List<FileBean> getTranslation() {
		LanguageListSorter.sort(ctx.getGlobalContext(), translation);
		return translation;
	}

	public String getBeanLanguage() {
		return beanLanguage;
	}

	public void setBeanLanguage(String beanLanguage) {
		this.beanLanguage = beanLanguage;
	}

	@Override
	public String getSortLanguage() {		
		return getBeanLanguage();
	}
	
	public String getVersionHash() {
		if (staticInfo != null) {
			return staticInfo.getVersionHash(ctx);
		} else {
			return null;
		}
	}
	
	public boolean isJpeg() {
		return StringHelper.isJpeg(getName());
	}
	
	public boolean isToJpeg() {
		if (staticInfo == null || staticInfo == StaticInfo.EMPTY_INSTANCE || staticInfo.getFile().isDirectory()) {
			return false;
		} else {
			return StringHelper.isPDF(getName()) || StringHelper.getFileExtension(getName()).equalsIgnoreCase("png");
		}
	}
	
	@Override
	public Set<String> getTaxonomy() {
		return staticInfo.getTaxonomy(ctx);
	}
	
	public String getTaxonomySelect() {
		try {
			return ctx.getGlobalContext().getAllTaxonomy(ctx).getSelectHtml("taxonomy-"+getId(), staticInfo.getTaxonomy(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
}
