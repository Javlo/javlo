package org.javlo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
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
		BufferedReader reader;

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			int c = 0;
			while (line != null) {

				if (line.contains("org.javlo.component.form.SmartGenericForm performSubmit")) {					
					String date = '"'+line.substring(0,24).trim()+'"';
					for (int i = 0; i < 11; i++) {
						line = reader.readLine();
					} // read 11 lines					
					List<String> email = StringHelper.extractItem(line, ">", "</a><br />");
					if (email.size()==1) {
						c++;
						out.println();
						out.print(date + ",");
						out.print('"'+StringHelper.extractItem(line, ">", "</a><br />").get(0) + '"' +",");
						for (int i = 0; i < 12; i++) {
							line = reader.readLine();
						} // read 12 lines				
						for (int i = 0; i < 9; i++) {
							line = reader.readLine();
							out.print('"'+StringHelper.extractItem(line, ";\">", "</span></li>").get(0).replace("\"", "'") + '"' +",");
						}
					}
				}

				line = reader.readLine();
			}
			System.out.println("***** ReadLog.main : found entry = " + c); // TODO:
																			// remove
																			// debug
																			// trace
			reader.close();
			out.close();
			ResourceHelper.writeStringToFile(new File("c:/trans/euroscola_result.csv"), new String(outStream.toByteArray()), "UTF-8");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
