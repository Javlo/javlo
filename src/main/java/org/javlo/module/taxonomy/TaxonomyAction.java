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
		TaxonomyService.getInstance(ctx);		
		return msg;
	}
	
	private static void updateBean(ContentContext ctx, Module module, String id) throws ServletException, IOException {
		String jsp = module.getJspPath("/jsp/list.jsp?focus"+id+"=true&id="+id);
		String html = ServletHelper.executeJSP(ctx, jsp);		
		ctx.getAjaxZone().put("item-"+id, html);
	}
	
	public static String performUpdate(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, Module module) throws ServletException, IOException, ServiceException {
		TaxonomyService taxonomyService = TaxonomyService.getInstance(ctx);		
		String deletedId = rs.getParameter("delete", "");
		
		String moveto = rs.getParameter("moveto", null);
		String moved = rs.getParameter("moved", null);
		Boolean asChild = StringHelper.isTrue(rs.getParameter("aschild", null));		
		
		if (!StringHelper.isEmpty(moveto) && !StringHelper.isEmpty(moved)) {
			if (taxonomyService.move(moved, moveto, asChild)) {
				String parentId = moveto;
				for (TaxonomyBean bean : taxonomyService.getAllBeans()) {
					if (bean.getId().equals(moveto) && bean.getParent() != null) {
						parentId = bean.getParent().getId();
					}
				}
				updateBean(ctx, module, parentId);
			} else {
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("taxonomy.move.error", "Error on move, check if there are not allready the name in the target list."), GenericMessage.ERROR));
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
				String newName = StringHelper.createFileName(name);
				newName = newName.replace("-", "_");
				if (!name.equals(newName)) {
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("taxonomy.update.bad-name", "Name of node is updated but name was changed, because your name was unvalid."), GenericMessage.ALERT));
				}
				if (bean.getParent() != null && bean.getParent().searchChildByName(newName) != null && !bean.getName().equals(newName)) {
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("taxonomy.name-exist", "Name already exist."), GenericMessage.ERROR));
				} else if (bean.setName(newName)) {
					updateItem=true;
				}				
			}
			for (String lg : ctx.getGlobalContext().getContentLanguages()) {
				if (bean.updateLabel(lg, rs.getParameter("label-"+lg+"-"+bean.getId(), null))) {
					updateList = true;
				}
			}			
			if (!StringHelper.isEmpty(rs.getParameter("newname-"+bean.getId(), null))) {
				String nName = rs.getParameter("newname-"+bean.getId(), null);
				String newName = StringHelper.createFileName(nName);
				newName = newName.replace("-", "_");
				if (!nName.equals(newName)) {
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("taxonomy.create.bad-name", "Node is create but name was changed, because your name was unvalid."), GenericMessage.ALERT));
				}
				if (bean.searchChildByName(newName) != null) {
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("taxonomy.name-exist", "Name already exist."), GenericMessage.ERROR));
				} else {
					bean.addChildAsFirst(new TaxonomyBean(StringHelper.getRandomId(),newName));
					taxonomyService.clearCache();
					updateList=true;
				}
			}
			String newId = rs.getParameter("change-id-"+bean.getId(), "");
			if (!newId.equals(bean.getId())) {
				if (ctx.getGlobalContext().getAllTaxonomy(ctx).getBean(newId) != null) {
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("taxonomy.changeid.error", "ID all ready exist."), GenericMessage.ERROR));
				} else {					
					taxonomyService.updateId(bean, newId);
				}
			}
			String newDeco = rs.getParameter("change-deco-"+bean.getId(), "");
			if (!newDeco.equals(bean.getDecoration())) {
				bean.setDecoration(newDeco);
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
