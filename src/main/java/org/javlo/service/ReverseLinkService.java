package org.javlo.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.CharUtils;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.StringRemplacementHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.Comparator.StringSizeComparator;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.exception.ServiceException;

public class ReverseLinkService {

	private class ComponentPage {
		private IReverseLinkComponent component;
		private IContentVisualComponent previous;
		private boolean previousSearched = false;
		private MenuElement page;

		public IReverseLinkComponent getComponent() {
			return component;
		}

		public MenuElement getPage() {
			return page;
		}

		public void setComponent(IReverseLinkComponent component) {
			this.component = component;
		}

		public void setPage(MenuElement page) {
			this.page = page;
		}

		public IContentVisualComponent getPrevious() {
			if (!previousSearched) {
				IContentVisualComponent prev = ((IContentVisualComponent) component).getPreviousComponent();
				// search previous component without link.
				while (prev != null && prev instanceof IReverseLinkComponent) {
					prev = prev.getPreviousComponent();
				}
				previous = prev;
				previousSearched = true; // pervious can be always null
			}
			return previous;
		}

	}

	public static final String NONE = "none";
	public static final String ONLY_FIRST = "only-first";
	public static final String ONLY_THIS_PAGE = "only-this-page";
	public static final String ONLY_PREVIOUS_COMPONENT = "only-previous-component";

	public static final String ALL = "all";

	public static final List<String> LINK_TYPES = Arrays.asList(new String[] { ALL, ONLY_FIRST, ONLY_THIS_PAGE, ONLY_PREVIOUS_COMPONENT });

	private static final String KEY = ReverseLinkService.class.getName();

	public static ReverseLinkService getInstance(GlobalContext globalContext) throws ServiceException {
		ReverseLinkService service = (ReverseLinkService) globalContext.getAttribute(KEY);
		if (service == null) {
			service = new ReverseLinkService();
			globalContext.setAttribute(KEY, service);
		}
		return service;
	}

	public static void main(String[] args) {
		System.out.println("CharUtils.isAsciiAlphanumeric = " + CharUtils.isAsciiAlphanumeric(' '));
	}

	private transient Map<String, MenuElement> reversedLinkCache = null;

	private transient Map<String, ComponentPage> reversedLinkComponentCache = null;

	private transient String reversedLinkComponentCacheLang = null;

	private final Object lock = new Object();

	public void clearCache() {
		synchronized (lock) {
			reversedLinkCache = null;
			reversedLinkComponentCache = null;
		}
	}

	public List<String> getAllTextName(ContentContext ctx, MenuElement elem) throws Exception {
		List<String> allNames = new LinkedList<String>();
		allNames.addAll(getReversedLinkCache(elem).keySet());
		allNames.addAll(getReversedLinkComponentCache(ctx, elem).keySet());
		Collections.sort(allNames, new StringSizeComparator());
		return allNames;
	}

	public Map<String, MenuElement> getReversedLinkCache(MenuElement elem) throws Exception {
		if (reversedLinkCache == null) {
			synchronized (lock) {
				if (reversedLinkCache == null) {
					reversedLinkCache = new HashMap<String, MenuElement>();					
					for (MenuElement element : elem.getAllChildrenList()) {
						String[] linkNames = StringHelper.readLines(element.getReversedLink());
						for (String linkName : linkNames) {
							if (linkName.trim().length() > 0) {
								reversedLinkCache.put(linkName, element);
							}
						}
					}
				}
			}
		}
		return reversedLinkCache;
	}

	/* reverese link component */
	private Map<String, ComponentPage> getReversedLinkComponentCache(ContentContext ctx, MenuElement elem) throws Exception {
		if ((reversedLinkComponentCache == null) || (!reversedLinkComponentCacheLang.equals(ctx.getRequestContentLanguage()))) {
			synchronized (lock) {
				if ((reversedLinkComponentCache == null) || (!reversedLinkComponentCacheLang.equals(ctx.getRequestContentLanguage()))) {
					ContentContext noAreaCtx = ctx.getContextWithArea(null);
					reversedLinkComponentCacheLang = ctx.getRequestContentLanguage();
					reversedLinkComponentCache = new HashMap<String, ComponentPage>();					
					for (MenuElement element : elem.getAllChildrenList()) {
						ContentElementList content = element.getLocalContentCopy(noAreaCtx);

						int count = 0; // DEBUG

						while (content.hasNext(noAreaCtx) && count < 100000) {

							count++;

							IContentVisualComponent comp = content.next(noAreaCtx);
							if (comp instanceof IReverseLinkComponent) {
								if (((IReverseLinkComponent) comp).isReverseLink()) {
									String text = ((IReverseLinkComponent) comp).getLinkText(noAreaCtx);
									ComponentPage componentPage = new ComponentPage();
									componentPage.setComponent((IReverseLinkComponent) comp);
									componentPage.setPage(element);
									reversedLinkComponentCache.put(text, componentPage);
									String transformText = XHTMLHelper.escapeXHTML(text);
									if (!transformText.equals(text)) {
										reversedLinkComponentCache.put(transformText, componentPage);
									}
								}
							}
						}

						if (count == 100000) {
							GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
							System.out.println("***************************************************************************");
							System.out.println("***************************************************************************");
							System.out.println("***** BAD CONTENT STRUCTURE DETECTED IN " + this.getClass().getName());
							System.out.println("***** context key : " + globalContext.getContextKey());
							System.out.println("***** path : " + ctx.getPath());
							System.out.println("***************************************************************************");
							System.out.println("***************************************************************************");
						}
					}
				}
			}
		}
		return reversedLinkComponentCache;

	}

