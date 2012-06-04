package org.javlo.module.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.ztatic.StaticInfo;

public class JavloELFile extends ELFile {

	private File file;
	private JavloELFile parent;

	public JavloELFile(ELVolume volume, File file, JavloELFile parent) {
		super(volume);		
		this.file = file;
		this.parent = parent;
	}

	public File getFile() {
		return file;
	}

	public List<ELFile> getChildren() {
		List<ELFile> children = new ArrayList<ELFile>();
		File[] array = file.listFiles();
		if (array != null) {
			for (File child : array) {
				children.add(new JavloELFile(getVolume(), child, this));
			}
		}
		return children;
	}
	
	@Override
	public boolean isRoot() {
		return false;
	}

	public JavloELFile getParentFile() {
		if (isRoot()) {
			return null;
		} else {
			return parent;
		}
	}
	
	public ContentContext getContentContext() {		
		return getParentFile().getContentContext();		
	}

	public String getURL() {
		if (getContentContext() != null) {
			try {
				StaticInfo info = StaticInfo.getInstance(getContentContext(), file);
				GlobalContext globalContext = GlobalContext.getInstance(getContentContext().getRequest());
				return URLHelper.createResourceURL(getContentContext(), '/'+globalContext.getStaticConfig().getStaticFolder()+info.getStaticURL());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public String getThumbnailURL() {
		if (getContentContext() != null && StringHelper.isImage(file.getName())) {
			try {
				StaticInfo info = StaticInfo.getInstance(getContentContext(), file);
				GlobalContext globalContext = GlobalContext.getInstance(getContentContext().getRequest());
				return URLHelper.createTransformURL(getContentContext(), globalContext.getStaticConfig().getStaticFolder()+info.getStaticURL(), "icone");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	

}