package org.javlo.module.market;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.media.jai.JAI;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.remote.IRemoteResource;
import org.javlo.remote.LocalResourceFactory;
import org.javlo.remote.RemoteResourceFactory;
import org.javlo.remote.RemoteResourceList;
import org.javlo.service.RequestService;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class MarketAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "market";
	}

	@Override
	public AbstractModuleContext getModuleContext(HttpSession session, Module module) throws Exception {
		return AbstractModuleContext.getInstance(session, GlobalContext.getSessionInstance(session), module, MarketContext.class);
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		super.prepare(ctx, modulesContext);
		String msg = null;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Module module = ModulesContext.getInstance(ctx.getRequest().getSession(), globalContext).getCurrentModule();
		RequestService rs = RequestService.getInstance(ctx.getRequest());

		RemoteResourceFactory remoteFactory = RemoteResourceFactory.getInstance(globalContext);

		if (getModuleContext(ctx.getRequest().getSession(), modulesContext.getCurrentModule()).getCurrentLink() != null) {
			String[] resourcePath = getModuleContext(ctx.getRequest().getSession(), modulesContext.getCurrentModule()).getCurrentLink().split("-");
			if (resourcePath.length == 2) {
				RemoteResourceList resoucesList = remoteFactory.getResources(resourcePath[0], resourcePath[1]);
				ctx.getRequest().setAttribute("resources", resoucesList.getList());
				if (modulesContext.getCurrentModule().getAction() != null) {
					if (resoucesList.getLayout().equals(RemoteResourceList.TABLE_LAYOUT)) {
						getModuleContext(ctx.getRequest().getSession(), module).setRenderer("/jsp/table.jsp");
					} else {
						getModuleContext(ctx.getRequest().getSession(), module).setRenderer("/jsp/list.jsp");
					}
				}
			}
		}
		if (rs.getParameter("id", null) != null) {
			module.setRenderer("/jsp/import.jsp");
			module.setToolsRenderer(null);
		} else {
			String currentRenderer = getModuleContext(ctx.getRequest().getSession(), module).getRenderer();
			ctx.getRequest().setAttribute("viewAllButton", "true");
			module.setToolsRenderer("/jsp/actions.jsp");
			if (currentRenderer == null) {
				module.restoreRenderer();
			} else {
				module.setRenderer(currentRenderer);
			}
		}

		return msg;
	}

	public static String performImportPage(RequestService rs, ContentContext ctx, GlobalContext globalContext, Module currentModule, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		String remoteId = rs.getParameter("id", null);
		if (remoteId == null) {
			return "bad request structure : need 'id' parameter.";
		}

		RemoteResourceFactory remoteResourceFactory = RemoteResourceFactory.getInstance(globalContext);
		IRemoteResource resource = remoteResourceFactory.getResource(ctx, remoteId);

		if (resource == null) {
			return "remote resource not found : " + remoteId;
		}
		ctx.getRequest().setAttribute("remoteResource", resource);

		LocalResourceFactory localResourceFactory = LocalResourceFactory.getInstance(globalContext);
		IRemoteResource localResource = localResourceFactory.getLocalResource(ctx, resource.getName(), resource.getType());
		if (localResource != null) {
			ctx.getRequest().setAttribute("localResource", localResource);
			if (!messageRepository.haveGlobalMessage()) {
				messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("market.message.local-found"), GenericMessage.ALERT));
			}
		} else {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("market.message.local-not-found"), GenericMessage.INFO));
		}

		return null;
	}

	public String performImport(RequestService requestService, ServletContext application, HttpSession session, ContentContext ctx, GlobalContext globalContext, Module currentModule, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String remoteId = requestService.getParameter("id", null);
		if (remoteId == null) {
			return "bad request structure : need 'id'.";
		}
		RemoteResourceFactory remoteResourceFactory = RemoteResourceFactory.getInstance(globalContext);
		IRemoteResource resource = remoteResourceFactory.getResource(ctx, remoteId);
		if (resource == null) {
			return "resource not found : " + remoteId;
		}
		boolean imported = false;
		if (resource.getType().equals(IRemoteResource.TYPE_TEMPLATE)) {
			Template newTemplate = TemplateFactory.createDiskTemplates(session.getServletContext(), resource.getName());

			InputStream in = null;
			OutputStream out = null;
			try {
				URL zipURL = new URL(resource.getDownloadURL());
				in = zipURL.openConnection().getInputStream();
				ZipManagement.uploadZipTemplate(ctx, in, newTemplate.getId(), false);
				in.close();
				if (resource.getImageURL() != null) {
					URL imageURL = new URL(resource.getImageURL());
					File visualFile = new File(URLHelper.mergePath(newTemplate.getTemplateRealPath(), newTemplate.getVisualFile()));
					RenderedImage image = JAI.create("url", imageURL);
					out = new FileOutputStream(visualFile);
					JAI.create("encode", image, out, "png", null);
					out.close();
				}
				newTemplate.getRenderer(ctx); // deploy template
				globalContext.addTemplate(newTemplate.getName(), false);
				imported = true;
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			} finally {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			}
		}

		if (imported) {
			TemplateFactory.clearTemplate(application);
			LocalResourceFactory localResourceFactory = LocalResourceFactory.getInstance(globalContext);
			localResourceFactory.clear();
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("market.message.imported", new String[][] { { "name", resource.getName() }, { "type", resource.getType() } }), GenericMessage.INFO));
		} else {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("market.message.not-imported"), GenericMessage.ERROR));
		}

		return null;
	}

	public static String performDelete(RequestService rs, ServletContext application, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		String id = rs.getParameter("lid", null);
		if (id == null) {
			return "bad request structure, need 'lid' as parameter";
		}
		LocalResourceFactory localResourceFactory = LocalResourceFactory.getInstance(globalContext);
		IRemoteResource resource = localResourceFactory.getLocalResource(ctx, id);
		if (resource == null) {
			return "resource not found : " + id;
		}
		if (resource.getType().equals(IRemoteResource.TYPE_TEMPLATE)) {
			Template template = TemplateFactory.getDiskTemplate(application, resource.getName());
			if (template != null) {
				template.delete();
			}
			TemplateFactory.clearTemplate(application);
			localResourceFactory.clear();
		}

		return null;
	}

}
