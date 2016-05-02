package org.javlo.navigation;

import org.javlo.context.ContentContext;

public interface IURLFactory {

	/**
	 * transform url to a unic string represent information (sp. : without extension).
	 * 
	 * @param url
	 * @return
	 */
	public String createURLKey(String url);

	/**
	 * create url to a page.
	 * 
	 * @param ctx
	 *            content context
	 * @param page
	 *            page in content
	 * @return a URL
	 * @throws Exception
	 */
	public String createURL(ContentContext ctx, MenuElement page) throws Exception;

	/**
	 * get the format of url (html, png, pdf...)
	 * 
	 * @param url
	 *            a url
	 * @return a content format (html, png, pdf...)
	 */
	public String getFormat(ContentContext ctx, String url);

}
