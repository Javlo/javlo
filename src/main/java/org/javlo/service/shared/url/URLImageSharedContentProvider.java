package org.javlo.service.shared.url;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XMLManipulationHelper;
import org.javlo.helper.XMLManipulationHelper.TagDescription;
import org.javlo.service.shared.AbstractSharedContentProvider;
import org.javlo.service.shared.SharedContent;

public class URLImageSharedContentProvider extends AbstractSharedContentProvider {

	private static Logger logger = Logger.getLogger(URLImageSharedContentProvider.class.getName());

	private Collection<SharedContent> content = null;

	public URLImageSharedContentProvider(URL url) {
		setName(url.getHost());
		setURL(url);
	}

	@Override
	public void refresh(ContentContext ctx) {
		content = null;
	}

	@Override
	public Collection<SharedContent> getContent(ContentContext ctx) {
		if (content == null) {
			content = new LinkedList<SharedContent>();
			try {
				String html = NetHelper.readPageGet(getURL());
				TagDescription[] tags = XMLManipulationHelper.searchAllTag(html, false);
				
				String urlPrefix = getURL().toString();
				if (urlPrefix.contains("/")) {
					urlPrefix = urlPrefix.substring(0, urlPrefix.lastIndexOf('/'));
				}

				String id = null;
				String imageURL = null;
				String imagePreviewURL = null;
				String imageTitle = null;				
				for (TagDescription tag : tags) {
					TagDescription parent = XMLManipulationHelper.searchParent(tags, tag);
					if (tag.getName().toLowerCase().equals("img") && parent.getName().toLowerCase().equals("a")) {
						String href = parent.getAttribute("href", "").trim();
						String src = tag.getAttribute("src", "");
						if (StringHelper.isImage(href) && StringHelper.isImage(src)) {
							if (StringHelper.isURL(href)) {
								imageURL = href;
							} else {	
								if (!href.startsWith("/") || href.startsWith("..")) {									
									imageURL = URLHelper.mergePath(urlPrefix, href);
								} else {
									imageURL = URLHelper.mergePath(URLHelper.removeURI(urlPrefix), href);
								}
							}
							if (StringHelper.isURL(src)) {
								imagePreviewURL = src;
							} else {								
								if (!href.startsWith("/") || href.startsWith("..")) {									
									imagePreviewURL = URLHelper.mergePath(urlPrefix, src);
								} else {
									imagePreviewURL = URLHelper.mergePath(URLHelper.removeURI(urlPrefix), src);
								}
							}
							imageTitle = tag.getAttribute("alt", parent.getAttribute("title", StringHelper.getFileNameWithoutExtension(StringHelper.getFileNameFromPath(imageURL))));
							id = ""+StringHelper.getCRC32(imagePreviewURL);	
							if (imageURL != null && imagePreviewURL != null) {
								URLSharedContent sharedContent = new URLSharedContent(id, null);
								sharedContent.setImageUrl(imagePreviewURL);
								sharedContent.setTitle(imageTitle);
								sharedContent.setRemoteImageUrl(imageURL);
								content.add(sharedContent);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return content;
	}

	@Override
	public String getType() {	
		return TYPE_IMAGE;
	}
	
	@Override
	public boolean isSearch() {
		return true;
	}
}

