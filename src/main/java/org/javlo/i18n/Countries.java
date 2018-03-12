package org.javlo.i18n;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Countries {

	public static Map<String, String> getCountriesList(String lang) {
		String[] locales = Locale.getISOCountries();
		Map<String, String> outList = new HashMap<String, String>();
		Locale displayLg = new Locale(lang);
		for (String countryCode : locales) {
			Locale obj = new Locale("", countryCode);
			outList.put(obj.getCountry(), obj.getDisplayCountry(displayLg));
		}
		return Collections.unmodifiableMap(outList);
	}
	
	public static void main(String[] args) {
		for (Map.Entry<String, String> c : getCountriesList("lv").entrySet()) {
			System.out.println("Country Code = " + c.getKey() + ", Country Name = " + c.getValue());
		}
	}

}
