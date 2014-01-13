/*
 * Created on 10-janv.-2004
 */
package org.javlo.comparator;

import java.util.Comparator;

/**
 * @author pvandermaesen
 * compare two liste title as "4.5.4" and "4.6", the second is biggest.
 */
public class ListTitleComparator implements Comparator {

	public ListTitleComparator() {
	}

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {
		int res = 0;
		if (!arg0.equals(arg1)) {
			String[] str0 = ((String)arg0).split("\\.");
			String[] str1 = ((String)arg1).split("\\.");

			res = -1;
			int length = str0.length;
			if (length > str1.length) {
				res = 1;
				length = str1.length;
			}
			int i;
			for (i = 0;(i < length) && (str0[i].equals(str1[i])); i++);
			if (i < length) {
				res = 1;
				if (Integer.parseInt(str0[i]) < Integer.parseInt(str1[i])) {
					res = -1;
				}
			}

		}
		return res;
	}

}
