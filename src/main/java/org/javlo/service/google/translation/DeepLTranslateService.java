package org.javlo.service.google.translation;

import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.utils.TimeMap;
import org.jcodec.common.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeepLTranslateService implements ITranslator {

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DeepLTranslateService.class.getName());

	private static URL DEEPL_URL = null;
	
	private static final DeepLTranslateService INSTANCE = new DeepLTranslateService();

	private static final Map<String, String> cache = Collections.synchronizedMap(new TimeMap<String, String>(60 * 60 * 24 * 355, 1000000));

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
		if (StringHelper.isEmpty(sourceText) || StringHelper.isDigit(sourceText)) {
			return sourceText;
		}

		// Extraction des SVG
		Map<String, String> svgMap = new LinkedHashMap<>();
		Pattern svgPattern = Pattern.compile("<svg[^>]*>.*?</svg>", Pattern.DOTALL);
		Matcher matcher = svgPattern.matcher(sourceText);
		int counter = 0;
		StringBuffer textWithoutSVG = new StringBuffer();

		while (matcher.find()) {
			String svg = matcher.group();
			String token = "#SVG-" + counter + "#";
			svgMap.put(token, svg);
			matcher.appendReplacement(textWithoutSVG, token);
			counter++;
		}
		matcher.appendTail(textWithoutSVG);

		String processedText = textWithoutSVG.toString();

		String cacheKey = processedText + "__" + sourceLang + "__" + targetLang;
		String translation = cache.get(cacheKey);

		if (translation == null) {
			String query = "text=" + encode(processedText);
			query += "&source_lang=" + encode(sourceLang);
			query += "&target_lang=" + encode(targetLang);
			query += "&auth_key=" + encode(apiKey);

			URL deeplURL = new URL(URLHelper.addParams(getGoogleUrl().toString(), query));
			logger.info("call deepl for : " + targetLang);
			String json = NetHelper.readPage(deeplURL);

			if (json == null) {
				Logger.error("error read page : " + deeplURL);
			} else {
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(json);
				translation = "" + ((JSONObject) (((JSONArray) jsonObject.get("translations")).get(0))).get("text");
				cache.put(cacheKey, translation);
			}
		} else {
			logger.info("deepl found in cache for : " + targetLang);
		}

		// Remplacement des tokens par les SVG
		for (Map.Entry<String, String> entry : svgMap.entrySet()) {
			translation = translation.replace(entry.getKey(), entry.getValue());
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
