/*
 * Created on 10-janv.-2004
 */
package org.javlo.comparator;

import java.util.Comparator;

/**
 * @author pvandermaesen
 * Compare two lines of a matrice on a define
 */
public class RowComparator implements Comparator<Object[]> {

	int c = 0;

	public RowComparator(int col) {
		c = col;
	}

	public int compare(Object[] array1, Object[] array2) {
		String s1 = array1[c].toString();
		String s2 = array2[c].toString();
		try {
			Double d1 = new Double(s1);
			Double d2 = new Double(s2);
			
			return d1.compareTo(d2);
		} catch (NumberFormatException e) {		
			return s1.compareTo(s2);
		}
	}
}
