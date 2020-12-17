package org.javlo.service.google.translation;

import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;

public class TranslatorFactory {
	
	public static ITranslator getTranslator(GlobalContext globalContext) {
		if (!StringHelper.isEmpty(globalContext.getGoogleApiKey())) {
			return GoogleTranslateService.getTranslator();
		}
		if (!StringHelper.isEmpty(globalContext.getDeepLApiKey())) {
			return DeepLTranslateService.getTranslator();
		}
		return null;
	}
	

}
