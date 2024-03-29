/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.VFSHelper;
import org.javlo.helper.XMLManipulationHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ReverseLinkService;

/**
 * @author pvandermaesen
 */
public class VFSFile extends AbstractFileComponent implements IReverseLinkComponent {
	
	public static final String TYPE = "vfs-file";

	@Override
	protected String getImageUploadTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-file.add");
	}

	@Override
	protected String getImageChangeTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-file.change");
	}

	@Override
	protected String getDeleteTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-file.delete-file");
	}

	@Override
	protected String getPreviewCode(ContentContext ctx) throws Exception {
		String dirFile = URLHelper.mergePath(getFileDirectory(ctx), getDirSelected(ctx));
		String fileName = StringHelper.createFileName(getFileName(ctx));
		File zipFile = new File(URLHelper.mergePath(dirFile, fileName));		 
		if (zipFile.exists()) {
			String url = URLHelper.createResourceURL(ctx, URLHelper.mergePath(getRelativeFileDirectory(ctx), getDirSelected(ctx), zipFile.getName()));
			return "<a href=\""+url+"\" target=\"_blank\">"+zipFile.getName()+" ("+StringHelper.renderSize(zipFile.length())+")</a>";
		} else {
			return "";
		}
	}

	@Override
	public boolean isWithDescription() {
		return false;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		String fileName = StringHelper.createFileName(getFileName(ctx));
		String fullRelativeFileName = URLHelper.mergePath(getDirSelected(ctx), fileName);
		String content = getHTMLContent(ctx);		
		String prefixLink = URLHelper.mergePath(getRelativeFileDirectory(ctx), fullRelativeFileName);
		content = content.replace("${vfs.url.root}",URLHelper.createVFSURL(ctx, prefixLink, ""));		
		String body = XMLManipulationHelper.getHTMLBody(content.toString());
		return XMLManipulationHelper.changeLink(body, URLHelper.createVFSURL(ctx, prefixLink, ""));
	}
	
	public File getFile(ContentContext ctx) {
		String dirFile = URLHelper.mergePath(getFileDirectory(ctx), getDirSelected(ctx));
		String fileName = StringHelper.createFileName(getFileName(ctx));
		return new File(URLHelper.mergePath(dirFile, fileName));
	}
	
	@Override
	public List<File> getFiles(ContentContext ctx) {
		List<File> files = new LinkedList<File>();
		File file = getFile(ctx);
		if (file != null) {
			files.add(file);
		}
		return files;
	}
	
	protected String getHTMLContent(ContentContext ctx) throws IOException {
		File internalFile = getFile(ctx);
		if (StringHelper.isHTML(internalFile.getName())) {
			return ResourceHelper.loadStringFromFile(internalFile); 
		} else {
			FileSystemManager fsManager = VFS.getManager();
			URL cleanPath = internalFile.toURI().toURL();
			FileObject file = fsManager.resolveFile(StringHelper.getFileExtension(internalFile.getName()) + ":" + cleanPath);
			file = file.resolveFile("/index.html");
			InputStream in = file.getContent().getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			String content;
			try {
				ResourceHelper.writeStreamToStream(in, out);
				content = new String(out.toByteArray(), getEncoding());
			} finally {
				ResourceHelper.closeResource(in);
				ResourceHelper.closeResource(out);
				VFSHelper.closeFileSystem(file);
				//VFSHelper.closeManager(fsManager);
			}
			return content;
		}
	}

	@Override
	public String getHeaderContent(ContentContext ctx) {
		File zipFile = getFile(ctx);
		String fullRelativeFileName = URLHelper.mergePath(getDirSelected(ctx), zipFile.getName());
		String outStr = null;
		try {
			String content = getHTMLContent(ctx);
			String header = XMLManipulationHelper.getHTMLCleanedHead(content);
			String prefixLink = URLHelper.mergePath(getRelativeFileDirectory(ctx), fullRelativeFileName);
			outStr = XMLManipulationHelper.changeLink(header, URLHelper.createVFSURL(ctx, prefixLink, ""));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return outStr;
	}

	@Override
	public String getFileDirectory(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), getRelativeFileDirectory(ctx));
		return folder;
	}

	@Override
	public String createFileURL(ContentContext ctx, String inURL) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String url = URLHelper.createStaticURL(ctx, staticConfig.getFileFolder() + '/' + inURL).replace('\\', '/');

		return url;
	}

	/*
	 * @see org.javlo.itf.IContentVisualComponent#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getHexColor() {
		return IContentVisualComponent.CONTAINER_COLOR;
	}

	@Override
	public boolean isListable() {
		return false;
	}

	@Override
	public boolean isReverseLink() {
		return false;
	}

	@Override
	public String getLinkText(ContentContext ctx) {
		return getLabel();
	}

	@Override
	protected boolean needEncoding() {
		return true;
	}

	@Override
	public String getLinkURL(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		if (staticConfig == null) {
			return "";
		}
		String url = URLHelper.mergePath(getDirSelected(ctx), getFileName(ctx));
		url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + url);
		return url;
	}

	@Override
	protected String getRelativeFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getVFSFolder();
	}

	/*@Override
	public Collection<String> getExternalResources(ContentContext ctx) {
		Collection<String> resources = new LinkedList<String>();
		resources.add("/js/global.js");
		resources.add("/js/shadowbox/src/adapter/shadowbox-base.js");
		resources.add("/js/shadowbox/src/shadowbox.js");
		resources.add("/js/shadowboxOptions.js");
		resources.add("/js/onLoadFunctions.js");
		return resources;

	}*/

	@Override
	public boolean isOnlyThisPage() {
		return properties.getProperty(REVERSE_LINK_KEY, "none").equals(ReverseLinkService.ONLY_THIS_PAGE);
	}
	
	@Override
	public boolean isOnlyPreviousComponent() {
		return properties.getProperty(REVERSE_LINK_KEY, "none").equals(ReverseLinkService.ONLY_PREVIOUS_COMPONENT);
	}	

	@Override
	public int getPopularity(ContentContext ctx) {
		return 0;
	}
	
	@Override
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception {
		if (getFileName(ctx) == null || getFileName(ctx).length() == 0) {
			return super.getEmptyXHTMLCode(ctx);
		} else {
			return getViewXHTMLCode(ctx);
		}
	}
	
	@Override
	public boolean isUploadOnDrop() {
		return false;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
	
	@Override
	protected String getMainFolder(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getVFSFolderName();
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {	
		return true;
	}

}
