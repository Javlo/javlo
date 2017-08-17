package org.javlo.module.taxonomy;

import java.io.IOException;

import javax.servlet.ServletException;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.data.taxonomy.TaxonomyBean;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.exception.ServiceException;

public class TaxonomyAction extends AbstractModuleAction {
	
	public static final String NAME = "taxonomy";

	@Override
	public String getActionGroupName() {
		return NAME;
	}
	
	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);
		TaxonomyService taxonomyService = TaxonomyService.getInstance(ctx.getGlobalContext());
		ctx.getRequest().setAttribute(TaxonomyService.KEY, taxonomyService);
		return msg;
	}
	
	private static void updateBean(ContentContext ctx, Module module, String id) throws ServletException, IOException {
		String jsp = module.getJspPath("/jsp/list.jsp?focus"+id+"=true&id="+id);
		String html = ServletHelper.executeJSP(ctx, jsp);		
		ctx.getAjaxZone().put("item-"+id, html);
	}
	
	public static String performUpdate(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, Module module) throws ServletException, IOException, ServiceException {
		TaxonomyService taxonomyService = TaxonomyService.getInstance(ctx.getGlobalContext());
		ctx.getRequest().setAttribute(TaxonomyService.KEY, taxonomyService);
		String deletedId = rs.getParameter("delete", "");
		
		String moveto = rs.getParameter("moveto", null);
		String moved = rs.getParameter("moved", null);
		Boolean asChild = StringHelper.isTrue(rs.getParameter("aschild", null));		
		
		if (!StringHelper.isEmpty(moveto) && !StringHelper.isEmpty(moved)) {
			if (taxonomyService.move(moved, moveto, asChild)) {
				updateBean(ctx, module, moveto);
			}
		}
		
		if (!StringHelper.isEmpty(deletedId)) {
			taxonomyService.delete(deletedId);
			ctx.getAjaxZone().put("item-"+deletedId, "");
		}
		for (TaxonomyBean bean : taxonomyService.getAllBeans()) {			
			boolean updateList = false;
			boolean updateItem = false;
			String name = rs.getParameter("name-"+bean.getId(), "");
			if (name.length() > 0) {
				name = StringHelper.createFileName(name);
				if (bean.setName(name)) {
					updateItem=true;
				}				
			}
			for (String lg : ctx.getGlobalContext().getContentLanguages()) {
				if (bean.updateLabel(lg, rs.getParameter("label-"+lg+"-"+bean.getId(), null))) {
					updateList = true;
				}
			}			
			if (!StringHelper.isEmpty(rs.getParameter("newname-"+bean.getId(), null))) {				
				bean.addChildAsFirst(new TaxonomyBean(StringHelper.getRandomId(), rs.getParameter("newname-"+bean.getId(), null)));
				taxonomyService.clearCache();
				updateList=true;
			}	
			if (updateItem) {
				String jsp = module.getJspPath("/jsp/item.jsp?id="+bean.getId());
				String html = ServletHelper.executeJSP(ctx, jsp);		
				ctx.getAjaxZone().put("item-wrapper-"+bean.getId(), html);
			}
			if (updateList) {
				updateBean(ctx, module, bean.getId());
			}
		}		
		PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("taxonomy.updated", "Taxonomy tree is updated."), GenericMessage.INFO));		
		return null;
	}

}
