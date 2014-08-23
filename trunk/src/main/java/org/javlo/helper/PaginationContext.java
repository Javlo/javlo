package org.javlo.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;
import org.javlo.service.exception.ServiceException;

public class PaginationContext {

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PaginationContext.class.getName());

	private static final String KEY = PaginationContext.class.getName();

	private static final String PAGE_PARAM_NAME = "_page";

	private int page = 1;

	private int maxPage = 1;

	private int pageSize = 10;

	private int countElement = 0;

	private String key = null;

	public static PaginationContext getInstance(HttpServletRequest request, String key) throws ServiceException {		
		PaginationContext service = (PaginationContext) request.getAttribute(key);
		try {
			String newPage = request.getParameter("page");
			if (newPage != null && key.equals(request.getParameter("key"))) {
				service.page = Integer.parseInt(newPage);
			}
		} catch (Throwable t) {
			logger.fine(t.getMessage());
		}
		return service;
	}

	public static PaginationContext getInstance(HttpServletRequest request, String key, int inCountElement, int elemByPage) throws ServiceException {
		int maxPage = 1;
		if (elemByPage > 0) {
			maxPage = inCountElement / elemByPage;
			if (inCountElement % elemByPage != 0) {
				maxPage = maxPage + 1;
			}			
		}
		PaginationContext service = (PaginationContext) request.getAttribute(key);
		if (service == null) {
			service = new PaginationContext();
			service.key = key;
			request.setAttribute(key, service);
		}
		service.maxPage = maxPage;
		service.pageSize = elemByPage;

		if (service.countElement != inCountElement) {
			service.countElement = inCountElement;
			service.setPage(1);
		}
		try {
			String newPage = request.getParameter("page");
			if (newPage != null && key.equals(request.getParameter("key"))) {
				service.page = Integer.parseInt(newPage);
			}
		} catch (Throwable t) {
			logger.fine(t.getMessage());
		}
		return service;
	}

	public String transformURLToPage(String url, int page) {
		char sep = '?';
		if (url.contains("?")) {
			sep = '&';
		}
		if (page > maxPage) {
			page = maxPage;
		}
		if (page < 1) {
			page = 1;
		}
		return url + sep + PAGE_PARAM_NAME + '=' + page;
	}

	public String transformURLNextPage(String url) {
		char sep = '?';
		if (url.contains("?")) {
			sep = '&';
		}
		int nextPage = page + 1;
		if (nextPage > maxPage) {
			nextPage = maxPage;
		}
		return url + sep + PAGE_PARAM_NAME + '=' + nextPage;
	}

	public String transformURLPreviousPage(String url) {
		char sep = '?';
		if (url.contains("?")) {
			sep = '&';
		}
		int previouxPage = page - 1;
		if (previouxPage < 1) {
			previouxPage = 1;
		}
		return url + sep + PAGE_PARAM_NAME + '=' + previouxPage;
	}

	public String transformURLFirstPage(String url) {
		char sep = '?';
		if (url.contains("?")) {
			sep = '&';
		}
		return url + sep + PAGE_PARAM_NAME + '=' + 1;
	}

	public String transformURLLastPage(String url) {
		char sep = '?';
		if (url.contains("?")) {
			sep = '&';
		}
		return url + sep + PAGE_PARAM_NAME + '=' + maxPage;
	}

	public void pageAction(HttpServletRequest request) {
		RequestService requestService = RequestService.getInstance(request);
		String newPageStr = requestService.getParameter(PAGE_PARAM_NAME, "" + getPage());
		int newPage = Integer.parseInt(newPageStr);
		setPage(newPage);
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getMaxPage() {
		return maxPage;
	}

	public void setMaxPage(int maxPage) {
		this.maxPage = maxPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public boolean isPageVisible(int itemNumber) {
		boolean visible = itemNumber > (getPage() - 1) * getPageSize();
		visible = visible && (itemNumber <= (getPage() * getPageSize()));
		return visible;
	}

	public String renderCommand(ContentContext ctx, String url) throws FileNotFoundException, IOException {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<div class=\"pagination-command\">");

		out.print("<span class=\"first-page\">");
		String firstPageURL = transformURLFirstPage(url);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		out.println("<a href=\"" + firstPageURL + "\">" + i18nAccess.getViewText("global.first") + "</a></span>");

		/* previous page */
		if (getPage() > 1) {
			out.print("<span class=\"previous-page\">");
			String previousPageURL = transformURLPreviousPage(url);
			out.println("<a href=\"" + previousPageURL + "\">" + i18nAccess.getViewText("global.previous") + "</a></span>");
		}

		/* page status */
		out.print("<span class=\"page-status\">");
		if (getPage() > 3) {
			out.print("<span class=\"direct-page-link\">" + "<a href=\"" + transformURLToPage(url, getPage() - 5) + "\">...</a></span>");
		}
		if (getPage() > 2) {
			out.print("<span class=\"direct-page-link\">" + "<a href=\"" + transformURLToPage(url, getPage() - 2) + "\">" + (getPage() - 2) + "</a></span>");
		}
		if (getPage() > 1) {
			out.print("<span class=\"direct-page-link\">" + "<a href=\"" + transformURLToPage(url, getPage() - 1) + "\">" + (getPage() - 1) + "</a></span>");
		}
		out.print("<span class=\"direct-page-link current-page\">" + "<a href=\"" + transformURLToPage(url, getPage()) + "\">" + getPage() + "</a></span>");
		if (getPage() <= getMaxPage() - 1) {
			out.print("<span class=\"direct-page-link\">" + "<a href=\"" + transformURLToPage(url, getPage() + 1) + "\">" + (getPage() + 1) + "</a></span>");
		}
		if (getPage() <= getMaxPage() - 2) {
			out.print("<span class=\"direct-page-link\">" + "<a href=\"" + transformURLToPage(url, getPage() + 2) + "\">" + (getPage() + 2) + "</a></span>");
		}
		if (getPage() <= getMaxPage() - 3) {
			out.print("<span class=\"direct-page-link\">" + "<a href=\"" + transformURLToPage(url, getPage() + 5) + "\">...</a></span>");
		}

		// out.print(getPage() + "/" + getMaxPage());
		out.print("</span>");

		/* next page */
		if (getPage() < getMaxPage()) {
			out.print("<span class=\"next-page\">");
			String nextPageURL = transformURLNextPage(url);
			out.println("<a href=\"" + nextPageURL + "\">" + i18nAccess.getViewText("global.next") + "</a></span>");
		}

		out.print("<span class=\"last-page\">");
		String lastPageURL = transformURLLastPage(url);
		out.println("<a href=\"" + lastPageURL + "\">" + i18nAccess.getViewText("global.last") + "</a></span>");

		out.println("</div>");
		out.close();
		return writer.toString();
	}

	public int getCountElement() {
		return countElement;
	}

	public String getKey() {
		if (key == null) {
			return KEY;
		} else {
			return key;
		}
	}

}
