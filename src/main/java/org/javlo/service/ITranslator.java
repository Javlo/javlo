package org.javlo.service;

import org.javlo.context.ContentContext;

/**
 * implementation can translate a text
 * @author user
 *
 */
public interface ITranslator {
	
	public static final String ERROR_PREFIX = "[TRANSLATION ERROR] - ";
	
	public String translate (ContentContext ctx, String text, String sourceLang, String targetLang);

}
