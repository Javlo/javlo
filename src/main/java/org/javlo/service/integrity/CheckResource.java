package org.javlo.service.integrity;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.ztatic.IStaticContainer;
import org.javlo.ztatic.StaticInfo;

public class CheckResource extends AbstractIntegrityChecker {

	public CheckResource() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean checkPage(ContentContext ctx, MenuElement page) throws Exception {
		ContentContext noAreaCtx = ctx.getContextWithArea(null);
		int error = 0;
		Collection<File> errorFile = new LinkedList<File>();
		for (IContentVisualComponent comp : page.getContent(noAreaCtx).asIterable(noAreaCtx)) {
			if (comp instanceof IStaticContainer) {
				IStaticContainer staticContainer = (IStaticContainer) comp;
				List<File> files = staticContainer.getFiles(noAreaCtx);
				if (files != null) {
					for (File file : files) {
						StaticInfo info = StaticInfo.getInstance(noAreaCtx, file);
						if (StringHelper.isEmpty(info.getTitle(noAreaCtx))) {
							error++;
							errorFile.add(file);
						}
					}
				}
			}
		}
		if (error > 0) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			setErrorMessage(i18nAccess.getText("integrity.error.resource_without_title", "Resources without title (" + errorFile.iterator().next().getName() + ')'));
			setErrorCount(error);
			setLevel(WARNING_LEVEL);
			return false;
		} else {
			return true;
		}
	}

}
