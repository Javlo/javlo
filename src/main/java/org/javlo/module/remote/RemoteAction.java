package org.javlo.module.remote;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;

public class RemoteAction extends AbstractModuleAction {

	private static final String UNKNOWN_SERVER = "[unknown]";
	private static final String RENDER_MODE = "renderMode";
	private static final String RENDER_MODE_LIST = "list";
	private static final String RENDER_MODE_TREE = "tree";

	@Override
	public String getActionGroupName() {
		return "remote";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);		
		RemoteService remoteService = RemoteService.getInstance(ctx);
		Collection<RemoteBean> remotes = remoteService.getRemotes();
		ctx.getRequest().setAttribute("remotes", remotes);

		String currentRenderMode = getCurrentRenderMode(modulesContext.getCurrentModule());
		ctx.getRequest().setAttribute("currentRemoteRenderMode", currentRenderMode);

		List<RemoteServer> remoteServers = new LinkedList<RemoteServer>();
		Map<String, RemoteServer> serversByAddress = new HashMap<String, RemoteServer>();
		Map<String, RemoteInstance> instancesByAddressPort = new HashMap<String, RemoteInstance>();

		if (RENDER_MODE_TREE.equals(currentRenderMode)) {

			for (RemoteBean remote : remotes) {
				String address = UNKNOWN_SERVER;
				String port = UNKNOWN_SERVER;
				String hostname = UNKNOWN_SERVER;
				String systemUser = UNKNOWN_SERVER;
				String version = UNKNOWN_SERVER;
				if (remote.isServerInfoLoaded()) {
					address = remote.getServerAddress();
					port = remote.getServerPort();
					systemUser = remote.getSystemUser();
					version = remote.getVersion();
					hostname = remote.getServerHostname();
				}
				RemoteServer server = serversByAddress.get(address);
				if (server == null) {
					server = new RemoteServer();
					server.setAddress(address);
					server.setHostname(hostname);
					remoteServers.add(server);
					serversByAddress.put(address, server);
				}
				String addressPort = address + ":" + port;
				RemoteInstance instance = instancesByAddressPort.get(addressPort);
				if (instance == null) {
					instance = new RemoteInstance();
					instance.setPort(port);
					instance.setSystemUser(systemUser);
					instance.setVersion(version);
					server.getInstances().add(instance);
					instancesByAddressPort.put(addressPort, instance);
				}
				instance.getSites().add(remote);
			}
		}
		
		ctx.getRequest().setAttribute("remoteServers", remoteServers);

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

	public String performChangeRenderMode(HttpSession session, RequestService requestService, GlobalContext globalContext, Module currentModule, I18nAccess i18nAccess) throws Exception {

		String newRenderMode = requestService.getParameter("rendermode", null);
		if (newRenderMode == null) {
			return "bad request structure : need 'rendermode' as parameter.";
		}
		String renderer;
		if (RENDER_MODE_TREE.equals(newRenderMode)) {
			renderer = "jsp/tree.jsp";
		} else {
			newRenderMode = RENDER_MODE_LIST;
			renderer = "jsp/list.jsp";
		}
		setCurrentRenderMode(currentModule, newRenderMode);
		currentModule.setRenderer(renderer);
		return null;
	}

	private String getCurrentRenderMode(Module module) {
		String out = (String) module.getAttribute(RENDER_MODE);
		if (out == null) {
			out = RENDER_MODE_LIST;
		}
		return out;
	}

	private void setCurrentRenderMode(Module module, String renderMode) {
		module.setAttribute(RENDER_MODE, renderMode);
	}

}
