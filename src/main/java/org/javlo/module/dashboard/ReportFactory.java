package org.javlo.module.dashboard;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.javlo.bean.Link;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IInternalLink;
import org.javlo.component.core.ILink;
import org.javlo.component.files.AbstractFileComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.integrity.CheckDescription;
import org.javlo.service.integrity.CheckImageLabel;
import org.javlo.service.integrity.CheckTitle;
import org.javlo.service.integrity.CheckTitleHierarchy;
import org.javlo.service.integrity.IIntegrityChecker;

public class ReportFactory {

	private ReportFactory() {
	}

	protected static boolean isPageValid(ContentContext ctx, ReportFilter filter, MenuElement page) {
		boolean accepted = true;
		if (filter.getStartDate() != null) {
			if (TimeHelper.isBeforeOrEqualForDay(page.getCreationDate(), filter.getStartDate())) {
				accepted = false;
			}
		}
		return accepted;
	}

	public static ReportBean getReport(ContentContext ctx) throws Exception {
		ReportBean report = new ReportBean();
		ContentService contentService = ContentService.getInstance(ctx.getRequest());
		MenuElement root = contentService.getNavigation(ctx);
		Map<String, String> moduleAction = new HashMap<String, String>();
		moduleAction.put("module", "content");
		Map<String, MenuElement> title = new HashMap<String, MenuElement>();
		report.allTitleDifferent = true;
		Set<String> pageDone = new HashSet<String>();
		ReportFilter filter = ReportFilter.getInstance(ctx.getRequest().getSession());
		for (MenuElement page : root.getAllChildrenList()) {
			if (isPageValid(ctx, filter, page)) {
				report.totalPageAnnalysed++;
				if (page.isRealContent(ctx) && !page.isInTrash()) {
					if (report.allTitleDifferent) {
						if (title.keySet().contains(page.getTitle(ctx))) {
							report.sameTitlePage1 = new Link(URLHelper.createURL(ctx, title.get(page.getTitle(ctx)), moduleAction), "1");
							report.sameTitlePage2 = new Link(URLHelper.createURL(ctx, page, moduleAction), "2");
							report.allTitleDifferent = false;
						}
						title.put(page.getTitle(ctx), page);
					}
					report.pageWithContent++;
					CheckTitle checkTitle = new CheckTitle();
					checkTitle.checkPage(ctx, page);
					if (checkTitle.getErrorCount(ctx) > 0 && checkTitle.getLevel(ctx) > IIntegrityChecker.WARNING_LEVEL) {
						report.getNoTitlePages().add(new Link(URLHelper.createURL(ctx, page, moduleAction), page.getTitle(ctx)));
						report.pageTitleBad++;
					} else {
						report.pageTitleRight++;
						if (checkTitle.getLevel(ctx) == IIntegrityChecker.WARNING_LEVEL) {
							report.pageTitleBadSize++;
						} else {
							report.pageTitleRightSize++;
						}
					}
					/* description */
					CheckDescription checkDescrition = new CheckDescription();
					checkDescrition.checkPage(ctx, page);
					if (checkDescrition.getErrorCount(ctx) > 0 && checkDescrition.getLevel(ctx) > IIntegrityChecker.WARNING_LEVEL) {
						report.getNoDescriptionPages().add(new Link(URLHelper.createURL(ctx, page, moduleAction), page.getTitle(ctx)));
						report.pageDescriptionBad++;
					} else {
						report.pageDescriptionRight++;
						if (checkDescrition.getLevel(ctx) == IIntegrityChecker.WARNING_LEVEL) {
							report.pageDescriptionBadSize++;
						} else {
							report.pageDescriptionRightSize++;
						}
					}
					/* title */
					CheckTitleHierarchy checkTitleHierachy = new CheckTitleHierarchy();
					checkTitleHierachy.checkPage(ctx, page);
					if (checkTitleHierachy.getErrorCount(ctx) > 0) {
						report.pageTitleStructureBad++;
					} else {
						report.pageTitleStructureRight++;
					}
					/* image */
					CheckImageLabel checkImage = new CheckImageLabel();
					checkImage.checkPage(ctx, page);
					if (checkImage.getErrorCount(ctx) > 0) {
						report.pageImageAltBad++;
					} else {
						report.pageImageAltRight++;
					}
				} else {
					if (page.getAllChildrenList().size() == 0) {
						report.pageWithoutContent++;
					} else {
						report.pageWithContent++;
					}
				}
				ContentContext allAreaContext = ctx.getContextWithArea(null);
				ContentElementList content = page.getContent(allAreaContext);
				Calendar refreshDate = Calendar.getInstance();
				refreshDate.add(Calendar.MONTH, 2); // check all 2 months
				Calendar day = Calendar.getInstance();
				while (content.hasNext(allAreaContext)) {
					IContentVisualComponent comp = content.next(allAreaContext);
					if (comp instanceof AbstractFileComponent) {
						AbstractFileComponent fileComp = (AbstractFileComponent) comp;
						if (!fileComp.getFile(ctx).exists()) {
							if (!pageDone.contains(comp.getPage().getId())) {
								report.badResourceRef++;
								Map<String, String> params = new HashMap<String, String>();
								params.putAll(moduleAction);
								params.put("pushcomp", comp.getId());
								params.put("webaction", "edit.changearea");
								params.put("area", comp.getArea());
								report.badResourceLinkPages.add(new Link(URLHelper.createURL(ctx, comp.getPage(), params), page.getTitle(ctx)));
								pageDone.add(comp.getPage().getId());
							}
						}
					} else if (ctx.getGlobalContext().getStaticConfig().isInternetAccess() && comp instanceof ILink) {
						String url = ((ILink) comp).getURL(ctx);
						if (url != null && URLHelper.isAbsoluteURL(url)) {
							try {
								if (report.badExternalLink < ReportBean.MAX_LINK_CHECK) {
									Date latestDate = ((ILink) comp).getLatestValidDate();
									if (latestDate != null) {
										day.setTime(latestDate);
									}
									if (latestDate == null || refreshDate.after(latestDate)) {
										if (!NetHelper.isURLValid(new URL(url), true)) {
											report.badExternalLink++;
											Map<String, String> params = new HashMap<String, String>();
											params.putAll(moduleAction);
											params.put("pushcomp", comp.getId());
											params.put("webaction", "edit.changearea");
											params.put("area", comp.getArea());
											report.badExternalLinkPages.add(new Link(URLHelper.createURL(ctx, page, params), page.getTitle(ctx)));
										} else {
											report.rightExternalLink++;
											((ILink) comp).setLatestValidDate(new Date());
										}
									}
								}
							} catch (MalformedURLException e) {
								report.badExternalLink++;
							}
						}
					} else if (comp instanceof IInternalLink) {
						if (report.badInternalLink < ReportBean.MAX_LINK_CHECK) {
							String pageId = ((IInternalLink) comp).getLinkId();
							if (root.searchChildFromId(pageId) == null) {
								report.badInternalLink++;
								Map<String, String> params = new HashMap<String, String>();
								params.putAll(moduleAction);
								params.put("pushcomp", comp.getId());
								params.put("webaction", "edit.changearea");
								params.put("area", comp.getArea());
								report.badInternalLinkPages.add(new Link(URLHelper.createURL(ctx, page, params), page.getTitle(ctx)));
							} else {
								report.rightInternalLink++;
							}
						}
					}
				}
			}
		}
		return report;
	}

	public static int getMaxMLinkCheck() {
		return ReportBean.MAX_LINK_CHECK;
	}
	
	public static void main(String[] args) throws MalformedURLException {
		URL url = new URL("http://www.europarl.europa.eu/sides/getDoc.do?type=OQ&reference=O-2015-000038&language=ES");
		System.out.println("***** ReportFactory.main : NetHelper.isURLValid(new URL(url), true) = "+NetHelper.isURLValid(url, true)); //TODO: remove debug trace
	}

}
