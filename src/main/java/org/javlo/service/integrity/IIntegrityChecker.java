package org.javlo.service.integrity;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

public interface IIntegrityChecker {
	
	public static final String SUCCESS = "success";
	public static final String INFO = "info";
	public static final String WARNING = "warning";
	public static final String DANGER = "danger";
	
	public static final String[] LEVEL_LABEL = new String[] {SUCCESS, INFO, WARNING, DANGER};
	
	public static final int SUCCESS_LEVEL = 0;
	public static final int INFO_LEVEL = 1;
	public static final int WARNING_LEVEL = 2;
	public static final int DANGER_LEVEL = 3;
	
	/**
	 * check a integrity of the page.
	 * @param ctx
	 * @param page
	 * @return true if checker have found no problem.
	 * @throws Exception
	 */
	public boolean checkPage(ContentContext ctx, MenuElement page) throws Exception ;
	
	public String getErrorMessage(ContentContext ctx);
	
	/**
	 * the component where the problem found.
	 * @param ctx
	 * @return
	 */
	public String getComponentId(ContentContext ctx);
	
	public String getArea(ContentContext ctx);
	
	public int getErrorCount(ContentContext ctx);
	
	public String getLevelLabel(ContentContext ctx);
	
	public int getLevel(ContentContext ctx);
	
	public boolean isApplicableForMailing(ContentContext ctx);
}
