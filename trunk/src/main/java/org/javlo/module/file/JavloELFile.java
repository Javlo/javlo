package org.javlo.module.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.ztatic.StaticInfo;

public class JavloELFile extends ELFile {

	private File file;
	private ELFile parent;

	public JavloELFile(ELVolume volume, File file, ELFile parent) {
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
			return (JavloELFile) parent;
		}
	}

	public ContentContext getContentContext() {
		return getParentFile().getContentContext();
	}

	public StaticInfo getStaticInfo() throws Exception {
		return StaticInfo.getInstance(getContentContext(), file);
	}

	public String getURL() {
		if (getContentContext() != null) {
			try {
				StaticInfo info = StaticInfo.getInstance(getContentContext(), file);
				GlobalContext globalContext = GlobalContext.getSessionInstance(getContentContext().getRequest().getSession());				
				if (!ResourceHelper.isTemplateFile(globalContext, file)) {
					String url = URLHelper.createResourceURL(getContentContext(), '/' + globalContext.getStaticConfig().getStaticFolder() + info.getStaticURL());					
					return url;
				} else {					
					String url = URLHelper.createTemplateResourceURL(getContentContext(), '/' + globalContext.getStaticConfig().getStaticFolder() + info.getStaticURL());					
					return url;
				}
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
				GlobalContext globalContext = GlobalContext.getSessionInstance(getContentContext().getRequest().getSession());
				if (!ResourceHelper.isTemplateFile(globalContext, file)) {
					return URLHelper.createTransformURL(getContentContext(), globalContext.getStaticConfig().getStaticFolder() + info.getStaticURL(), "icone") + "?ts=" + file.lastModified();
				} else {
					String templateName = ResourceHelper.extractTemplateName(globalContext, file);
					Template template = TemplateFactory.getDiskTemplate(getContentContext().getRequest().getSession().getServletContext(), templateName);
					if (template != null) {						
						String url = URLHelper.createTransformStaticTemplateURL(getContentContext(), template, "template", info.getStaticURL().replaceFirst('/'+templateName, ""))+ "?ts=" + file.lastModified();
						return url;
					} else {
						return null;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}