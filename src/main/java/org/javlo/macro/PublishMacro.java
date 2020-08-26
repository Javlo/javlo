package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.content.Edit;
import org.javlo.service.ContentService;

public class PublishMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "publish";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		return Edit.performPublish(ctx.getRequest().getSession().getServletContext(), ctx.getRequest(), ctx.getGlobalContext().getStaticConfig(), ctx.getGlobalContext(), content, ctx, i18nAccess);
	}

	@Override
	public boolean isPreview() {
		return true;
	}
	
	@Override
	public boolean isAdd() {
		return true;
	}
	
	@Override
	public String getIcon() {
		return "fa fa-arrow-up";
	}	
	

}
