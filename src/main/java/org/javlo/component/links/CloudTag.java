/*
 * Created on 30/11/2009
 */
package org.javlo.component.links;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javlo.bean.Link;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

/**
 * @author pvandermaesen
 */
public class CloudTag extends AbstractVisualComponent {

	public static final String TYPE = "cloud-tag";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);		
		List<String> keywordSet = new LinkedList<String>();
		Map<String, Integer> countKeywords = new HashMap<String, Integer>();
		int maxSize = 0;
		for (MenuElement item : getPage().getAllChildrenList()) {
			String keywords = item.getKeywords(ctx);			
			if (!StringHelper.isEmpty(keywords)) {
				for (String keyword : StringHelper.stringToCollection(keywords, ",")) {
					if (!keywordSet.contains(keyword)) {
						keywordSet.add(keyword);
						countKeywords.put(keyword, 1);
						if (maxSize == 0) {
							maxSize = 1;
						}
					} else {
						int newCount = countKeywords.get(keyword) + 1;
						if (maxSize < newCount) {
							maxSize = newCount;
						}
						countKeywords.put(keyword, newCount);
					}
				}
			}
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("webaction", "search.search");
		String basicURL = URLHelper.createURL(ctx, "/", params);
		String maxSizeRAW = getConfig(ctx).getProperty("max-size", null);
		int maxListSize = Integer.MAX_VALUE;
		if (maxSizeRAW != null) {
			maxListSize = Integer.parseInt(maxSizeRAW);
		}
		Collections.shuffle(keywordSet);
		List<Link> clouds = new LinkedList<Link>();
		if (maxSize > 0) {
			for (String keyword : keywordSet) {
				Link link = new Link(basicURL + "&keywords=" + keyword, keyword);
				link.setStyle("weight-" + Math.round(countKeywords.get(keyword) * 8) / maxSize);
				clouds.add(link);
				if (clouds.size() >= maxListSize) {
					break;
				}
			}
		}
		ctx.getRequest().setAttribute("cloudTag", clouds);
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isContentTimeCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

}
