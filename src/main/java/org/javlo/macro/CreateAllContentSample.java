package org.javlo.macro;

import java.util.Map;
import java.util.logging.Logger;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.title.Heading;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.module.content.Edit;
import org.javlo.service.ContentService;

public class CreateAllContentSample extends AbstractMacro {

	private static Logger logger = Logger.getLogger(CreateAllContentSample.class.getName());

	@Override
	public String getName() {
		return "create-all-content";
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		String previousID = "0";
		ContentContext contentCtx = ctx.getContextWithArea(ComponentBean.DEFAULT_AREA);
		ContentService content = ContentService.getInstance(ctx.getRequest());
		for (Edit.ComponentWrapper comp : ComponentFactory.getComponentForDisplay(ctx)) {
			if (comp.getComponent() instanceof AbstractVisualComponent) {

				ComponentBean heading = new ComponentBean(Heading.TYPE, "depth=2\ntext=" + comp.getType(), previousID);
				heading.setId(StringHelper.getRandomId());
				heading.setLanguage(ctx.getRequestContentLanguage());
				heading.setAuthors(ctx.getCurrentEditUser().getLogin());
				contentCtx.getCurrentPage().addContent(previousID, heading);
				previousID = heading.getId();

				AbstractVisualComponent component = (AbstractVisualComponent) comp.getComponent();
				ComponentBean newComp = new ComponentBean(component.getType(), "", previousID);
				if (component.getRenderes(contentCtx).size() > 0) {
					for (String renderer : component.getRenderes(contentCtx).keySet()) {
						heading = new ComponentBean(Heading.TYPE, "depth=3\ntext=renderer : " + renderer, previousID);
						heading.setId(StringHelper.getRandomId());
						heading.setLanguage(ctx.getRequestContentLanguage());
						heading.setAuthors(ctx.getCurrentEditUser().getLogin());
						contentCtx.getCurrentPage().addContent(previousID, heading);
						previousID = heading.getId();

						newComp.setId(StringHelper.getRandomId());
						newComp.setLanguage(ctx.getRequestContentLanguage());
						newComp.setAuthors(ctx.getCurrentEditUser().getLogin());
						contentCtx.getCurrentPage().addContent(previousID, newComp);
						previousID = newComp.getId();
						IContentVisualComponent newCompInstance = content.getComponent(contentCtx, previousID);
						newCompInstance.initContent(contentCtx);
						newCompInstance.setRenderer(contentCtx, renderer);
					}
				} else {
					newComp.setId(StringHelper.getRandomId());
					newComp.setLanguage(ctx.getRequestContentLanguage());
					newComp.setAuthors(ctx.getCurrentEditUser().getLogin());
					contentCtx.getCurrentPage().addContent(previousID, newComp);
					previousID = newComp.getId();
					IContentVisualComponent newCompInstance = content.getComponent(contentCtx, previousID);
					newCompInstance.initContent(contentCtx);
				}

			}

		}
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
