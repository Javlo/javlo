/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.io.File;
import java.util.Comparator;

/**
 * @author pvanderm
 *
 * This class is used for sort a array of File. 
 */
public class FileComparator implements Comparator<File> {
	
	public static final int NAME = 1;
	public static final int LASTMODIFIED = 2;
	public static final int SIZE = 3;
	
	int sortType = NAME;
	boolean ascending = true;

	public FileComparator ( int inSortType, boolean inAscending ) {
		sortType = inSortType;
		ascending = inAscending;
	}

	/**
	 * compare two array of Comparable
	 */
	public int compare(File file1, File file2) {
		switch (sortType) {
		case NAME:
			if (ascending) {
				return file1.getName().compareTo(file2.getName());
			} else {
				return -(file1.getName().compareTo(file2.getName()));
			}
		case LASTMODIFIED:
			if (ascending) {
				return (int)(file1.lastModified()-file2.lastModified());
			} else {
				return (int)(file2.lastModified()-file1.lastModified());
			}
		case SIZE:
			if (ascending) {
				return (int)(file1.length()-file2.length());
			} else {
				return (int)(file2.length()-file1.length());
			}
		default:
			return file1.getName().compareTo(file1.getName());
		}
	}
	
	

}
