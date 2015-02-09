package org.javlo.module.remote;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;

public class RemoteAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "remote";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);		
		RemoteService remoteService = RemoteService.getInstance(ctx);
		ctx.getRequest().setAttribute("remotes", remoteService.getRemotes());
		return msg;
	}

	public static String performUpdate(RequestService rs, GlobalContext globalContext, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		RemoteService remoteSevice = RemoteService.getInstance(ctx);

		if (rs.getParameter("id", null) != null) {
			// update
		} else {
			RemoteBean newBean = new RemoteBean();
			newBean.setUrl(rs.getParameter("url", ""));
			newBean.setSynchroCode(rs.getParameter("synchrocode", null));
			newBean.setAuthors(ctx.getCurrentEditUser().getLogin());
			newBean.setText(rs.getParameter("text", ""));
			newBean.setPriority(Integer.parseInt(rs.getParameter("priority", "1")));
			newBean.check(remoteSevice.getDefaultSynchroCode());
			remoteSevice.updateRemove(newBean);
		}

		return null;
	}

	public static String performCheck(RequestService rs, GlobalContext globalContext, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String id = rs.getParameter("id", null);
		RemoteService remoteService = RemoteService.getInstance(ctx);
		String defaultSynchroCode = remoteService.getDefaultSynchroCode();
		if (id != null) {
			RemoteBean bean = remoteService.getRemote(id);
			if (bean == null) {
				return "remote not found : " + id;
			} else {
				bean.check(defaultSynchroCode, true);
			}
		} else {
			for (RemoteBean bean : remoteService.getRemotes()) {
				bean.check(defaultSynchroCode, true);
			}
		}
		return null;
	}

	public static String performDelete(RequestService rs, GlobalContext globalContext, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String id = rs.getParameter("id", null);
		RemoteService remoteService = RemoteService.getInstance(ctx);
		if (id == null) {
			return "bad request structure : need 'id'.";
		} else {
			remoteService.deleteBean(id);
		}
		return null;

	}

}
