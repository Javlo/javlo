package org.javlo.helper.Comparator;

import java.util.Comparator;

public class StringSizeComparator implements Comparator<String> {

	public int compare(String arg0, String arg1) {
		return arg1.length()-arg0.length();
	}

}
