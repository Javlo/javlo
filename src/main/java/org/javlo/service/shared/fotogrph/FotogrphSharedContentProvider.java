package org.javlo.service.shared.fotogrph;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XMLManipulationHelper;
import org.javlo.helper.XMLManipulationHelper.TagDescription;
import org.javlo.service.shared.AbstractSharedContentProvider;
import org.javlo.service.shared.SharedContent;

public class FotogrphSharedContentProvider extends AbstractSharedContentProvider {

	private static Logger logger = Logger.getLogger(FotogrphSharedContentProvider.class.getName());

	private static final int NUMBER_OF_PAGES = 14;

	private Collection<SharedContent> content = null;

	public FotogrphSharedContentProvider() {
		setName("fotogrph.com");
		try {
			setURL(new URL("http://fotogrph.com/"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void refresh() {
		content = null;
		getContent();
	}

	@Override
	public Collection<SharedContent> getContent() {
		if (content == null) {
			content = new LinkedList<SharedContent>();
			try {
				for (int page = 1; page <= NUMBER_OF_PAGES; page++) {
					URL catURL;
					if (page == 1) {
						catURL = getURL();
					} else {
						catURL = new URL(URLHelper.mergePath(getURL().toString(), "" + page));
					}

					logger.info("load images list : " + catURL);

					String html = NetHelper.readPage(catURL);
					TagDescription[] tags = XMLManipulationHelper.searchAllTag(html, false);

					String id = null;
					String imageURL = null;
					String imagePreviewURL = null;
					String imageTitle = null;
					for (TagDescription tag : tags) {
						TagDescription parent = XMLManipulationHelper.searchParent(tags, tag);
						if (tag.getName().toLowerCase().equals("img") && parent.getAttribute("class", "").equals("thumbnail")) {
							imagePreviewURL = URLHelper.mergePath(getURL().toString(), tag.getAttribute("src", null));
							id = StringHelper.getFileNameFromPath(imagePreviewURL);
							imageURL = URLHelper.mergePath(getURL().toString(), parent.getAttribute("href", null), "download");
							for (TagDescription child : XMLManipulationHelper.searchChildren(tags, parent)) {
								if (child.getName().toLowerCase().equals("h3")) {
									imageTitle = html.substring(child.getOpenEnd() + 1, child.getCloseStart()).trim();
								}
							}
							if (imageURL != null && imagePreviewURL != null) {
								FotogrphSharedContent sharedContent = new FotogrphSharedContent(id, null);
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

	public static void main(String[] args) {
		try {
			FotogrphSharedContentProvider provider = new FotogrphSharedContentProvider();
			for (SharedContent content : provider.getContent()) {
				System.out.println(content.getTitle());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getType() {
		return TYPE_IMAGE;
	}

	@Override
	public int getContentSize() {
		if (content != null) {
			return super.getContentSize();
		} else {
			return -1;
		}
	}

	@Override
	public int getCategoriesSize() {
		if (content != null) {
			return super.getCategoriesSize();
		} else {
			return -1;
		}
	}

}
