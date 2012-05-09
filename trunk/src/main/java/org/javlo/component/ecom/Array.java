/*
 * Created on 30-d�c.-2003
 */
package org.javlo.component.ecom;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.javlo.comparator.RowComparator;
import org.javlo.helper.StringHelper;


/**
 * @author pvandermaesen 
 */
public class Array {
	
	int WIDTH = 4;

	String[][] list = new String[0][0];

	public String encode(String value) {
		String res = value.replaceAll("\\|", "\\$\\{pipe\\}");
		res = res.replaceAll("\\*", "\\$\\{star\\}");
		return res;
	}

	public String decode(String value) {
		String res = value.replaceAll("\\$\\{pipe\\}", "\\|");
		res = res.replaceAll("\\$\\{star\\}", "\\*");
		return res;
	}

	public String toString() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter outWriter = new PrintWriter(out);
		for (int i = 0; i < list.length; i++) {
			outWriter.print('|');
			for (int j = 0; j < list[i].length; j++) {
				if ( j > 0 ) {
					outWriter.print('*');
				}
				outWriter.print(encode(list[i][j]));
			}
		}
		outWriter.close();
		return out.toString();
	}

	public void fromString(String in) {
		
		System.out.println("[Array.java]-[fromString]-in="+in); /*TODO: REMOVE TRACE*/
		
		list = new String[0][0];
		StringTokenizer lineToken = new StringTokenizer(in, "|");		
		while (lineToken.hasMoreTokens()) {
			String token = lineToken.nextToken();
			String[] split = StringHelper.split(token, "*");			
			addRow(split);
		}
	}

	public String getColTitle(int c) {
		return list[0][c];
	}

	public void setColTitle(int c, String value) {
		list[0][c] = value;
	}

	public String getRowTitle(int r) {
		return list[r][0];
	}

	public void setRowTitle(int r, String title) {
		list[r][0] = title;
	}

	public double getCellDoubleValue(int r, int c) {
		double res;
		try {
			res = Double.parseDouble(list[r][c]);
		} catch (NumberFormatException e) {
			res = 0;
		}
		return res;
	}
	
	public String getCellValue(int r, int c) {
		if ((list[r][c]==null)||( list[r][c].trim().length() == 0 ) ){
			return "&nbsp;";
		}
		return list[r][c];
	}

	public void setCellValue(int r, int c, String value) {
		list[r][c] = value;
	}

	public String getCellValueFormated(int r, int c, int digit) {	
		return getCellValueConvertFormated(r,c,1,digit);
	}

	public String getCellValueConvertFormated(int r, int c, double k, int digit) {
		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(digit);
		String res;
		try {
			res = "&euro; "+format.format(Double.parseDouble(getCellValue(r,c)) * k);
		} catch (NumberFormatException e) {
			res = getCellValue(r,c);			
		}
		return res;
	}

	public void addRow(String[] row) {
		int newRow = list.length;
		String[][] newList = new String[newRow + 1][getWidth()];
		System.arraycopy(list, 0, newList, 0, list.length);
		list = newList;
		for (int i = 0; i < row.length; i++) {			
			list[newRow][i] = row[i];
		}
	}

	public void addEmptyRow() {
		String[] row = new String[getWidth()];		
		for (int i = 0; i < row.length; i++) {
			row[i] = "";
		}
		addRow(row);
	}
	
	public void deleteRow ( int delRow ) {		
		String[][] newList = new String[getSize()-1][getWidth()];
		int newR = 0; 
		for (int r = 0; r < list.length; r++) {
			if ( r != delRow ) {			
				for (int c = 0; c < list[r].length; c++) {
					newList[newR][c]=list[r][c];
				}
				newR++;
			}
		}
		list=newList;
	}

	public int getSize() {
		return list.length;
	}

	public int getWidth() {
		return WIDTH;
	}
	
	public void sort ( int col ) {	
		Arrays.sort( list, 1, list.length, new RowComparator (col) );
	}
}
