package org.javlo.helper;

public class ArrayHelper {

	/**
	 * insert a value at the fist position in a array of String
	 * @param inArray a Array of String
	 * @param firstValue the value of the first element
	 * @return a Array with a new first element (size+1)
	 */
	public static final String[] addFirstElem(String[] inArray, String firstValue) {
		String[] outArray = new String[inArray.length + 1];
		outArray[0] = firstValue;
		for (int i = 0; i < inArray.length; i++) {
			outArray[i + 1] = inArray[i];
		}
		return outArray;
	}

}
