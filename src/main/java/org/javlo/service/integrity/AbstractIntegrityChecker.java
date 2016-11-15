package org.javlo.service.integrity;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.DebugHelper;
import org.javlo.service.ContentService;

public abstract class AbstractIntegrityChecker implements IIntegrityChecker {
	
	private String message = null;
	private String compId = null;
	private int errorCount = 0;
	private int level = WARNING_LEVEL;
	
	 
	
	@Override
	public String getErrorMessage(ContentContext ctx) {
		return message;
	}

	@Override
	public String getComponentId(ContentContext ctx) {
		return compId;
	}
	
	@Override
	public int getErrorCount(ContentContext ctx) {	
		return errorCount;
	}
	
	@Override
	public int getLevel(ContentContext ctx) {	
		return level;
	}
	
	@Override
	public String getLevelLabel(ContentContext ctx) {	
		return LEVEL_LABEL[getLevel(ctx)];
	}
	
	public void setErrorMessage(String message) {
		this.message = message;
	}
	
	public void setComponentId(String compId) {
		this.compId = compId;
	}
	
	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	@Override
	public boolean isApplicableForMailing(ContentContext ctx) {
		return true;
	}
	
	@Override
	public String getArea(ContentContext ctx) {
		String compId = getComponentId(ctx);
		if (compId != null) {
			try {
				IContentVisualComponent comp = ContentService.getInstance(ctx.getGlobalContext()).getComponent(ctx, compId);
				if (comp != null) {
					return comp.getArea();
				}
			} catch (Exception e) {
				e.printStackTrace();
			};
		}
		return null;
	}

}
