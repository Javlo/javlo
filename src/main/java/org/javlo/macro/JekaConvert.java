package org.javlo.macro;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.files.GenericFile;
import org.javlo.component.title.SubTitle;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class JekaConvert extends AbstractMacro {

	@Override
	public String getName() {
		return "jeka";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		ContentService.getInstance(ctx.getRequest());
		int countChange = 0;
		ctx = ctx.getContextWithArea(null);
		for (MenuElement page : ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx).getAllChildrenList()) {
			ContentElementList comps = page.getContent(ctx);
			boolean house = false;
			List<IContentVisualComponent> moveToContent = new LinkedList<IContentVisualComponent>(); 
			while (comps.hasNext(ctx)) {
				IContentVisualComponent comp = comps.next(ctx);
				if (comp.getType().equals("title")) {
					if (!comp.getPage().getName().contains("details")) {
						comp.setStyle(ctx, "standard");
						countChange++;
					} else {
						comp.setStyle(ctx, "hidden");
					}
				}
				if (comp.getType().equals("file")) {
					if (comp.getPreviousComponent() != null && comp.getPreviousComponent().getPreviousComponent() != null) {
						if (comp.getPreviousComponent().getPreviousComponent().getType().equals("title")) {
							comp.setRenderer(ctx, "square");
							countChange++;
						}
					}
					if (comp.getPreviousComponent() != null) {
						if (comp.getPreviousComponent().getType().equals(SubTitle.TYPE)) {
							comp.setRenderer(ctx, "default");
							comp.setList(true);
						}
						if (comp.getPreviousComponent().getType().equals(GenericFile.TYPE)) {
							comp.setRenderer(ctx, "default");
							comp.setList(true);
						}
					}
				}
				if (comp.getType().equals("multimedia")) {
					comp.setRenderer(ctx, "small-slide");
				}
				if (comp.getType().equals("cottage")) {
					if (comp.getArea().equals("content")) {
						house = true;
						countChange++;
					}
					if (house) {
						ComponentHelper.moveComponent(ctx, comp, null, page, "intro");
					}
				}
				if (house) {
					if (comp.getType().equals("multimedia")) {
						comp.setRenderer(ctx, "small-slide");
					}
				}
				if (comp.getArea().equals("intro") && !comp.getType().equals("destination") && !comp.getType().equals("cottage")) {
					moveToContent.add(0,comp);
				}
			}
			for (IContentVisualComponent comp : moveToContent) {
				ComponentHelper.moveComponent(ctx, comp, null, page, "content");
			}
			
		}
		String msg = "Component change : " + countChange;
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
};
