package org.javlo.service.google.translation;

import org.javlo.context.ContentContext;

/**
 * implementation can translate a text
 * @author user
 *
 */
public interface ITranslator {
	
	public static final String ERROR_PREFIX = "[TRANSLATION ERROR] - ";

	/** last error returned by the translation server, for the current thread */
	static final ThreadLocal<String> LAST_ERROR = new ThreadLocal<String>();

	public String translate (ContentContext ctx, String text, String sourceLang, String targetLang);

	public String getName();

	static void setLastError(String sourceLang, String targetLang, String message) {
		LAST_ERROR.set("error " + sourceLang + ">" + targetLang + " : " + message);
	}

	static void clearLastError() {
		LAST_ERROR.remove();
	}

	/**
	 * error prefix with the last error message of the server (consumed).
	 * ex: [TRANSLATION ERROR] error fr>nl : target_lang not found -
	 */
	default String getErrorPrefix() {
		String error = LAST_ERROR.get();
		LAST_ERROR.remove();
		if (error == null || error.trim().isEmpty()) {
			return ERROR_PREFIX;
		}
		return "[TRANSLATION ERROR] " + error + " - ";
	}

}
