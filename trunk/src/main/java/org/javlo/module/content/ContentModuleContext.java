package org.javlo.module.content;

import java.util.List;

import org.javlo.bean.LinkToRenderer;
import org.javlo.module.core.AbstractModuleContext;

public class ContentModuleContext extends AbstractModuleContext {
	
	public static final int EDIT_MODE = 1;	
	public static final int PREVIEW_MODE = 2;
	public static final int PAGE_MODE = 3;
	
	private int mode = EDIT_MODE;
	
	/**
	 * get the mode of the content.
	 * <ul>
	 * <li>edit:edit the content</li>
	 * <li>preview:preview the content</li>
	 * </ul>
	 * @return a string with the mode as text.
	 */
	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public List<LinkToRenderer> getNavigation() {
		return null;
	}
	
	@Override
	public void init() {
	}

	@Override
	public LinkToRenderer getHomeLink() {		
		return null;
	}

	
}
