package org.javlo.data;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * create map with all translation for language
 * 
 * @author pvand
 *
 */
public class LanguageMap implements Map<String, String> {

	private Locale locale = new Locale("en");

	public String getLang() {
		return locale.getLanguage();
	}

	public void setLang(String lang) {
		if (lang == null) {
			locale = null;
		} else {
			locale = new Locale(lang);
		}
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		return (""+key).length()==2;
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public String get(Object key) {
		Locale l = new Locale(""+key);
		if (locale != null) {
			return l.getDisplayLanguage(locale);
		} else {
			return l.getDisplayLanguage(l);
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
		return null;
	}

	@Override
	public Collection<String> values() {
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		return null;
	}

	

}
