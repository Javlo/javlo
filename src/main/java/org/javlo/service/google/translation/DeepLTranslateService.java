package org.javlo.service.google.translation;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.utils.TimeMap;
import org.jcodec.common.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DeepLTranslateService implements ITranslator {

	private static URL DEEPL_URL = null;
	
	private static final DeepLTranslateService INSTANCE = new DeepLTranslateService();

	private static final Map<String, String> cache = Collections.synchronizedMap(new TimeMap<String, String>(60 * 60 * 24 * 355, 10000));

	private static final URL getGoogleUrl() throws MalformedURLException {
		if (DEEPL_URL == null) {
			DEEPL_URL = new URL("https://api.deepl.com/v2/translate");
		}
		return DEEPL_URL;
	}
	
	private static String encode(String text) throws UnsupportedEncodingException {
		return URLEncoder.encode(text, ContentContext.CHARACTER_ENCODING);
	}

	/**
	 * Translate the source text from source to target language.
	 *
	 * @param sourceText	s
	 *            source text to be translated
	 * @param sourceLang
	 *            source language of the text
	 * @param targetLang
	 *            target language of translated text
	 * @throws Exception 
	 */
	public static String translate(String sourceText, String sourceLang, String targetLang, String apiKey) throws Exception {
		System.out.println(">>>>>>>>> DeepLTranslateService.translate : sourceLang = "+sourceLang); //TODO: remove debug trace
		System.out.println(">>>>>>>>> DeepLTranslateService.translate : targetLang = "+targetLang); //TODO: remove debug trace
		if (StringHelper.isEmpty(sourceText) || StringHelper.isDigit(sourceText)) {
			return sourceText;
		}
		String cacheKey = sourceText + sourceLang + targetLang;
		String translation = cache.get(cacheKey);
		if (translation == null) {			
			String query ="text="+encode(sourceText);
			query+="&source_lang="+encode(sourceLang);
			query+="&target_lang="+encode(targetLang);
			query+="&auth_key="+encode(apiKey);
			URL deeplURL = new URL (URLHelper.addParams(getGoogleUrl().toString(), query));
			String json = NetHelper.readPage(deeplURL);
			if (json == null) {
				Logger.error("error read page : "+ deeplURL);
			} else {
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(json);
				translation = ""+((JSONObject)(((JSONArray)jsonObject.get("translations")).get(0))).get("text");
				cache.put(cacheKey, translation);
			}
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
			return translate(text, sourceLang, targetLang, ctx.getGlobalContext().getSpecialConfig().getTranslatorDeepLApiKey());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		String key = "";
		String trad = translate("Immanence assure l’installation et l’entretien de sites et applications web basées sur Javlo.", "fr", "en", key);
		System.out.println(trad);
	}




}
