package org.javlo.module.market;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.media.jai.JAI;
import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.module.template.TemplateContext;
import org.javlo.module.template.remote.IRemoteTemplateFactory;
import org.javlo.module.template.remote.RemoteTemplateFactoryManager;
import org.javlo.remote.IRemoteResource;
import org.javlo.remote.RemoteFactory;
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
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {
		String msg = null;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Module module = ModulesContext.getInstance(ctx.getRequest().getSession(), globalContext).getCurrentModule();
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		
		RemoteFactory remoteFactory = RemoteFactory.getInstance(globalContext);
		RemoteResourceList resourceList =  remoteFactory.loadResource(ctx.getRequest().getSession().getServletContext());
		
		ctx.getRequest().setAttribute("resources", resourceList.getList());

		return msg;
	}

	public String performImport(RequestService requestService, HttpSession session, ContentContext ctx, GlobalContext globalContext, Module currentModule, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String list = requestService.getParameter("list", null);
		String templateName = requestService.getParameter("name", null);
		if (list == null || templateName == null) {
			return "bad request structure : need 'list' and 'name' as parameter.";
		}
		TemplateContext templateContext = TemplateContext.getInstance(session, globalContext, currentModule);
		templateContext.setCurrentLink(list);
		if (list != null) {
			IRemoteTemplateFactory tempFact = RemoteTemplateFactoryManager.getInstance(session.getServletContext()).getRemoteTemplateFactory(globalContext, list);
			IRemoteResource template = tempFact.getTemplate(templateName);
			if (template == null) {
				return "template not found : " + templateName;
			}
			Template newTemplate = TemplateFactory.createDiskTemplates(session.getServletContext(), templateName);

			newTemplate.setAuthors(template.getAuthors());

			InputStream in = null;
			OutputStream out = null;
			try {
				URL zipURL = new URL(template.getDownloadURL());
				in = zipURL.openConnection().getInputStream();
				ZipManagement.uploadZipTemplate(ctx, in, newTemplate.getId(), false);
				in.close();

				URL imageURL = new URL(template.getImageURL());
				File visualFile = new File(URLHelper.mergePath(newTemplate.getTemplateRealPath(), newTemplate.getVisualFile()));
				RenderedImage image = JAI.create("url", imageURL);
				out = new FileOutputStream(visualFile);
				JAI.create("encode", image, out, "png", null);
				out.close();

				messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("template.message.imported", new String[][] { { "name", newTemplate.getId() } }), GenericMessage.INFO));

				templateContext.setCurrentLink(null); // return to local template list.
				currentModule.restoreAll();
				currentModule.clearAllBoxes();

			} catch (Exception e) {
				e.printStackTrace();
				newTemplate.delete();
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

		return null;
	}
	
}
