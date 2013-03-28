package org.javlo.helper.Comparator;

import java.util.Comparator;

import org.javlo.helper.StringHelper;

public class StringComparator implements Comparator<String> {

	public int compare(String arg0, String arg1) {
		String str1 = StringHelper.createASCIIString(arg0).toLowerCase();
		String str2 = StringHelper.createASCIIString(arg1).toLowerCase();
		return str1.compareTo(str2);
	}

}
