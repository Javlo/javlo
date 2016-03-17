package org.javlo.module.dashboard;

import java.net.MalformedURLException;
import java.net.URL;

import org.javlo.bean.Link;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IInternalLink;
import org.javlo.component.core.ILink;
import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
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

	public static ReportBean getReport(ContentContext ctx) throws Exception {
		ReportBean report = new ReportBean();
		ContentService contentService = ContentService.getInstance(ctx.getRequest());
		MenuElement root = contentService.getNavigation(ctx);
		for (MenuElement page : root.getAllChildren()) {
			/* description */
			if (page.isRealContent(ctx)) {
				report.pageWithContent++;
				CheckTitle checkTitle = new CheckTitle();
				checkTitle.checkPage(ctx, page);				
				if (checkTitle.getErrorCount(ctx) > 0 && checkTitle.getLevel(ctx) > IIntegrityChecker.WARNING_LEVEL) {
					report.pageTitleBad++;
				} else {
					report.pageTitleRight++;
				}
				
				CheckDescription checkDescrition = new CheckDescription();
				checkDescrition.checkPage(ctx, page);
				if (checkDescrition.getErrorCount(ctx) > 0 && checkDescrition.getLevel(ctx) > IIntegrityChecker.WARNING_LEVEL) {
					report.pageDescriptionBad++;
				} else {
					report.pageDescriptionRight++;
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
				if (page.getAllChildren().length == 0) {
					report.pageWithoutContent++;
				} else {
					report.pageWithContent++;
				}
			}
			ContentContext allAreaContext = ctx.getContextWithArea(null);
			ContentElementList content = page.getContent(allAreaContext);
			while (content.hasNext(allAreaContext)) {
				IContentVisualComponent comp = content.next(allAreaContext);
				if (comp instanceof IInternalLink) {
					String pageId = ((IInternalLink)comp).getLinkId();
					if (root.searchChildFromId(pageId) == null) {
						report.badInternalLink++;						
						report.badInternalLinkPages.add(new Link(URLHelper.createURL(ctx, page), page.getTitle(ctx)));
					} else {
						report.rightInternalLink++;
					}
				} else if (comp instanceof ILink) {
					String url = ((ILink) comp).getURL(ctx);
					if (url != null && URLHelper.isAbsoluteURL(url)) {
						try {
							if (!NetHelper.isURLValid(new URL(url))) {
								report.badExternalLink++;
								report.badExternalLinkPages.add(new Link(URLHelper.createURL(ctx, page), page.getTitle(ctx)));
							} else {
								report.rightExternalLink++;
							}
						} catch (MalformedURLException e) {
							report.badExternalLink++;
						}
					}
				}
			}

		}
		return report;
	}

}
