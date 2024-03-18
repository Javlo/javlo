package org.javlo.macro.core;

import org.javlo.context.ContentContext;

import java.util.Map;

public interface IMacro {

	public static final int TYPE_TOOLS = 10;

	public static final int TYPE_MODULE = 20;

	public static final String DEFAULT_MAX_MODAL_SIZE = "df";
	
	public static final String LARGE_MODAL_SIZE = "lg";
	
	public static final String MIDDEL_MODAL_SIZE = "md";
	
	public static final String SMALL_MODAL_SIZE = "sm";
	
	public static int DEFAULT_PRIORITY = 100;
	
	public String getName();

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception;

	/**
	 * macro for admin >> return true macro for contributor >> retrun false
	 * 
	 * @return
	 */
	public boolean isAdmin();

	/**
	 * is this macro can be execute in preview mode.
	 * 
	 * @return
	 */
	public boolean isPreview();
	
	public boolean isAdd();
	
	public boolean isInterative();
	
	/**
	 * is macro active in current context
	 * @return
	 */
	public boolean isActive();
	
	public void init(ContentContext ctx);
	
	public String getInfo(ContentContext ctx);
	
	public String getIcon();
	
	/**
	 * force url of the macro
	 * @return default null >> url will be generated for default macro modal
	 */
	public String getUrl();
	
	public int getPriority();

	public int getType();

	default public String getLabel() {
		String name = getName();
		if (name == null || name.length() < 3) {
			return name;
		}
		String label = name.replace("macro.", "").replace("-", " ");
		label = label.substring(0, 1).toUpperCase() + label.substring(1);
		return label;
	}
	
}
