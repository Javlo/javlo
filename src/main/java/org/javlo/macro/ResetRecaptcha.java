package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.form.SmartGenericForm;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class ResetRecaptcha extends AbstractMacro {

	@Override
	public String getName() {
		return "reset-recaptcha";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		MenuElement root = content.getNavigation(ctx);
		int removeCount = 0;
		for (MenuElement child : root.getAllChildrenList()) {
			for (IContentVisualComponent comp : child.getContentByType(ctx, SmartGenericForm.TYPE)) {
				removeCount++;
				((SmartGenericForm)comp).getLocalConfig(false).remove(SmartGenericForm.RECAPTCHAKEY);
				((SmartGenericForm)comp).getLocalConfig(false).remove(SmartGenericForm.RECAPTCHASECRETKEY);
				((SmartGenericForm)comp).store(ctx);
			}
		}
		return "remove recaptcha from " + removeCount + " components.";
	}

	@Override
	public boolean isPreview() {
		return false;
	}

}
