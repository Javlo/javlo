package org.javlo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

import fr.opensagres.xdocreport.core.io.internal.ByteArrayOutputStream;

public class ReadLog {

	public ReadLog() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {

		File file = new File("c:/trans/catalina_200000000.out");
		// File file = new File("c:/trans/catalina_10000000.out");
		BufferedReader reader;
		int errorLine = 1;
		try {

			reader = new BufferedReader(new FileReader(file));
			String targetLine = reader.readLine();
			int target = 0;
			while (targetLine != null) {
				if (targetLine.contains("Volný pohyb osob, zboží a služeb je jedním z nejvyužívanějších práv v Evropské unii.")) {
					target++;
				}
				targetLine = reader.readLine();
			}
			System.out.println("target=" + target);

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);

			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			int c = 0;
			int er = 0;
			int specialLineFound = 0;
			while (line != null) {

				if (line.contains("org.javlo.component.form.SmartGenericForm performSubmit")) {
					String date = '"' + line.substring(0, 24).trim() + '"';

					for (int j = 0; j < 21; j++) {
						j++;
						line = reader.readLine();
						errorLine++;
					}

					String emailLine = line;
					List<String> email = StringHelper.extractItem(line, "\">", "</a><br />");
					if (email.size() == 0) {
						line = line.trim();
						if (line.contains("@") && line.endsWith("<br />")) {
							email = Arrays.asList(line.replace("<br />", ""));
							specialLineFound++;
						}
					}
					if (email.size() == 1) {
						emailLine = email.get(0);
					}
					
					int lineNumber=0;
					while (!line.contains("Volný pohyb osob, zboží a služeb je jedním z nejvyužívanějších práv v Evropské unii.") && lineNumber<25) {
						lineNumber++;
						line = reader.readLine();
					}
 					
					if (lineNumber < 24) {
						c++;
						out.println();
						out.print(date + ",");
						out.print('"' + emailLine + '"' + ",");
						/*for (int i = 0; i < 12; i++) {
							line = reader.readLine();
							errorLine++;
						} // read 12 lines*/
						for (int i = 0; i < 9; i++) {							
							errorLine++;
							try {
								out.print('"' + emailLine.replace("\"", "'") + '"' + ",");
							} catch (Throwable t) {
								// System.out.println("ERROR in : " + line);
								out.print("\"ERROR\",");
							}
							line = reader.readLine();
						}
					}
				}

				line = reader.readLine();
				errorLine++;
			}
			System.out.println("***** ReadLog.main : found entry = " + c);
			System.out.println("***** ReadLog.main : bad entry = " + er);
			System.out.println("***** ReadLog.main : specialLineFound = " + specialLineFound); // TODO:
																								// remove
																								// debug
																								// trace
			reader.close();
			out.close();
			ResourceHelper.writeStringToFile(new File("c:/trans/euroscola_result.csv"), new String(outStream.toByteArray()), "UTF-8");

		} catch (Exception e) {
			System.out.println("***** ReadLog.main : errorLine = " + errorLine); // TODO:
																					// remove
																					// debug
																					// trace
			e.printStackTrace();
		}
	}

}
