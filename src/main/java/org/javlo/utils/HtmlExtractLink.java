package org.javlo.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class HtmlExtractLink {

	private static final String login = "arnaud@andromede.be";
	private static final String password = "";

	public static void main(String[] args) {

		try {
			// Votre URL
			URL url = new URL("https://www.hotrec.eu/members-area/");

			// Ouvrir une connexion HTTP
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// Construire la chaîne pour l'authentification de base
			String userCredentials = login+":"+password;
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));

			// Définir les propriétés de la requête
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", basicAuth);

			// Obtenir la réponse
			Document doc = Jsoup.parse(connection.getInputStream(), "UTF-8", "/");
			System.out.println(doc.title());

			// Lisez la réponse ici si nécessaire...

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
