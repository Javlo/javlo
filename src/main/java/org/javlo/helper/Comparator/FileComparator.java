/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.io.File;
import java.util.Comparator;

/**
 * @author pvanderm
 * 
 *         This class is used for sort a array of File.
 */
public class FileComparator implements Comparator<File> {

	public static final int NAME = 1;
	public static final int LASTMODIFIED = 2;
	public static final int SIZE = 3;

	int sortType = NAME;
	boolean ascending = true;

	public FileComparator(int inSortType, boolean inAscending) {
		sortType = inSortType;
		ascending = inAscending;
	}

	/**
	 * compare two array of Comparable
	 */
	@Override
	public int compare(File file1, File file2) {
		int diff = 0;

		switch (sortType) {
			case NAME:
				diff = file1.getName().compareTo(file2.getName());
				break;
			case LASTMODIFIED:
				long timeDiff = file1.lastModified() - file2.lastModified();
				diff = timeDiff > 0 ? 1 : (timeDiff < 0 ? -1 : 0); // évite le dépassement d'int
				break;
			case SIZE:
				long sizeDiff = file1.length() - file2.length();
				diff = sizeDiff > 0 ? 1 : (sizeDiff < 0 ? -1 : 0); // évite le dépassement d'int
				break;
			default:
				return file1.compareTo(file2);
		}

		if (!ascending) {
			diff = -diff;
		}

		return diff;
	}
}
