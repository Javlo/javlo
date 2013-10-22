package org.javlo.module.social;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.service.social.ISocialNetwork;
import org.javlo.service.social.SocialService;

public class SocialAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "social";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		SocialService socialService = SocialService.getInstance(globalContext);

		ctx.getRequest().setAttribute("networks", socialService.getAllNetworks());

		return msg;
	}

	public static String performUpdateNetwork(RequestService rs, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) {

		String name = rs.getParameter("name", null);

		if (name == null) {
			return "bad request structure, need the 'name' of social network as parameter.";
		}

		SocialService socialService = SocialService.getInstance(globalContext);
		ISocialNetwork network = socialService.getNetwork(name);
		if (network == null) {
			return "network '" + name + "' not found.";
		} else {
			network.update(rs.getParameterMap());
		}
		socialService.store();

		messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("social.message.network-updated"), GenericMessage.INFO));
		return null;
	}
	
}
