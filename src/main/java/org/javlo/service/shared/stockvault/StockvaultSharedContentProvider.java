package org.javlo.service.shared.stockvault;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.CssSelectorNodeFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.shared.AbstractSharedContentProvider;
import org.javlo.service.shared.SharedContent;

public class StockvaultSharedContentProvider extends AbstractSharedContentProvider {

	private Collection<SharedContent> content = null;

	private static Logger logger = Logger.getLogger(StockvaultSharedContentProvider.class.getName());

	public StockvaultSharedContentProvider() throws Exception {
		try {
			setURL(new URL("http://www.stockvault.net/"));
			setName("stockvault.net");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void refresh(ContentContext ctx) {
		content = null;
		getContent(ctx);
	}

	@Override
	public Collection<SharedContent> getContent(ContentContext ctx) {
		return getContent(ctx, getURL());
	}

	@Override
	public Collection<SharedContent> searchContent(ContentContext ctx, String query) {
		try {
			Collection<SharedContent> outContent = new LinkedList<SharedContent>();
			Set<String> allReadyFound = new HashSet<String>();
			for (int page = 1; page < 4; page++) { // read 3 pages
				URL url = new URL(URLHelper.addAllParams(URLHelper.mergePath(getURL().toString(), "/search/"), "query=" + StringHelper.toHTMLAttribute(query), "page=" + page));
				try {
					outContent.addAll(readURL(url, allReadyFound));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return outContent;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void init() throws ParserException, IOException {
		Parser htmlParser = new Parser(getURL().openConnection());
		NodeFilter cssFilter = new CssSelectorNodeFilter(".sidebar li a");
		NodeList nodeList = htmlParser.extractAllNodesThatMatch(cssFilter);
		Map<String, String> categories = new HashMap<String, String>();
		for (int i = 0; i < nodeList.size(); i++) {
			if (nodeList.elementAt(i) instanceof TagNode) {
				TagNode tag = (TagNode) nodeList.elementAt(i);
				String label = tag.toPlainTextString().trim();
				String href = tag.getAttribute("href").trim();
				if (label.length() > 0 && href.length() > 0) {
					categories.put(href, label);
				}
			} else {
				logger.severe("bad tag format found, check html on : " + getURL());
			}
		}
		setCategories(categories);
	}

	private Collection<SharedContent> getContent(ContentContext ctx, URL url) {
		if (content == null) {
			try {
				init();
				content = new LinkedList<SharedContent>();
				for (Map.Entry<String, String> category : getCategories(ctx).entrySet()) {
					logger.info("Stockvault load category : " + category.getValue());
					try {
						URL catURL = new URL(URLHelper.mergePath(url.toString(), category.getKey()));
						Collection<SharedContent> result = readURL(catURL);
						for (SharedContent sharedContent : result) {
							sharedContent.addCategory(category.getKey());
						}
						content.addAll(result);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return content;
	}
	
	private Collection<SharedContent> readURL(URL url) throws Exception {
		return readURL(url, null);
	}

	private Collection<SharedContent> readURL(URL url, Set<String> allReadyFound) throws Exception {
		Collection<SharedContent> outSharedContent = new LinkedList<SharedContent>();

		Parser htmlParser = new Parser(url.openConnection());
		NodeFilter cssFilter = new CssSelectorNodeFilter(".row_images2 li img");
		NodeList nodeList = htmlParser.extractAllNodesThatMatch(cssFilter);
		if (nodeList.size() == 0) {
			logger.severe("bad structure no images found.");
		}		
		for (int i = 0; i < nodeList.size(); i++) {
			if (nodeList.elementAt(i) instanceof TagNode) {
				TagNode tag = (TagNode) nodeList.elementAt(i);
				String src = tag.getAttribute("src");
				String[] splitedSrc = src.split("/");
				if (splitedSrc.length > 0) {
					String id = StringHelper.getFileNameWithoutExtension(splitedSrc[splitedSrc.length - 2]);
					StockvaultSharedContent sharedContent = new StockvaultSharedContent(id, null);
					String title = tag.getAttribute("alt");
					if (allReadyFound == null || !allReadyFound.contains(title)) {
						if (allReadyFound != null) {
							allReadyFound.add(title);
						}
						String[] splitedTitle = title.split("-");
						if (splitedTitle.length > 0) {
							title = splitedTitle[1];
						}
						sharedContent.setTitle(title.trim());
						sharedContent.setImageUrl(URLHelper.mergePath(getURL().toString(), src));
						sharedContent.setRemoteImageUrl(URLHelper.mergePath(getURL().toString(), "/photo/download/", id));
						outSharedContent.add(sharedContent);
					}
				}
			}
		}
		return outSharedContent;
	}

	@Override
	public int getContentSize(ContentContext ctx) {
		if (content != null) {
			return super.getContentSize(ctx);
		} else {
			return -1;
		}
	}

	@Override
	public int getCategoriesSize(ContentContext ctx) {
		if (content != null) {
			return super.getCategoriesSize(ctx);
		} else {
			return -1;
		}
	}

	@Override
	public String getType() {
		return TYPE_IMAGE;
	}

}
