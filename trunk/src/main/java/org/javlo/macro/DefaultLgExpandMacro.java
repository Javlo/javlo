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
import org.javlo.service.ContentService;

public class DefaultLgExpandMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "default-language-expand";
	}

	protected boolean expand(ContentContext ctx, IContentVisualComponent baseComp, IContentVisualComponent targetComp) {
		if (baseComp.getType().equals(GlobalImage.TYPE) && targetComp.getType().equals(GlobalImage.TYPE)) {
			GlobalImage srcComp = (GlobalImage)baseComp;
			GlobalImage trgComp = (GlobalImage)targetComp;
			
			if (srcComp.getFileName().equals(trgComp.getFileName())) {
				trgComp.setLink(srcComp.getLink());
				trgComp.setStyle(ctx, srcComp.getStyle(ctx));
				return true;
			}
		}
		return false;
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		MenuElement root = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx);
		MenuElement[] children = root.getAllChilds();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		ContentContext defaultLgContext = new ContentContext(ctx);
		defaultLgContext.setLanguage(globalContext.getDefaultLanguage());
		defaultLgContext.setRequestContentLanguage(globalContext.getDefaultLanguage());
		ContentContext lgCtx = new ContentContext(ctx);
		int modif = 0;
		for (MenuElement child : children) {
			ContentElementList defaultLgContent = child.getContent(defaultLgContext);
			Collection<String> lgs = globalContext.getContentLanguages();
			for (String lg : lgs) {
				if (!lg.equals(defaultLgContext.getLanguage())) {
					lgCtx.setRequestContentLanguage(lg);
					defaultLgContent.initialize(lgCtx);
					ContentElementList content = child.getContent(lgCtx);
					while (defaultLgContent.hasNext(defaultLgContext)) {
						while (content.hasNext(lgCtx)) {
							if (expand(ctx, defaultLgContent.next(defaultLgContext), content.next(lgCtx))) {
								modif++;
							}
						}
					}
				}
			}
		}
		
		String msg = modif+" component(s) identified as similar.";
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
		
		return null;
	}

}
