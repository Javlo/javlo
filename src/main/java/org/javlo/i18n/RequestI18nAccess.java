package org.javlo.i18n;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.ReverseLinkService;

/**
 * TODO: create class with reverse link on messages
 * @author user
 *
 */
public class RequestI18nAccess implements Map<String, String> {
	
	private Map<String,String> internalMap = null;
	private ContentContext ctx;
	private boolean attribute = false;
	
	public RequestI18nAccess (ContentContext ctx, I18nAccess i18nAccess, boolean attribute) {
		this.internalMap = i18nAccess.getView();		
		this.ctx = ctx;
		this.attribute = attribute;
	}

	@Override
	public int size() {
		return internalMap.size();
	}

	@Override
	public boolean isEmpty() {
		return internalMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return internalMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return internalMap.containsKey(value);
	}

	@Override
	public String get(Object key) {
		String value = internalMap.get(key);
		if (value != null) {
			try {
				value = ReverseLinkService.getInstance(ctx.getGlobalContext()).replaceLink(ctx, null, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (ctx.isPreview() && !attribute) {
			String id="vi18n-"+StringHelper.getRandomId();
			String jsCopy = "navigator.clipboard.writeText(document.getElementById('"+id+"').getAttribute('title'));";
			return "<span id=\""+id+"\" onclick=\""+jsCopy+"\" class=\"preview-vi18n\" title=\""+key+"\">"+value+"</span>";
		} else {
			return value;
		}
	}

	@Override
	public String put(String key, String value) {
		return null;
	}

	@Override
	public String remove(Object key) {
		return null;
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
	}

	@Override
	public void clear() {
	}

	@Override
	public Set<String> keySet() {
		return internalMap.keySet();
	}

	@Override
	public Collection<String> values() {
		return internalMap.values();
	}

	@Override
	public Set<Entry<String, String>> entrySet() {
		return internalMap.entrySet();
	}
}
