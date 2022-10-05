package org.javlo.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HTMLExtract {

	public static void extractAria() throws MalformedURLException, Exception {
		String url = "https://www.europarl.europa.eu/about-parliament/en/";

		String propFile = "C:/work/template/galaxy-template/view_en.properties";

		Document doc = Jsoup.connect(url).get();
		System.out.println(doc.title());

		Elements langItems = doc.select("nav .menu-content li a");
		for (int i = 0; i < langItems.size(); i++) {
			String lg = langItems.get(i).attr("lang");
			Document doci18n = Jsoup.connect(url.replace("/en/", "/" + lg + "/")).get();
			Elements navItems = doci18n.select("#informationlinks-title .ep_name");
			if (navItems.size() != 1) {
				System.out.println("ERROR : #navItems = " + navItems.size());
			} else {
				
				String data = navItems.first().text();
				//String data = navItems.first().attr("aria-label");

				System.out.println("aria [" + lg + "] : " + data);

				StructuredProperties p = new StructuredProperties();
				try (FileInputStream in = new FileInputStream(propFile.replace("_en", "_" + lg))) {
					p.load(in);
				}
				p.setProperty("aria.info-link", data);
				try (FileOutputStream in = new FileOutputStream(propFile.replace("_en", "_" + lg))) {
					p.store(in, null);
				}
			}
		}
	}

	public static void main(String[] args) throws MalformedURLException, Exception {
		extractAria();
	}

}
