package org.javlo.module.persistence;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.content.Edit;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.resource.Resource;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.xml.NodeXML;
import org.javlo.xml.XMLFactory;

public class Persistence extends AbstractModuleAction {
	
	private static Logger logger = Logger.getLogger(Persistence.class.getName());

	@Override
	public String getActionGroupName() {
		return "persistence";
	}
	
	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		ctx.getRequest().setAttribute("persistences", persistenceService.getPersistences());
		
		/** download **/
		ctx.getRequest().setAttribute("downloadAll", URLHelper.createStaticURL(ctx, "/zip/" + globalContext.getContextKey() + ".zip"));
		ctx.getRequest().setAttribute("download", URLHelper.createStaticURL(ctx, "/zip/" + globalContext.getContextKey() + "_xml.zip?filter=xml"));

		
		return msg;
	}
	
	public static String performUpload(RequestService requestService, HttpServletRequest request, HttpServletResponse response, ContentContext ctx, ContentService content, I18nAccess i18nAccess) {

		Collection<FileItem> fileItems = requestService.getAllFileItem();

		for (FileItem item : fileItems) {
			try {
				if (StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("zip")) {
					InputStream in = item.getInputStream();
					try {
						ZipManagement.uploadZipFile(request, response, in);
					} finally {
						ResourceHelper.closeResource(in);
					}

					content.releasePreviewNav(ctx);

					String msg = i18nAccess.getText("edit.message.uploaded");
					MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.INFO));

				}
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}
		return null;
	}
	
	public static String performImportpage(RequestService requestService, ContentContext ctx, HttpServletRequest request, MenuElement currentPage, I18nAccess i18nAccess) throws Exception {
		if (!Edit.checkPageSecurity(ctx)) {
			return null;
		}
		String importURL = requestService.getParameter("import-url", null);

		if (importURL != null) {
			String XMLURL = StringHelper.changeFileExtension(importURL, "xml");
			InputStream in = null;
			
			int countResources = 0;
			
			try {
				URL url = new URL(XMLURL);
				in = url.openStream();
				NodeXML node = XMLFactory.getFirstNode(in);
				NodeXML pageNode = node.getChild("page");
				GlobalContext globalContext = GlobalContext.getInstance(request);
				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				NavigationHelper.importPage(ctx, persistenceService, pageNode, currentPage, ctx.getLanguage(), false);
				NodeXML resourcesNode = node.getChild("resources");
				if (resourcesNode != null) {
					String baseURL = resourcesNode.getAttributeValue("url");
					Collection<Resource> resources = new LinkedList<Resource>();
					NodeXML resourceNode = resourcesNode.getChild("resource");
					if (resourceNode == null) {
						logger.warning("resource node not found in : " + url);
					}
					while (resourceNode != null) {
						if (baseURL != null) {
							Resource resource = new Resource();
							resource.setId(resourceNode.getAttributeValue("id"));
							resource.setUri(resourceNode.getAttributeValue("uri"));
							resources.add(resource);							
							ResourceHelper.downloadResource(globalContext.getDataFolder(), baseURL, resources);
							countResources++;
						}
						resourceNode = resourceNode.getNext("resource");
					}
				} else {
					logger.warning("resources node not found in : " + url);					
				}
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx,new GenericMessage(i18nAccess.getText("persistence.message.imported", new String[][] {{"countResources",""+countResources}}), GenericMessage.INFO));
			} catch (Exception e) {
				e.printStackTrace();
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
			} finally {
				ResourceHelper.closeResource(in);
			}

		} else {
			return "bad parameters : need 'import-url'.";
		}
		return null;
	}

}
