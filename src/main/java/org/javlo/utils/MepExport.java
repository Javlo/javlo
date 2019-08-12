package org.javlo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MepExport {
	
	private static final List<String> COUNTRIES = Arrays.asList(new String[] {"be", "fr", "de"});
	
	public static String getJsonMepByContries(String lg, String country) throws IOException, ParseException {
		URL url = new URL("http://www.europarl.europa.eu/meps/rest/mepsforcountry/"+lg+"/"+country);
		JSONParser jsonParser = new JSONParser();
		InputStream in = url.openConnection().getInputStream();
		Object obj = jsonParser.parse(new InputStreamReader(in));
		 JSONArray employeeList = (JSONArray) obj;
         System.out.println(employeeList);
         return null;
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		getJsonMepByContries("en", "be");
	}

}
