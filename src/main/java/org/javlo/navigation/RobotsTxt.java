package org.javlo.navigation;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.service.ContentService;

public class RobotsTxt {

	private RobotsTxt() {}
	
	public static void renderRobotTxt(ContentContext ctx, OutputStream outStream) throws Exception {
		PrintWriter out = new PrintWriter(outStream);
		ContentService content = ContentService.getInstance(ctx.getRequest());
		out.println("User-agent: *");
		out.println("Sitemap: "+URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), "/sitemap.xml"));
		out.println("Disallow: "+URLHelper.createStaticURL(ctx, "/edit/"));
		out.println("Disallow: "+URLHelper.createStaticURL(ctx, "/preview/"));
		out.println("Disallow: "+URLHelper.createStaticURL(ctx, "/page/"));
		for (MenuElement page : content.getNavigation(ctx).getAllChildren()) {			
			if (page.getFinalSeoWeight() == MenuElement.SEO_HEIGHT_NULL) {
				ctx.setFormat("html");
				out.println("Disallow: "+URLHelper.createURL(ctx, page));
				ctx.setFormat("pdf");
				out.println("Disallow: "+URLHelper.createURL(ctx, page));			
			}
		}
		out.flush();
	}	

}
