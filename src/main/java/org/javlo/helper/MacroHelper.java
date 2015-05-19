package org.javlo.helper;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.meta.DateComponent;
import org.javlo.component.meta.EventDefinitionComponent;
import org.javlo.component.meta.Tags;
import org.javlo.component.meta.TimeRangeComponent;
import org.javlo.component.text.Description;
import org.javlo.component.text.Paragraph;
import org.javlo.component.title.SubTitle;
import org.javlo.component.title.Title;
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
import org.javlo.user.User;
import org.jfree.data.time.DateRange;

public class MacroHelper {

	public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MacroHelper.class.getName());

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
	public static final String addContent(String lg, MenuElement page, String parentCompId, String contentType, String value, User authors) throws Exception {
		return addContent(lg, page, parentCompId, contentType, null, value, authors);
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
	public static final String addContent(String lg, MenuElement page, String parentCompId, String contentType, String style, String value, User authors) throws Exception {
		return addContent(lg, page, parentCompId, contentType, style, null, value, authors);
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
	public static final String addContent(String lg, MenuElement page, String parentCompId, String contentType, String style, String area, String value, User authors) throws Exception {
		ComponentBean comp = new ComponentBean(StringHelper.getRandomId(), contentType, value, lg, false, authors);
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
	 * @param renderer
	 * 	          the renderer selection of the component
	 * @param area
	 *            the area of the component
	 * @param value
	 *            the value of the component
	 * @return the if of the new component
	 * @throws Exception
	 */
	public static final String addContent(String lg, MenuElement page, String parentCompId, String contentType, String style, String area, String renderer, String value, User authors) throws Exception {
		ComponentBean comp = new ComponentBean(StringHelper.getRandomId(), contentType, value, lg, false, authors);
		if (area != null) {
			comp.setArea(area);
		}
		if (style != null) {
			comp.setStyle(style);
		}
		comp.setRenderer(renderer);
		page.addContent(parentCompId, comp);
		return comp.getId();
	}

	public static final String addContent(String lg, MenuElement page, String parentCompId, String contentType, String style, String area, String value, boolean inList, User authors) throws Exception {
		ComponentBean comp = new ComponentBean(StringHelper.getRandomId(), contentType, value, lg, false, authors);
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
		ComponentBean newComp = new ComponentBean(StringHelper.getRandomId(), contentType, value, ctx.getContentLanguage(), false, ctx.getCurrentEditUser());
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
	 *            the prefix of the new page (suffix in the number). sp. :
	 *            prefix : news- page name : news-12
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

	public static final MenuElement addPageIfNotExist(ContentContext ctx, MenuElement parentPage, String pageName, boolean top, boolean store) throws Exception {
		return addPageIfNotExist(ctx, parentPage, pageName, top, store, true);
	}

	public static final MenuElement addPage(ContentContext ctx, MenuElement parentPage, String pageName, boolean top, boolean store) throws Exception {
		return addPageIfNotExist(ctx, parentPage, pageName, top, store, false);
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
	private static final MenuElement addPageIfNotExist(ContentContext ctx, MenuElement parentPage, String pageName, boolean top, boolean store, boolean returnPageIfFound) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement nav = content.getNavigation(ctx);

		MenuElement newPage = nav.searchChildFromName(pageName);
		if (newPage != null) {
			if (returnPageIfFound) {
				return newPage;
			} else {
				return null;
			}
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

			NavigationService navigationService = NavigationService.getInstance(globalContext);
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
	 * insert the page in the navigation if she does not exist and add not
	 * existing parent page too.
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
			persistenceService.setAskStore(true);
			ctx.setPath(newPage.getPath());
			NavigationService navigationService = NavigationService.getInstance(globalContext);
			navigationService.clearAllPage();
		} else {
		}

		return newPage;
	}

	/**
	 * Copy all component in the current language to the otherLanguageContexts
	 * BUT with an empty value.
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
						parentId = addContent(lgCtx.getRequestContentLanguage(), currentPage, parentId, comp.getType(), comp.getStyle(ctxNoArea), comp.getArea(), comp.getCurrentRenderer(ctx), content, ctx.getCurrentEditUser());
					}
				}
			}
		}
	}

	/**
	 * Copy the local content of the current language to <code>toPage</code>.
	 * Create the page or the parent page if they don't exists.
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
			parentCompId = MacroHelper.addContent(toCtx.getRequestContentLanguage(), toPage, parentCompId, component.getType(), component.getStyle(fromCtx), component.getArea(), component.getValue(fromCtx), fromCtx.getCurrentEditUser());
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
		return getXHTMLMacroSelection(ctx, true, true);
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
			contentId = addContent(ctx.getRequestContentLanguage(), page, contentId, type, style, area, value, ctx.getCurrentEditUser());
		}
	}

	public static final String getXHTMLMacroSelection(ContentContext ctx, boolean adminMode, boolean preview) throws FileNotFoundException, IOException {
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
			if (macro != null && (adminMode || !macro.isAdmin()) && (!preview || macro.isPreview())) {
				macroFound = true;
				out.println("<div class=\"macro\">");
				if (macro instanceof IInteractiveMacro) {
					String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE)) + "?module=macro&previewEdit=true&webaction=macro.executeInteractiveMacro&macro-" + name + '=' + name + "&macro=" + name;
					String js = "jQuery.colorbox({href : '" + url + "',opacity : 0.6,iframe : true,width : '95%',	height : '95%'});";
					out.println("<a class=\"action-button\" href=\"#\" onclick=\"" + js + " return false;\">" + i18nAccess.getText("macro.name." + name, name) + "</a>");
				} else {
					out.println("<form method=\"post\" action=\"" + URLHelper.createURL(ctx) + "\">");
					out.println("<input type=\"hidden\" name=\"module\" value=\"macro\" />");
					out.println("<input type=\"hidden\" name=\"previewEdit\" value=\"true\" />");
					out.println("<input type=\"hidden\" name=\"webaction\" value=\"macro.executeMacro\" />");
					out.println("<input type=\"hidden\" name=\"macro-" + name + "\" value=\"" + name + "\" />");
					out.println("<input type=\"hidden\" name=\"macro\" value=\"" + name + "\" />");
					out.println("<input class=\"action-button\" type=\"submit\" value=\"" + i18nAccess.getText("macro.name." + name, name) + "\" />");
					out.println("</form>");
				}
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

	public static MenuElement createArticlePageName(ContentContext ctx, MenuElement monthPage) throws Exception {
		if ((monthPage != null) && (monthPage.getParent() != null) && (monthPage.getParent().getParent() != null)) {
			MenuElement groupPage = monthPage.getParent().getParent();
			String[] splittedName = monthPage.getName().split("-");
			String year = null;
			String mount = null;
			if (splittedName.length >= 2) {
				year = splittedName[splittedName.length - 2];
				mount = splittedName[splittedName.length - 1];
			}
			try {
				Integer.parseInt(year);
			} catch (Throwable t) {
				year = null;
			}
			if (year != null && mount != null) {
				Collection<MenuElement> children = monthPage.getChildMenuElements();

				int maxNumber = 0;
				for (MenuElement child : children) {
					splittedName = child.getName().split("-");

					try {
						int currentNumber = Integer.parseInt(splittedName[splittedName.length - 1]);
						if (currentNumber > maxNumber) {
							maxNumber = currentNumber;
						}
					} catch (NumberFormatException e) {
					}
				}
				maxNumber = maxNumber + 1;
				MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, monthPage.getName(), groupPage.getName() + "-" + year + "-" + mount + "-" + maxNumber, true);
				newPage.setVisible(true);

				return newPage;

			} else {
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				String msg = i18nAccess.getText("action.add.new-news-today");
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
			}
		} else {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String msg = i18nAccess.getText("action.add.new-news-today");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		}

		return null;
	}

	/**
	 * return a list of page with only year as children.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public static List<MenuElement> searchArticleRoot(ContentContext ctx) throws Exception {
		List<MenuElement> outPages = new LinkedList<MenuElement>();
		MenuElement root = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx);
		for (MenuElement page : root.getAllChildren()) {
			if (page.getChildMenuElements().size() > 0) {
				boolean isArticleRoot = false;
				for (MenuElement child : page.getChildMenuElements()) {
					int index = child.getName().lastIndexOf('-');
					String year = child.getName();
					if (index > 0) {
						year = child.getName().substring(index + 1, child.getName().length());
					}
					if (year.length() == 4 && NumberUtils.isNumber(year)) {
						isArticleRoot = true;
					}
				}
				if (isArticleRoot) {
					outPages.add(page);
				}
			}
		}
		return outPages;
	}

	public static void createPageStructure(ContentContext ctx, MenuElement page, Map componentsType, boolean fakeContent) throws Exception {
		createPageStructure(ctx, page, componentsType, fakeContent, null, null);
	}

	public static void createPageStructure(ContentContext ctx, MenuElement page, Map componentsType, boolean fakeContent, Date date, Collection<String> tags) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> lgs = globalContext.getContentLanguages();
		if (!StringHelper.isTrue("" + componentsType.get("all-languages"))) {
			lgs = Arrays.asList(new String[] { ctx.getRequestContentLanguage() });
		}
		ContentService content = ContentService.getInstance(ctx.getRequest());
		for (String lg : lgs) {
			String parentId = "0";
			Set<String> keysSet = componentsType.keySet();
			List<String> keys = new LinkedList<String>();
			keys.addAll(keysSet);
			Collections.sort(keys);			
			for (String compName : keys) {
				if (compName.contains(".") && !compName.endsWith(".style") && !compName.endsWith(".list") && !compName.endsWith(".area") && !compName.endsWith(".init-content")) {
					String style = (String) componentsType.get(compName + ".style");
					boolean asList = StringHelper.isTrue(componentsType.get(compName + ".list"));
					String area = (String) componentsType.get(compName + ".area");
					boolean initContent= StringHelper.isTrue(componentsType.get(compName + ".init-content"));

					String type = StringHelper.split(compName, ".")[1];

					String value = (String) componentsType.get(compName);
					if (fakeContent) {
						if (type.equals(Title.TYPE) || type.equals(SubTitle.TYPE)) {
							value = LoremIpsumGenerator.getParagraph(3, false, true);
						} else {
							value = LoremIpsumGenerator.getParagraph(50, false, true);
						}
					}
					if (type.equalsIgnoreCase(EventDefinitionComponent.TYPE) || type.equalsIgnoreCase(TimeRangeComponent.TYPE)) {
						String dateStr = StringHelper.renderTime(date);
						value = dateStr+TimeRangeComponent.VALUE_SEPARATOR+dateStr;
					} else if (type.equals(DateComponent.TYPE) && date != null) {
						value = StringHelper.renderTime(date);
					} else if (type.equals(Tags.TYPE) && tags != null) {
						value = StringHelper.collectionToString(tags, ";");
					}
					parentId = MacroHelper.addContent(lg, page, parentId, type, style, area, value, asList, ctx.getCurrentEditUser());
					if (initContent) {
						IContentVisualComponent comp = content.getComponent(ctx, parentId);
						comp.initContent(ctx);
					}
				}
			}
		}
	}

	public static void addContentInPage(ContentContext ctx, MenuElement newPage, String pageStructureName) throws IOException, Exception {
		addContentInPage(ctx, newPage, pageStructureName, null, null);
	}

	public static void addContentInPage(ContentContext ctx, MenuElement newPage, String pageStructureName, Date date, Collection<String> tags) throws IOException, Exception {
		newPage.setVisible(true);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(new Date());

		Properties pressReleaseStructure = ctx.getCurrentTemplate().getMacroProperties(globalContext, pageStructureName);
		if (pressReleaseStructure == null) {
			logger.warning("file not found : " + pageStructureName);
			Collection<String> lgs = globalContext.getContentLanguages();
			for (String lg : lgs) {
				String parentId = "0";
				String dateValue = "";
				if (date != null) {
					dateValue = StringHelper.renderTime(date);
				}
				parentId = MacroHelper.addContent(lg, newPage, parentId, DateComponent.TYPE, dateValue, ctx.getCurrentEditUser());
				if (tags != null) {
					parentId = MacroHelper.addContent(lg, newPage, parentId, Tags.TYPE, StringHelper.collectionToString(tags, ";"), ctx.getCurrentEditUser());
				}
				parentId = MacroHelper.addContent(lg, newPage, parentId, Title.TYPE, "", ctx.getCurrentEditUser());
				parentId = MacroHelper.addContent(lg, newPage, parentId, Description.TYPE, "", ctx.getCurrentEditUser());
				parentId = MacroHelper.addContent(lg, newPage, parentId, GlobalImage.TYPE, "", ctx.getCurrentEditUser());
				parentId = MacroHelper.addContent(lg, newPage, parentId, Paragraph.TYPE, "", ctx.getCurrentEditUser());
			}
		} else {
			MacroHelper.createPageStructure(ctx, newPage, pressReleaseStructure, StringHelper.isTrue(pressReleaseStructure.get("fake-content")), date, tags);
		}

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setAskStore(true);
	}

	public static String getMonthPageName(ContentContext ctx, String yearPageName, Date date) {

		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(date);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		String monthName = MacroHelper.getDisplayName(cal, Calendar.MONTH, MacroHelper.CALENDAR_LONG, new Locale(globalContext.getDefaultLanguage()));
		monthName = StringHelper.createFileName(monthName); // remove special
															// char

		return yearPageName + "-" + monthName;
	}

	public static void createMonthStructure(ContentContext ctx, MenuElement yearPage) throws Exception {

		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.MONTH, 11);

		boolean lastMounth = false;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		while (!lastMounth) {
			Collection<String> lgs = globalContext.getContentLanguages();
			MenuElement mounthPage = MacroHelper.addPageIfNotExist(ctx, yearPage.getName(), getMonthPageName(ctx, yearPage.getName(), cal.getTime()), false);
			if (mounthPage.getContent().length == 0) {
				mounthPage.setVisible(true);
				for (String lg : lgs) {
					String monthName = MacroHelper.getDisplayName(cal, Calendar.MONTH, MacroHelper.CALENDAR_LONG, new Locale(globalContext.getDefaultLanguage()));
					MacroHelper.addContent(lg, mounthPage, "0", Title.TYPE, monthName, ctx.getCurrentEditUser());
				}
			}
			cal.roll(Calendar.MONTH, false);
			if (cal.get(Calendar.MONTH) == 11) {
				lastMounth = true;
			}
		}
	}

	public static MenuElement createArticlePage(ContentContext ctx, MenuElement rootPage, Date date) throws IOException, Exception {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if (rootPage != null) {
			String yearPageName = rootPage.getName() + "-" + cal.get(Calendar.YEAR);
			MenuElement yearPage = MacroHelper.addPageIfNotExist(ctx, rootPage.getName(), yearPageName, true);
			createMonthStructure(ctx, yearPage);
			String mountPageName = MacroHelper.getMonthPageName(ctx, yearPage.getName(), date);
			MenuElement mountPage = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx).searchChildFromName(mountPageName);
			if (mountPage != null) {
				MenuElement newPage = MacroHelper.createArticlePageName(ctx, mountPage);
				return newPage;
			}
		}
		return null;
	}

	public static String getAlphabeticChildrenName(MenuElement parentPage, Character letter) {
		return parentPage.getName() + '_' + (letter == null ? "other" : letter.toString());
	}

	public static void createAlphabeticChildren(ContentContext ctx, MenuElement parentPage) throws Exception {
		for (char c : ALPHABET.toCharArray()) {
			String newPageName = getAlphabeticChildrenName(parentPage, c);
			MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, parentPage.getName(), newPageName, false);
			for (String lg : ctx.getGlobalContext().getContentLanguages()) {
				MacroHelper.addContent(lg, newPage, "0", Title.TYPE, "" + c, ctx.getCurrentEditUser());
			}
		}
		PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
	}
	
	public static void deleteContentByLanguage(ContentContext ctx, MenuElement page, String lg) {
		for (ComponentBean bean : page.getContent()) {
			if (bean.getLanguage().equals(lg)) {
				page.removeContent(ctx, bean.getId(), false);
			}
		}		
	}

	public static String getLaunchMacroXHTML(ContentContext ctx, String macro, String label) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		IMacro macroClass = MacroFactory.getInstance(ctx.getGlobalContext().getStaticConfig()).getMacro(macro);
		if (macroClass instanceof IInteractiveMacro) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("editPreview", "true");
			params.put("module", "macro");
			params.put("previewEdit", "true");
			params.put("webaction", "macro.executeInteractiveMacro");
			params.put("macro", macro);
			String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE), params);
			String actionURL = "try{jQuery.colorbox({href : '" + url + "',opacity : 0.6,iframe : true,width : '95%',	height : '95%'});} catch(err) {}; return false;";
			out.println("<div class=\"macro\">");
			out.println("<a class=\"as-modal\" href=\""+url+"\" onclick=\"" + actionURL + "\">" + label + "</a>");
			out.println("</div>");
		} else {
			out.println("<div class=\"macro\">");
			out.println("<form action=\"" + URLHelper.createURL(ctx) + "\" method=\"post\">");
			out.println("<input type=\"hidden\" value=\"macro\" name=\"module\">");
			out.println("<input type=\"hidden\" value=\"true\" name=\"previewEdit\">");
			out.println("<input type=\"hidden\" value=\"macro.executeMacro\" name=\"webaction\">");
			out.println("<input type=\"hidden\" value=\"" + macro + "\" name=\"macro\">");
			out.println("<input class=\"action-button\" type=\"submit\" value=\"" + label + "\">");
			out.println("</form>");
			out.println("</div>");
		}
		out.close();
		return new String(outStream.toByteArray());
	}
	
	public static MenuElement duplicatePage(ContentContext ctx, MenuElement page, String newname) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (page.getName().equals(newname)) {
			return null;
		}
		MenuElement outPage = MenuElement.getInstance(ctx.getGlobalContext());
		outPage.setId(StringHelper.getRandomId()) ;
		outPage.setName(newname);
		ComponentBean[] sourceData = page.getContent();
		ComponentBean[] targetData = new ComponentBean[sourceData.length];
		int i=0;
		for (ComponentBean bean : sourceData) {			
			targetData[i] = new ComponentBean(bean);
			targetData[i].setId(StringHelper.getRandomId());
			i++;
		}
		outPage.setContent(targetData);
		return outPage;
	}
	
	public static String copyChildren (ContentContext ctx, MenuElement source, MenuElement target, String sourcePattern, String targetPattern) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String errorPage = "";
		String sep = "";
		for (MenuElement child : source.getChildMenuElements())	{
			MenuElement newChild = duplicatePage(ctx, child, child.getName().replace(sourcePattern, targetPattern));
			if (newChild != null) {
				target.addChildMenuElement(newChild);
				errorPage = errorPage+copyChildren(ctx, child, newChild, sourcePattern, targetPattern);
			} else {
				errorPage = errorPage+sep+child.getName();
				sep = ",";
			}
		}
		return errorPage;
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

		System.out.println("***** MacroHelper.main : type = " + type); // TODO:
																		// remove
																		// debug
																		// trace
		System.out.println("***** MacroHelper.main : area = " + area); // TODO:
																		// remove
																		// debug
																		// trace
		System.out.println("***** MacroHelper.main : style = " + style); // TODO:
																			// remove
																			// debug
																			// trace
	}

}