	public String replaceLink(ContentContext ctx, IContentVisualComponent comp, String contentValue) throws Exception {

		if (!ctx.getGlobalContext().isReversedLink()) {
			return contentValue;
		}

		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement root = content.getNavigation(ctx);
		MenuElement currentPage = ctx.getCurrentPage();
		List<String> texts = getAllTextName(ctx, root);

		MenuElement parentPage = null;

		StringRemplacementHelper remplacement = new StringRemplacementHelper();

		for (Object element : texts) {
			String text = (String) element;
			if (text.trim().length() > 0) {
				ComponentPage componentPage = getReversedLinkComponentCache(ctx, root).get(text);
				
				String url = null;
				if (componentPage == null) {
					MenuElement targetPage = getReversedLinkCache(root).get(text);					
					if ((targetPage != null) && !targetPage.getId().equals(currentPage.getId())) {
						parentPage = targetPage;
						ContentContext viewCtx = new ContentContext(ctx);
						if (ctx.getRenderMode() == ContentContext.PAGE_MODE) {
							viewCtx.setRenderMode(ContentContext.VIEW_MODE);
						}
						url = URLHelper.createURL(viewCtx, getReversedLinkCache(root).get(text).getPath());
					}
				} else {
					url = componentPage.getComponent().getLinkURL(ctx);
					parentPage = componentPage.getPage();
				}

				String target = "";
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

				if (url != null) {
					if (URLHelper.isAbsoluteURL(url) && globalContext.isOpenExternalLinkAsPopup(url)) {
						target = " target=\"_blank\"";
					}
					int textPos = contentValue.indexOf(text);

					boolean continueSearch = true;
					while (continueSearch && (textPos >= 0) && (textPos + text.length() <= contentValue.length())) {
						/*
						 * check if the word (or sentence) is separed with space
						 * or punctuation
						 */
						boolean replace = true;
						if (textPos + text.length() + 1 < contentValue.length()) {
							if (CharUtils.isAsciiAlphanumeric(contentValue.charAt(textPos + text.length()))) {
								replace = false;
							}
						}
						if (textPos - 1 >= 0) {
							if (CharUtils.isAsciiAlphanumeric(contentValue.charAt(textPos - 1))) {
								replace = false;
							}
						}
						if (url.trim().length() == 0) {
							replace = false;
						}
						if (replace) {
							//url = StringHelper.toXMLAttribute(url);
							if (componentPage != null && componentPage.getComponent() != null && componentPage.getComponent().isOnlyPreviousComponent()) {
								IContentVisualComponent prevComp = ComponentHelper.getPreviousComponent((IContentVisualComponent) componentPage.getComponent(), ctx);
								if (prevComp != null && comp != null && prevComp.getId().equals(comp.getId())) {
									remplacement.addReplacementIfPossible(textPos, textPos + text.length(), "<a href=\"" + url + "\"" + target + ">" + text + "</a>");
								}
							} else {
								if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
									if (parentPage != null) {
										StringBuffer linkInfo = new StringBuffer();
										String randomId = StringHelper.getRandomId();
										linkInfo.append("<span id=\"box-link-" + randomId + "\" class=\"reverselink-info\">");
										I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
										String reverseLinkTitle = i18nAccess.getText("item.reversed-link.title");
										linkInfo.append("<span class=\"title\">" + reverseLinkTitle + "</span>");
										String pageURL = URLHelper.createURL(ctx, parentPage.getPath());
										linkInfo.append("<a title=\"" + reverseLinkTitle + parentPage.getLabel(ctx) + "\" href=\"" + pageURL + "\"><span class=\"glyphicon glyphicon-link\" aria-hidden=\"true\"></span>" + parentPage.getLabel(ctx) + "</a>");
										linkInfo.append("</span>");
										if (componentPage != null && ctx.getRequest().getAttribute("replaced-" + componentPage.hashCode()) == null) {
											if (!componentPage.getComponent().isOnlyThisPage() || componentPage.getPage().equals(currentPage)) {
												if (!url.equals(URLHelper.createURL(ctx, comp.getPage()))) {
													remplacement.addReplacementIfPossible(textPos, textPos + text.length(), linkInfo + "<a class=\"reverse-link-preview\" href=\"" + url + "\" id=\"link-" + randomId + "\">" + text + "</a>");
												}
											}
										}
									}
								} else if (componentPage != null) {
									componentPage.getComponent();
									if (!componentPage.getComponent().isOnlyThisPage() || componentPage.getPage().getId().equals(currentPage.getId())) {
										if (ctx.getRequest().getAttribute("replaced-" + componentPage.hashCode()) == null) {
											/* no link on him self */
											if (!url.equals(URLHelper.createURL(ctx, comp.getPage()))) {
												remplacement.addReplacementIfPossible(textPos, textPos + text.length(), "<a href=\"" + url + "\"" + target + ">" + text + "</a>");
											}
										}
									}
								}
								if (componentPage != null && componentPage.getComponent().isOnlyFirstOccurrence()) {
									continueSearch = false;
									ctx.getRequest().setAttribute("replaced-" + componentPage.hashCode(), "");
								}
							}
						}
						int delta = textPos + text.length();
						textPos = contentValue.substring(textPos + text.length()).indexOf(text);
						if (textPos >= 0) {
							textPos = textPos + delta;
						}
					}
				}
			}
		}

		String outText = remplacement.start(contentValue);

		return outText;
	}
}
