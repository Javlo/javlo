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
	
	public static void addCol(String[][] array, String colName) {
		if (array.length == 0) {
			return;
		} else {
			for (int i = 0; i < array.length; i++) {
				String[] line = array[i];
				String[] newLine = new String[array[i].length+1];				
				for (int j = 0; j < line.length; j++) {
					newLine[j] = line[j];					
				}
				array[i] = newLine;
				newLine[newLine.length-1] = "";
			}
			array[0][array[0].length-1]=colName;
		}
	}

}
