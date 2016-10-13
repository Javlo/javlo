package org.javlo.module.persistence;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
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

	public static final class ExportBean {
		private String label;
		private String csvURL;
		private String excelURL;

		public ExportBean(String label, String csvURL, String excelURL) {
			super();
			this.label = label;
			this.csvURL = csvURL;
			this.excelURL = excelURL;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getCsvURL() {
			return csvURL;
		}

		public void setCsvURL(String csvURL) {
			this.csvURL = csvURL;
		}

		public String getExcelURL() {
			return excelURL;
		}

		public void setExcelURL(String excelURL) {
			this.excelURL = excelURL;
		}

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

		ContentContext absCtx = ctx.getContextForAbsoluteURL();
		absCtx.setArea(null);
		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<String> allExportType = new LinkedList<String>();
		for (IContentVisualComponent comp : content.getAllContent(absCtx)) {
			if (comp instanceof DynamicComponent) {
				if (!allExportType.contains(comp.getType())) {
					allExportType.add(comp.getType());
				}
			}
		}
		Collections.sort(allExportType);
		List<ExportBean> beans = new LinkedList<ExportBean>();
		for (String type : allExportType) {
			String csvURL = URLHelper.createStaticURL(absCtx, "/expcomp/" + ctx.getRequestContentLanguage() + '/' + type + ".csv");
			String excelURL = URLHelper.createStaticURL(absCtx, "/expcomp/" + ctx.getRequestContentLanguage() + '/' + type + ".xlsx");
			beans.add(new ExportBean(type, csvURL, excelURL));
		}
		ctx.getRequest().setAttribute("exportLinks", beans);
		if (ctx.getCurrentUser().getUserInfo().getToken() != null) {
			ctx.getRequest().setAttribute("token", globalContext.getOneTimeToken(ctx.getCurrentUser().getUserInfo().getToken()));
		}

		return msg;
	}

	public static String performUpload(RequestService requestService, HttpServletRequest request, HttpServletResponse response, ContentContext ctx, ContentService content, I18nAccess i18nAccess) throws Exception {

		Collection<FileItem> fileItems = requestService.getAllFileItem();
		
		String urlParam = requestService.getParameter("url", "");
		if (urlParam.trim().length() > 0) {
			URL url = new URL(urlParam);
			InputStream in = url.openStream();
			try {
				ZipManagement.uploadZipFile(request, response, in);
			} finally {
				ResourceHelper.closeResource(in);
			}
			content.releasePreviewNav(ctx);
			String msg = i18nAccess.getText("edit.message.uploaded");
			MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.INFO), false);
		}

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
					MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.INFO), false);
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
				NavigationHelper.importPage(ctx, persistenceService, pageNode, currentPage, ctx.getLanguage(), true);
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
							ResourceHelper.downloadResource(ctx, globalContext.getDataFolder(), baseURL, resources);
							countResources++;
						}
						resourceNode = resourceNode.getNext("resource");
					}
				} else {
					logger.warning("resources node not found in : " + url);
				}
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("persistence.message.imported", new String[][] { { "countResources", "" + countResources } }), GenericMessage.INFO), false);
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

	public static String performRestore(RequestService rs, ContentContext ctx, ContentService content, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String newServiceStr = rs.getParameter("version", null);
		if (newServiceStr == null) {
			return "bad request structure : need 'version' parameter.";
		}
		int newVersion = Integer.parseInt(newServiceStr);
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setVersion(newVersion);

		content.releasePreviewNav(ctx);

		MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("persistence.message.new-version", new String[][] { { "version", "" + newVersion } }), GenericMessage.INFO), false);

		return null;
	}

}
