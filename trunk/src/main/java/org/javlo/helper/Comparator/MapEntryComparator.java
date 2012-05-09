/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.util.Comparator;
import java.util.Map;

/**
 * @author pvanderm
 *
 * This class is used for sort a array of File. 
 */
public class MapEntryComparator implements Comparator {
	
	private boolean sortOnValue = false;
	
	public MapEntryComparator (boolean inSortOnValue) {
		sortOnValue = inSortOnValue;
	}

	/**
	 * compare two array of Comparable
	 */
	public int compare(Object inFile1, Object inFile2) {
		Map.Entry<Comparable, Comparable> entry1 = (Map.Entry<Comparable, Comparable>)inFile1;
		Map.Entry<Comparable, Comparable> entry2 = (Map.Entry<Comparable, Comparable>)inFile2;
		if (sortOnValue) {
			return entry1.getValue().compareTo(entry2.getValue());
		} else {
			return entry1.getKey().compareTo(entry2.getKey());
		}
	}
	
	

}
