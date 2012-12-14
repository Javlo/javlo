package org.javlo.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.macro.core.IMacro;
import org.javlo.macro.core.MacroFactory;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;

public class MacroHelper {

	public static String MACRO_DATE_KEY = "__macro_key__";

	public static final int CALENDAR_SHORT = 1; // user for jdk < 1.6

	public static final int CALENDAR_LONG = 2; // user for jdk < 1.6

	/**
	 * add content to a page
	 * 
	 * @param ctx
	 *            the current content context
	 * @param page
	 *            the page when the content must be insered
	 * @param parentComp
	 *            the parent component
	 * @param contentType
	 *            the type of the component
	 * @param value
	 *            the value of the component
	 * @return the if of the new component
	 * @throws Exception
	 */
	public static final String addContent(String lg, MenuElement page, String parentCompId, String contentType, String value) throws Exception {
		return addContent(lg, page, parentCompId, contentType, null, value);
	}

	/**
	 * add content to a page
	 * 
	 * @param page
	 *            the page when the content must be insered
	 * @param parentCompId
	 *            the parent component id
	 * @param contentType
	 *            the type of the component
	 * @param style
	 *            the style of the component
	 * @param value
	 *            the value of the component
	 * @return the if of the new component
	 * @throws Exception
	 */
	public static final String addContent(String lg, MenuElement page, String parentCompId, String contentType, String style, String value) throws Exception {
		return addContent(lg, page, parentCompId, contentType, style, null, value);
	}

	/**
	 * add content to a page
	 * 
	 * @param lg
	 * 
	 * @param page
	 *            the page when the content must be insered
	 * @param parentCompId
	 *            the parent component id
	 * @param contentType
	 *            the type of the component
	 * @param style
	 *            the style of the component
	 * @param area
	 *            the area of the component
	 * @param value
	 *            the value of the component
	 * @return the if of the new component
	 * @throws Exception
	 */
	public static final String addContent(String lg, MenuElement page, String parentCompId, String contentType, String style, String area, String value) throws Exception {
		ComponentBean comp = new ComponentBean(StringHelper.getRandomId(), contentType, value, lg, false);
		if (area != null) {
			comp.setArea(area);
		}
		if (style != null) {
			comp.setStyle(style);
		}
		page.addContent(parentCompId, comp);
		return comp.getId();
	}

	public static final String addContent(String lg, MenuElement page, String parentCompId, String contentType, String style, String area, String value, boolean inList) throws Exception {
		ComponentBean comp = new ComponentBean(StringHelper.getRandomId(), contentType, value, lg, false);
		comp.setList(inList);
		if (area != null) {
			comp.setArea(area);
		}
		if (style != null) {
			comp.setStyle(style);
		}
		page.addContent(parentCompId, comp);
		return comp.getId();
	}

	/**
	 * add content to a page
	 * 
	 * @param ctx
	 *            the current content context
	 * @param page
	 *            the page when the content must be insered
	 * @param parentComp
	 *            the parent component
	 * @param contentType
	 *            the type of the component
	 * @param value
	 *            the value of the component
	 * @return the if of the new component
	 * @throws Exception
	 */
	public static final String addContentIfNotExist(ContentContext ctx, MenuElement page, String parentCompId, String contentType, String value) throws Exception {
		return addContentIfNotExist(ctx, page, parentCompId, contentType, value, null);
	}

	/**
	 * add content to a page
	 * 
	 * @param ctx
	 *            the current content context
	 * @param page
	 *            the page when the content must be insered
	 * @param parentComp
	 *            the parent component
	 * @param contentType
	 *            the type of the component
	 * @param value
	 *            the value of the component
	 * @return the if of the new component
	 * @throws Exception
	 */
	public static final String addContentIfNotExist(ContentContext ctx, MenuElement page, String parentCompId, String contentType, String value, String style) throws Exception {
		ComponentBean newComp = new ComponentBean(StringHelper.getRandomId(), contentType, value, ctx.getContentLanguage(), false);
		if (style != null) {
			newComp.setStyle(style);
		}

		ContentElementList content = page.getContent(ctx);
		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (comp.getType().equals(newComp.getType())) {
				if (comp.getValue(ctx).equals(newComp.getValue())) {
					if (comp.getArea().equals(newComp.getArea())) {
						if (comp.getStyle(ctx).equals(newComp.getStyle())) {
							return comp.getId();
						}
					}
				}
			}
		}

