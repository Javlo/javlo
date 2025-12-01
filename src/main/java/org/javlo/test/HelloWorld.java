package org.javlo.test;

import java.io.Serializable;
import java.util.Locale;

public class HelloWorld implements Serializable {
	



private static final long serialVersionUID = 1L;

	public String getMessage() {
		return "Hello word!";
	}

	public static void main(String[] args) {
		String locale = (new Locale("en")).toString();
		if (locale.length() == 2) {
			locale = locale + "-" + locale.toUpperCase();
		}
		System.out.println(locale);
	}

}
