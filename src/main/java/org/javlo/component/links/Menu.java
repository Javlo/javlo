package org.javlo.component.links;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.MenuElement.PageBean;
import org.javlo.service.ContentService;

/**
 * component for generate a menu
 * <h4>JSTL variable : </h4>
 * <ul>
 * <li>{@link PageBean} page : root page of menu. See {@link #getRootPage}.</li>
 * <li>{@link Integer} start : first depth. See {@link #getStartLevel}</li>
 * <li>{@link Integer} end : last depth. See {@link #getEndLevel}</li>  
 * </ul>
 * @author Patrick Vandermaesen
 *
 */
public class Menu extends AbstractPropertiesComponent {

	public static final String TYPE = "menu";

	private static final String START_LEVEL = "start-level";
	private static final String END_LEVEL = "end-level";
	private static final String ROOT_PAGE = "root-page";

	private static final List<String> FIELDS = Arrays.asList(new String[] { START_LEVEL, END_LEVEL, ROOT_PAGE });

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}
	
	@Override
	public String getDefaultRenderer(ContentContext ctx) {
		return "default.jsp";
	}

	@Override
	public String getType() {
		return TYPE;
	}
	
	int getStartLevel() {
		int startLevel = 1;
		try {
			startLevel = Integer.parseInt(getFieldValue(START_LEVEL));
		} catch (NumberFormatException e) {
			setFieldValue(START_LEVEL, ""+startLevel );
		}
		return startLevel;
	}
	
	/**
	 * return of the root page setted by user.
	 * @return
	 */
	String getRootPage() {
		return getFieldValue(ROOT_PAGE, "");
	}
	
	int getEndLevel() {
		int endLevel = 999;
		try {
			endLevel = Integer.parseInt(getFieldValue(END_LEVEL));
		} catch (NumberFormatException e) {
			setFieldValue(END_LEVEL, ""+endLevel );
		}
		return endLevel;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement page = content.getNavigation(ctx);
		
		if (getRootPage() != null) {
			MenuElement rootPage = page.searchChildFromName(getRootPage());
			if (rootPage != null) {
				page = rootPage;
			}
		}
		
		int level = 0;
		int startLevel = getStartLevel();
		
		while (level > startLevel && page.getChildList().length > 0) {
			List<MenuElement> children = page.getChildMenuElementsList();
			for (MenuElement child : children) {
				if (child.isSelected(ctx)) {
					page = child;
					level++;
				}
			}
		}
		ctx.getRequest().setAttribute("page", page.getPageBean(ctx));
		ctx.getRequest().setAttribute("start", getStartLevel());
		ctx.getRequest().setAttribute("end", getEndLevel());
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println(super.getEditXHTMLCode(ctx));
		out.println(getSelectRendererXHTML(ctx));
		out.close();
		return new String(outStream.toByteArray());
	}
	
	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}	

}