		page.addContent(parentCompId, newComp);
		return newComp.getId();
	}

	/**
	 * insert a page in the navigation.
	 * 
	 * @parem ctx context
	 * @param parentName
	 *            the name of the parent page
	 * @param pagePrefix
	 *            the prefix of the new page (suffix in the number). sp. : prefix : news- page name : news-12
	 * @return the new page
	 * @throws Exception
	 */
	public synchronized static final MenuElement addPage(ContentContext ctx, String parentName, String pagePrefix, boolean top) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement nav = content.getNavigation(ctx);

		MenuElement newPage = nav.searchChildFromName(parentName);

		if (newPage != null) {
			Collection<MenuElement> allPages = newPage.getChildMenuElements();
			int maxNumber = 0;
			for (MenuElement menuElement : allPages) {
				String numberStr = menuElement.getName().substring(pagePrefix.length());
				try {
					int number = Integer.parseInt(numberStr);
					if (number > maxNumber) {
						maxNumber = number;
					}
				} catch (RuntimeException e) {
				}
			}
			maxNumber = maxNumber + 1;
			String pageName = pagePrefix + maxNumber;
			newPage = addPageIfNotExist(ctx, parentName, pageName, top);
		} else {
			String msg = "page not found : " + parentName;
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		}

		return newPage;
	}

	/**
	 * insert a page in the navigation if she does'nt exist.
	 * 
	 * @parem ctx context
	 * @param parentName
	 *            the name of the parent page
	 * @param pageName
	 *            the name of the new page
	 * @return the new page of the page with the same name
	 * @store store the result in the content repository if true.
	 * @throws Exception
	 */
	public static final MenuElement addPageIfNotExist(ContentContext ctx, MenuElement parentPage, String pageName, boolean top, boolean store) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement nav = content.getNavigation(ctx);

		MenuElement newPage = nav.searchChildFromName(pageName);
		if (newPage != null) {
			return newPage;
		}

		if (parentPage != null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			newPage = MenuElement.getInstance(globalContext);
			EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
			newPage.setName(pageName);
			newPage.setCreator(editCtx.getUserPrincipal().getName());
			if (top) {
				parentPage.addChildMenuElementOnTop(newPage);
			} else {
				parentPage.addChildMenuElementAutoPriority(newPage);
			}
			if (store) {
				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				persistenceService.store(ctx);
			}
			ctx.setPath(newPage.getPath());

			NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
			navigationService.clearAllPage();

			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String msg = i18nAccess.getText("action.add.new-page", new String[][] { { "path", pageName } });
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
		} else {
			String msg = "page not found.";
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		}

		return newPage;
	}

	/**
	 * insert a page in the navigation if she does'nt exist.
	 * 
	 * @parem ctx context
	 * @param parentName
	 *            the name of the parent page
	 * @param pageName
	 *            the name of the new page
	 * @return the new page of the page with the same name
	 * @throws Exception
	 */
	public static final MenuElement addPageIfNotExist(ContentContext ctx, String parentName, String pageName, boolean top) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement nav = content.getNavigation(ctx);

		MenuElement parentPage = nav.searchChildFromName(parentName);
		MenuElement newPage = nav.searchChildFromName(pageName);
		if (newPage != null) {
			return newPage;
		}

		if (parentPage != null) {
			return addPageIfNotExistWithoutMessage(ctx, parentPage, pageName, top);
		} else {
			String msg = "page not found : " + parentName;
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		}

		return newPage;
	}

	/**
	 * insert the page in the navigation if she does not exist and add not existing parent page too.
	 * 
	 * @param ctx
	 * @param parentPage
	 * @param subPage
	 * @param top
	 * @parem store store the data on the repository if true.
	 * @return
	 * @throws Exception
	 */
	public static final MenuElement addPageIfNotExistWithoutMessage(ContentContext ctx, MenuElement parentPage, MenuElement subPage, boolean top, boolean store) throws Exception {

		String parentPath = parentPage.getPath();
		String subPath = subPage.getPath();
		if (!subPath.startsWith(parentPath)) {
			throw new IllegalArgumentException("the subPage path must start with the parentPage path");
		}
		subPath = subPath.substring(parentPath.length());
		subPath = subPath.replaceFirst("^/+", "");
		String[] parts = subPath.split("/");
		for (String pageName : parts) {
			parentPage = addPageIfNotExist(ctx, parentPage, pageName, top, store);
		}

		return parentPage;
	}

	/**
	 * insert a page in the navigation if she does'nt exist.
	 * 
	 * @parem ctx context
	 * @param parentName
	 *            the name of the parent page
	 * @param pageName
	 *            the name of the new page
	 * @return the new page of the page with the same name
	 * @throws Exception
	 */
	public static final MenuElement addPageIfNotExistWithoutMessage(ContentContext ctx, MenuElement parentPage, String pageName, boolean top) throws Exception {

		if (pageName == null || pageName.trim().length() == 0) {
			throw new IllegalArgumentException("page name can not be null or empty");
		}

		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement nav = content.getNavigation(ctx);

		MenuElement newPage = nav.searchChildFromName(pageName);
		if (newPage != null) {
			return newPage;
		}

		if (parentPage != null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			newPage = MenuElement.getInstance(globalContext);
			EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
			newPage.setName(pageName);
			newPage.setCreator(editCtx.getUserPrincipal().getName());
			if (top) {
				parentPage.addChildMenuElementOnTop(newPage);
			} else {
				parentPage.addChildMenuElementAutoPriority(newPage);
			}
			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.store(ctx);
			ctx.setPath(newPage.getPath());
			NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
			navigationService.clearAllPage();
		} else {
		}

		return newPage;
	}

	/**
	 * Copy all component in the current language to the otherLanguageContexts BUT with an empty value.
	 * 
	 * @param currentPage
	 * @param ctx
	 * @param otherLanguageContexts
	 * @throws Exception
	 */
	public static void copyLanguageStructure(MenuElement currentPage, ContentContext ctx, List<ContentContext> otherLanguageContexts, boolean withContent) throws Exception {
		ContentContext ctxNoArea = new ContentContext(ctx);
		ctxNoArea.setArea(null);
		ContentElementList baseContent = currentPage.getLocalContentCopy(ctxNoArea);
		if (baseContent.hasNext(ctxNoArea)) {
			for (ContentContext lgCtx : otherLanguageContexts) {
				if (!currentPage.getLocalContentCopy(lgCtx).hasNext(lgCtx)) {
					String parentId = "0";
					baseContent.initialize(ctx);
					while (baseContent.hasNext(ctxNoArea)) {
						IContentVisualComponent comp = baseContent.next(ctxNoArea);
						String content = "";
						if (withContent) {
							content = comp.getValue(ctxNoArea);
						}
						parentId = addContent(lgCtx.getRequestContentLanguage(), currentPage, parentId, comp.getType(), comp.getStyle(ctxNoArea), comp.getArea(), content);
					}
				}
			}
		}
	}

	/**
	 * Copy the local content of the current language to <code>toPage</code>. Create the page or the parent page if they don't exists.
	 * 
	 * @param fromPage
	 * @param fromCtx
	 * @param toPage
	 * @param toCtx
	 * @throws Exception
	 */
	public static void copyLocalContent(MenuElement fromPage, ContentContext fromCtx, MenuElement toPage, ContentContext toCtx) throws Exception {
		ContentElementList sourceContent = fromPage.getLocalContentCopy(fromCtx);
		String parentCompId = "0";
		for (IContentVisualComponent component : sourceContent.asIterable(fromCtx)) {
			parentCompId = MacroHelper.addContent(toCtx.getRequestContentLanguage(), toPage, parentCompId, component.getType(), component.getStyle(fromCtx), component.getArea(), component.getValue(fromCtx));
		}
	}

	/**
	 * create all pages of a path or return the existing page.
	 * 
	 * @param ctx
	 *            current content
	 * @param path
	 *            the new path.
	 * @return a page.
	 * @throws Exception
	 */
	public static MenuElement createPathIfNotExist(ContentContext ctx, String path) throws Exception {
		String[] pagesName = path.split("/");
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement parent = content.getNavigation(ctx);

		for (String pageName : pagesName) {
			if (pageName.trim().length() > 0) {
				parent = addPageIfNotExistWithoutMessage(ctx, parent, pageName, false);
			}
		}

		return parent;
	}

	/**
	 * Delete local content of the current language for the page specified.
	 * 
	 * @param currentPage
	 * @param ctx
	 * @throws Exception
	 */
	public static void deleteLocalContent(MenuElement currentPage, ContentContext ctx) throws Exception {
		ContentElementList content = currentPage.getLocalContentCopy(ctx);
		for (IContentVisualComponent component : content.asIterable(ctx)) {
			currentPage.removeContent(ctx, component.getId());
		}
	}

	public static Date getCurrentMacroDate(HttpSession session) {
		Date date = (Date) session.getAttribute(MACRO_DATE_KEY);
		if (date == null) {
			date = new Date();
		}
		return date;
	}

	/**
	 * code from JDK 1.7 for compatibility to for JDK < 1.6
	 */
	public static String getDisplayName(Calendar cal, int field, int style, Locale locale) {
		DateFormatSymbols symbols = new DateFormatSymbols(locale);
		String[] strings = getFieldStrings(field, style, symbols);
		if (strings != null) {
			int fieldValue = cal.get(field);
			if (fieldValue < strings.length) {
				return strings[fieldValue];
			}
		}
		return null;
	}

	/**
	 * code from JDK 1.7 for compatibility to for JDK < 1.6
	 */
	private static String[] getFieldStrings(int field, int style, DateFormatSymbols symbols) {
		String[] strings = null;
		switch (field) {
		case Calendar.ERA:
			strings = symbols.getEras();
			break;

		case Calendar.MONTH:
			strings = (style == CALENDAR_LONG) ? symbols.getMonths() : symbols.getShortMonths();
			break;

		case Calendar.DAY_OF_WEEK:
			strings = (style == CALENDAR_LONG) ? symbols.getWeekdays() : symbols.getShortWeekdays();
			break;

		case Calendar.AM_PM:
			strings = symbols.getAmPmStrings();
			break;
		}
		return strings;
	}

	public static final String getXHTMLMacroSelection(ContentContext ctx) throws FileNotFoundException, IOException {
		return getXHTMLMacroSelection(ctx, true);
	}

	/**
	 * add content to a page.
	 * 
	 * @param ctx
	 *            Current Context
	 * @param page
	 *            page with new content
	 * @param content
	 *            the content formated in a string.<br />
	 *            format: [TYPE]:content;[TYPE]:content.<br />
	 *            sample : title:first title;subtitle;paragraph:lorem
	 * @throws Exception
	 * 
	 */
	public static void insertContent(ContentContext ctx, MenuElement page, String content) throws Exception {
		String[] allContent = content.split(";");
		String contentId = "0";
		for (String item : allContent) {
			String[] splitItem = item.split(":");
			String type = splitItem[0].trim();
			String value = "";
			if (splitItem.length > 1) {
				value = splitItem[1].trim();
			}
			String area = ComponentBean.DEFAULT_AREA;
			if (type.contains("(")) {
				area = StringUtils.split(type, "(")[0];
				type = StringUtils.split(type, "(")[1];
				if (type.endsWith(")")) {
					type = type.substring(0, type.length() - 1);
				}
			}
			String style = null;
			if (type.contains("|")) {
				style = StringUtils.split(type, "|")[1];
				type = StringUtils.split(type, "|")[0];
			}
			contentId = addContent(ctx.getRequestContentLanguage(), page, contentId, type, style, area, value);
		}
	}

	public static final String getXHTMLMacroSelection(ContentContext ctx, boolean adminMode) throws FileNotFoundException, IOException {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<div class=\"macro-list\">");
		List<String> macroName = globalContext.getMacros();
		boolean macroFound = false;
		MacroFactory factory = MacroFactory.getInstance(StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext()));
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());
		for (String name : macroName) {
			IMacro macro = factory.getMacro(name);
			if (macro != null && !(macro instanceof IInteractiveMacro) && (adminMode || !macro.isAdmin())) {
				macroFound = true;
				out.println("<div class=\"macro\">");
				out.println("<form id=\"form-macro-" + name + "\" method=\"post\" action=\"" + URLHelper.createURL(ctx) + "\">");
				out.println("<input type=\"hidden\" name=\"webaction\" value=\"macro.executeMacro\" />");
				out.println("<input type=\"submit\" name=\"macro-" + name + "\" value=\"" + i18nAccess.getText("macro.name." + name, name) + "\" />");
				out.println("</form>");
				out.println("</div>");
			}
		}
		if (!macroFound) {
			try {
				out.println("<p>" + i18nAccess.getText("command.macro.not-found") + "</p>");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		out.println("</div>");
		out.close();
		return writer.toString();
	}

	public static final boolean isMacro(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		List<String> macroName = globalContext.getMacros();
		return macroName.size() > 0;
	}

	public static void setCurrentMacroDate(HttpSession session, Date date) {
		session.setAttribute(MACRO_DATE_KEY, date);
	}

	public static void main(String[] args) {
		String type = "sidebar(page-reference)";

		String area = ComponentBean.DEFAULT_AREA;
		if (type.contains("(")) {
			area = StringUtils.split(type, "(")[0];
			type = StringUtils.split(type, "(")[1];
			if (type.endsWith(")")) {
				type = type.substring(0, type.length() - 1);
			}
		}
		String style = null;
		if (type.contains("|")) {
			style = StringUtils.split(type, "|")[1];
			type = StringUtils.split(type, "|")[0];
		}

		System.out.println("***** MacroHelper.main : type = " + type); // TODO: remove debug trace
		System.out.println("***** MacroHelper.main : area = " + area); // TODO: remove debug trace
		System.out.println("***** MacroHelper.main : style = " + style); // TODO: remove debug trace
	}

}
