package org.javlo.module.content;

import javax.servlet.http.HttpSession;

public class ContentModuleContext {
	
	private static final String KEY = "moduleContext";
	
	public static final int EDIT_MODE = 1;	
	public static final int PREVIEW_MODE = 2;
	public static final int PAGE_MODE = 3;
	
	private int mode = EDIT_MODE;
	
	public static final ContentModuleContext getInstance(HttpSession session) {
		ContentModuleContext moduleContext = (ContentModuleContext)session.getAttribute(KEY);
		if (moduleContext == null) {
			moduleContext = new ContentModuleContext();
			session.setAttribute(KEY, moduleContext);
		}
		return moduleContext;
	}

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

}
