/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.util.Comparator;

/**
 * @author pvanderm
 *
 * this class is used for sort a array of array 
 */
public class DoubleArrayComparator implements Comparator {
	
	private int sortIndex = 0;
	private int multiply = 1;
	
	public DoubleArrayComparator ( int newSortIndex ) {
		sortIndex = newSortIndex;
	}
	
	public DoubleArrayComparator ( int newSortIndex, boolean ascending ) {
		sortIndex = newSortIndex;
		if (!ascending) {
			multiply = -1;
		}
	}

	/**
	 * compare two array of Comparable
	 */
	public int compare(Object arg0, Object arg1) {
		if ((arg0 == null)) {
			return multiply;
		} else if (arg1 == null) {
			return -multiply;
		}
		
		Object[] a1 = (Object[])arg0;
		Object[] a2 = (Object[])arg1;
		
		if ((a1[sortIndex] == null)) {
			return 1;
		} else if (a2[sortIndex] == null) {
			return -1;
		}
		
		return ((Comparable)a1[sortIndex]).compareTo(a2[sortIndex])*multiply;
	}
	
	

}
