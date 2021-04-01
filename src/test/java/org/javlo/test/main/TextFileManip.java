package org.javlo.test.main;

import java.io.File;
import java.io.IOException;

import org.javlo.helper.ResourceHelper;

public class TextFileManip {
	
	public static void main(String[] args) throws IOException {
		String html = ResourceHelper.loadStringFromFile(new File("c:/trans/meps.html"));
		String prefix = "http://www.europarl.europa.eu/meps/fr/";
		int latestIndex = 0;
		while (html.indexOf(prefix, latestIndex+1)>0) {
			latestIndex = html.indexOf(prefix, latestIndex+1);
			String id = html.substring(latestIndex+prefix.length(), latestIndex+prefix.length()+6);
			id = id.replace('"', ' ').trim();
			System.out.println("http://www.myepdocspp.ep.parl.union.eu/myepdocs/?mep="+id);
		}
	}

}
