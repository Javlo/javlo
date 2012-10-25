package org.javlo.search;

import javax.servlet.http.HttpSession;

public class SearchFilter {

	public static final SearchFilter getInstance(HttpSession session) {
		String key = "searchFilter";
		SearchFilter filter = (SearchFilter) session.getAttribute(key);
		if (filter == null) {
			filter = new SearchFilter();
			session.setAttribute(key, filter);
		}
		return filter;
	}

	private String rootPageName;

	private String tag;

	private String mount;

	public String getRootPageName() {
		return rootPageName;
	}

	public void setRootPageName(String inRootPageName) {
		if (inRootPageName != null) {
			if (inRootPageName.trim().length() == 0) {
				this.rootPageName = null;
			} else {
				this.rootPageName = inRootPageName;
			}
		}
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		if (tag != null) {
			if (tag.trim().length() == 0) {
				this.tag = null;
			} else {
				this.tag = tag;
			}
		}
	}

	public String getMount() {
		return mount;
	}

	public void setMount(String mount) {
		this.mount = mount;
	}

}
