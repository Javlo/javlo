package org.javlo.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;


public class MergeProperties {
	
	public static final String getKey(String line) {
		if (line.indexOf("=") >= 0) {
			return line.substring(0, line.indexOf("="));
		} else {
			return "";
		}
	}
	
	public static final String getValue(String line) {
		if (line.indexOf("=") >= 0) {
			return line.substring(line.indexOf("=")+1);
		} else {
			return line;
		}
	}
	
	public static void mergeProperties (File file1, File file2) throws IOException {
		InputStream in1 = new FileInputStream(file1);
		BufferedReader reader1 = new BufferedReader(new InputStreamReader(in1));
		
		Properties prop2 = new Properties();
		InputStream inProp = new FileInputStream(file2);
		prop2.load(inProp);
		inProp.close();
		
		StringWriter outStr2 = new StringWriter();		
		BufferedWriter out2 = new BufferedWriter(outStr2);
		
		Queue<String> lineToBeInsered = new LinkedList<String>();
		Queue<Integer> positionOfInsertion = new LinkedList<Integer>();
		
		String line1 = reader1.readLine();
		int insertPos = 0;
		while ((line1 != null)) {
			if (!prop2.containsKey(getKey(line1))) {
				if (getKey(line1).trim().length() > 0) {
					lineToBeInsered.offer(getKey(line1)+"=[i18n]-"+getValue(line1));
					System.out.println("to add : "+getKey(line1)+"=[i18n]-"+getValue(line1));
					positionOfInsertion.offer(insertPos);
				}
			} else {
				insertPos=insertPos+1;
			}
			line1 = reader1.readLine();			
		}
		
		int pos = 0;
		String nextInsertion = lineToBeInsered.poll();
		int nextInsertionPos = positionOfInsertion.poll();
		
		InputStream in2 = new FileInputStream(file2);
		BufferedReader reader2 = new BufferedReader(new InputStreamReader(in2));
		String line2 = reader2.readLine();
		while (line2 != null) {
			int inseredLine = 0;
			while ((nextInsertion != null)&&(pos == nextInsertionPos)) {
				out2.append(nextInsertion);
				System.out.println("add  : "+nextInsertion);
				out2.newLine();
				nextInsertion = lineToBeInsered.poll();
				if (nextInsertion != null) {
					nextInsertionPos = positionOfInsertion.poll();
				}
				inseredLine=inseredLine+1;
			}			
			out2.append(line2);
			out2.newLine();
			line2 = reader2.readLine();
			pos = pos + 1;
		}
		out2.close();
		ResourceHelper.writeStringToFile(file2, outStr2.toString(), ContentContext.CHARACTER_ENCODING);
	}
	
	public static void main(String[] args) {
		File file1 = new File("C:/p/work/dc/DC/src/WEB-INF/i18n/edit_fr.properties");
		File file2 = new File("C:/p/work/dc/DC/src/WEB-INF/i18n/edit_en.properties");
		try {
			mergeProperties(file1, file2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*file1 = new File("C:/p/work/dc/DC/src/WEB-INF/i18n/view_fr.properties");
		file2 = new File("C:/p/work/dc/DC/src/WEB-INF/i18n/view_en.properties");
		try {
			mergeProperties(file1, file2);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		System.out.println("DONE.");
	}
	
}
