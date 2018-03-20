package org.javlo.service.google.translation;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.ITranslator;
import org.javlo.utils.JSONMap;
import org.javlo.utils.TimeMap;
import org.owasp.encoder.Encode;

public class GoogleTranslateService implements ITranslator {

	private static URL GOOGLE_URL = null;
	
	private static final GoogleTranslateService INSTANCE = new GoogleTranslateService();

	private static final Map<String, String> cache = Collections.synchronizedMap(new TimeMap<String, String>(60 * 60 * 24 * 355, 10000));

	private static final URL getGoogleUrl() throws MalformedURLException {
		if (GOOGLE_URL == null) {
			GOOGLE_URL = new URL("https://translation.googleapis.com/language/translate/v2");
		}
		return GOOGLE_URL;
	}
	
	private static String encode(String text) throws UnsupportedEncodingException {
		return URLEncoder.encode(text, ContentContext.CHARACTER_ENCODING);
	}

	/**
	 * Translate the source text from source to target language.
	 *
	 * @param sourceText
	 *            source text to be translated
	 * @param sourceLang
	 *            source language of the text
	 * @param targetLang
	 *            target language of translated text
	 * @throws Exception 
	 */
	public static String translate(String sourceText, String sourceLang, String targetLang, String apiKey) throws Exception {
		String cacheKey = sourceText + sourceLang + targetLang;
		String translation = cache.get(cacheKey);
		if (translation == null) {			
			String query ="q="+encode(sourceText);
			query+="&source="+encode(sourceLang);
			query+="&target="+encode(targetLang);
			query+="&format=text";
			query+="&key="+encode(apiKey);
			URL googleURL = new URL (URLHelper.addParams(getGoogleUrl().toString(), query));			
			String json = NetHelper.readPage(googleURL);			
			JSONMap data = JSONMap.parseMap(json).getMap("data");
			List translations = (List)data.get("translations");
			translation = ""+((JSONMap)translations.get(0)).get("translatedText");
			translation = translation.replace("</ ", "</"); // strange google change close tag ?
			cache.put(cacheKey, translation);
		}
		return translation;
	}
	
	public static final ITranslator getTranslator() {
		return INSTANCE;
	}

	@Override
	public String translate(ContentContext ctx, String text, String sourceLang, String targetLang) {
		if (StringHelper.isEmpty(text) || StringHelper.isDigit(text)) {
			return text;
		}
		try {
			return translate(text, sourceLang, targetLang, ctx.getGlobalContext().getGoogleApiKey());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		System.out.println(">>>>>>>>> GoogleTranslateService.main : encode = "+encode("c'est moi & patrick toi")); //TODO: remove debug trace
		
		System.out.println("translated : "+translate("<p>Silence, ça tourne. La prochaine création originale de Canal +, \"<em>Vernon Subutex</em>\", vient de débuter son tournage et se déroulera entre Paris, Cannes et Barcelone, du 19 mars au 23 juin 2018, selon un communiqué de la chaîne. La nouvelle série, inspirée de la trilogie éponyme de Virginie Despentes, comptera neuf épisodes de 30 minutes et verra l'acteur Romain Duris dans la peau du héros.</p>\r\n" + 
				"<p> </p>", "fr", "en", "AIzaSyBJ9W5-MZss8VXWFRlk6cjIPzjAX7czv3w"));
	}

}
