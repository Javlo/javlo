package org.javlo.service.shared;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import org.javlo.component.core.ComponentBean;
import org.javlo.helper.StringHelper;

public abstract class AbstractSharedContentProvider implements ISharedContentProvider {

	private String name;
	private URL url;
	private Map<String, String> categories = new HashMap<String, String>();

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLabel(Locale locale) {
		return getName();
	}

	@Override
	public URL getURL() {
		return url;
	}

	public void setURL(URL url) {
		this.url = url;
	}

	@Override
	public abstract Collection<SharedContent> getContent();

	@Override
	public Collection<SharedContent> searchContent(String query) {
		Collection<SharedContent> outList = new HashSet<SharedContent>();
		query = StringHelper.createFileName(query);
		for (SharedContent content : getContent()) {
			if (content.getContent() != null) {
				for (ComponentBean bean : content.getContent()) {
					if (bean != null && !outList.contains(content) && StringHelper.createFileName(bean.getValue()).contains(query)) {
						outList.add(content);
					}
				}
			}
			if (!outList.contains(content) && StringHelper.createFileName(content.getTitle() + ' ' + content.getDescription()).contains(query)) {
				outList.add(content);
			}
		}
		return outList;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Map<String, String> getCategories() {
		return categories;
	}

	public void setCategories(Map<String, String> categories) {
		this.categories = categories;
	}

	@Override
	public Collection<SharedContent> getContent(Collection<String> categories) {
		if (getCategories().size() <= 1 || categories == null || categories.size() == 0) {
			return getContent();
		}
		Collection<SharedContent> outList = new HashSet<SharedContent>();
		for (SharedContent content : getContent()) {
			if (!Collections.disjoint(content.getCategories(), categories)) {
				outList.add(content);
			}
		}
		return outList;
	}

	@Override
	public boolean isEmpty() {
		return getContent().size() == 0;
	}

	@Override
	public void refresh() {
	}

	@Override
	public String getType() {
		return TYPE_DEFAULT;
	}
	
	@Override
	public boolean isSearch() {	
		return true;
	}

}
