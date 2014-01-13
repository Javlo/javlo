package org.javlo.helper.Comparator;

import java.util.Comparator;

import org.javlo.helper.StringHelper;

public class StringComparator implements Comparator<String> {

	public static int compareText(String inStr1, String inStr2) {
		String str1 = StringHelper.createASCIIString(inStr1).toLowerCase();
		String str2 = StringHelper.createASCIIString(inStr2).toLowerCase();
		return str1.compareTo(str2);
	}

	@Override
	public int compare(String arg0, String arg1) {
		return compareText(arg0, arg1);
	}

}
