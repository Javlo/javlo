package org.javlo.macro;

import java.util.Collection;
import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

public class CurrentLgExpandMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "current-language-expand";
	}

	protected boolean expand(ContentContext ctx, IContentVisualComponent baseComp, IContentVisualComponent targetComp) {
		if (baseComp.getType().equals(GlobalImage.TYPE) && targetComp.getType().equals(GlobalImage.TYPE)) {
			GlobalImage srcComp = (GlobalImage) baseComp;
			GlobalImage trgComp = (GlobalImage) targetComp;

			if (srcComp.getFileName(ctx).equals(trgComp.getFileName(ctx))) {
				trgComp.setLink(srcComp.getLink());
				trgComp.setStyle(ctx, srcComp.getComponentCssClass(ctx));
				trgComp.setModify();
				trgComp.storeProperties();
				return true;
			}
		}
		return false;
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		ContentContext defaultLgContext = new ContentContext(ctx);
		// defaultLgContext.setLanguage(globalContext.getDefaultLanguage());
		// defaultLgContext.setRequestContentLanguage(globalContext.getDefaultLanguage());
		ContentContext lgCtx = new ContentContext(ctx);
		int modif = 0;

		MenuElement currentPage = ctx.getCurrentPage();
		ContentElementList defaultLgContent = currentPage.getContent(defaultLgContext);
		Collection<String> lgs = globalContext.getContentLanguages();
		for (String lg : lgs) {
			if (!lg.equals(defaultLgContext.getLanguage())) {
				lgCtx.setRequestContentLanguage(lg);
				ContentElementList content = currentPage.getContent(lgCtx);
				while (content.hasNext(defaultLgContext)) {
					defaultLgContent.initialize(ctx);
					IContentVisualComponent comp = content.next(lgCtx);
					while (defaultLgContent.hasNext(lgCtx)) {
						if (expand(ctx, defaultLgContent.next(defaultLgContext), comp)) {
							modif++;
						}
					}
				}
			}
		}

		String msg = modif + " component(s) identified as similar.";
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setAskStore(true);

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
	

}
