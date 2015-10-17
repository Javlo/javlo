package org.javlo.helper.importation;

import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.module.core.IMainModuleName;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;

public class ImportConfigBean {
	
	private boolean imagesAsGallery = false;
	
	private boolean imagesAsImages = false;
	
	private boolean createContentOnImportImage = false;
	
	private String area = ComponentBean.DEFAULT_AREA;
	
	private boolean beforeContent = true;

	private boolean sharedImportDocument = false;
	
	public ImportConfigBean(ContentContext ctx) throws ModuleException {
		if (ctx.getGlobalContext().getStaticConfig().isCreateContentOnImportImage()) {
			createContentOnImportImage = true; 
		} else {
			ModulesContext modulesContext = ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext());			
			if (modulesContext.isModule(IMainModuleName.SHARED_CONTENT)) {
				createContentOnImportImage = false;
			} else {
				createContentOnImportImage = true;
			}
		}		
		sharedImportDocument = ctx.getGlobalContext().getStaticConfig().isSharedImportDocument();
	}

	public boolean isImagesAsGallery() {
		return imagesAsGallery;
	}

	public void setImagesAsGallery(boolean imagesAsGallery) {
		this.imagesAsGallery = imagesAsGallery;
	}

	public boolean isImagesAsImages() {
		return imagesAsImages;
	}

	public void setImagesAsImages(boolean imagesAsImages) {
		this.imagesAsImages = imagesAsImages;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public boolean isBeforeContent() {
		return beforeContent;
	}
	
	public boolean isCreateContentOnImportImage() {
		return createContentOnImportImage;
	} 

	public void setBeforeContent(boolean beforeContent) {
		this.beforeContent = beforeContent;
	}
	
	public boolean isSharedImportDocument() {
		return sharedImportDocument ;
	}
	
}
