package org.javlo.service.google.translation;

import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;

public class TranslatorFactory {	
	public static ITranslator getTranslator(GlobalContext globalContext) {
		if (!StringHelper.isEmpty(globalContext.getSpecialConfig().getTranslatorGoogleApiKey())) {
			return GoogleTranslateService.getTranslator();
		}
		if (!StringHelper.isEmpty(globalContext.getSpecialConfig().getTranslatorDeepLApiKey())) {
			return DeepLTranslateService.getTranslator();
		}
		return null;
	}
}
