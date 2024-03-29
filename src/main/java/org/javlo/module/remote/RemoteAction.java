package org.javlo.module.remote;

import jakarta.servlet.http.HttpSession;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.utils.TimeMap;
import org.javlo.xml.NodeXML;
import org.javlo.xml.XMLFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class RemoteAction extends AbstractModuleAction {
	
	private static Logger logger = Logger.getLogger(RemoteAction.class.getName());

	private static final TimeMap<String, SiteMapURL> testedURL = new TimeMap<String, SiteMapURL>(60 * 60, 100000);

	private static final String UNKNOWN_SERVER = "[unknown]";
	private static final String RENDER_MODE = "renderMode";
	private static final String RENDER_MODE_LIST = "list";
	private static final String RENDER_MODE_TREE = "tree";
	private static final String SITEMAP_TREE = "sitemap";
	private static final String STATUS = "status";

	@Override
	public String getActionGroupName() {
		return "remote";
	}
	
	private static void loadSiteMap(NodeXML node, List<SiteMapURL> urls) throws Exception {
		if (node == null) {
			return;
		}
		for (NodeXML n : node.getAllChildren()) {
			SiteMapURL url = new SiteMapURL(n);
			if (!StringHelper.isEmpty(url.getLink())) {
				urls.add(url);
			}
		}
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);
		RemoteService remoteService = RemoteService.getInstance(ctx);
		List<RemoteBean> remotes = new LinkedList(remoteService.getRemotes());
		ctx.getRequest().setAttribute("remotes", remotes);
		String currentRenderMode = getCurrentRenderMode(modulesContext.getCurrentModule());
		ctx.getRequest().setAttribute("currentRemoteRenderMode", currentRenderMode);
		List<RemoteServer> remoteServers = new LinkedList<RemoteServer>();
		Map<String, RemoteServer> serversByAddress = new HashMap<String, RemoteServer>();
		Map<String, RemoteInstance> instancesByAddressPort = new HashMap<String, RemoteInstance>();
		if ("charge".equals(currentRenderMode)) {
			Collections.sort(remotes, new RemoteBeanComparator());
		}

		String siteMapURL = ctx.getRequest().getParameter("sitemap");		
		if (StringHelper.isURL(siteMapURL)) {
			List<SiteMapURL> urls = new LinkedList<SiteMapURL>();			
			NodeXML node = XMLFactory.getFirstNode(NetHelper.followURL(new URL(siteMapURL)));	
			if (node != null && node.getName().equalsIgnoreCase("sitemapindex")) {
				for (NodeXML siteMapNode : node.getAllChildren()) {
					if (siteMapNode.getName().equalsIgnoreCase("sitemap")) {
						if (siteMapNode.getChild("loc") != null) {
							String siteMapUrl = siteMapNode.getChild("loc").getContent();
							if (StringHelper.isURL(siteMapUrl)) {								
								node = XMLFactory.getFirstNode(NetHelper.followURL(new URL(siteMapUrl)));
								loadSiteMap(node, urls);
							}
						}
					}
				}
			} else  {
				loadSiteMap(node, urls);
			}			
			ctx.getRequest().setAttribute("urls", urls);
		}

		if (RENDER_MODE_TREE.equals(currentRenderMode) || "charge".equals(currentRenderMode)) {

			for (RemoteBean remote : remotes) {
				String address = UNKNOWN_SERVER;
				String port = UNKNOWN_SERVER;
				String hostname = UNKNOWN_SERVER;
				String systemUser = UNKNOWN_SERVER;
				String version = UNKNOWN_SERVER;
				String os = UNKNOWN_SERVER;
				if (remote.isServerInfoLoaded()) {
					address = remote.getServerAddress();
					port = remote.getServerPort();
					systemUser = remote.getSystemUser();
					version = remote.getVersion();
					hostname = remote.getServerHostname();
					os = remote.getOs();
				}
				RemoteServer server = serversByAddress.get(address);
				if (server == null) {
					server = new RemoteServer();
					server.setAddress(address);
					server.setHostname(hostname);
					server.setOs(os);
					remoteServers.add(server);
					serversByAddress.put(address, server);
				}
				/** if new json version with os, set it **/
				if (!StringHelper.isEmpty(os)) {
					server.setHostname(hostname);
					server.setOs(os);
				}
				String addressPort = address + ":" + port;
				RemoteInstance instance = instancesByAddressPort.get(addressPort);
				if (instance == null) {
					instance = new RemoteInstance();
					instance.setPort(port);
					instance.setSystemUser(systemUser);
					instance.setVersion(version);
					instance.setCharge(remote.getServerCharge());
					server.getInstances().add(instance);
					instancesByAddressPort.put(addressPort, instance);
				}
				instance.getSites().add(remote);
			}
		}
		
		if (STATUS.equals(currentRenderMode)) {
			AtomicInteger error = new AtomicInteger(0);
			ctx.getRequest().setAttribute("status", RemoteThread.renderRemoteStatus(remoteService, error));
			ctx.getRequest().setAttribute("error", error.get());
		}

		ctx.getRequest().setAttribute("remoteServers", remoteServers);

		return msg;
	}

	public static String performUpdate(RequestService rs, GlobalContext globalContext, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		RemoteService remoteSevice = RemoteService.getInstance(ctx);
		
		if (StringHelper.isEmpty(remoteSevice.getDefaultSynchroCode())) {
			logger.severe("no synchro core found.");
			return "no synchro code";
		}

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
		if (StringHelper.isEmpty(remoteService.getDefaultSynchroCode())) {
			logger.severe("no synchro core found.");
			return "no synchro code";
		}
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
		} else if ("charge".equals(newRenderMode)) {
			newRenderMode = "charge";
			renderer = "jsp/charge.jsp";
		} else if (SITEMAP_TREE.equals(newRenderMode)) {
			renderer = "jsp/sitemap.jsp";
		} else if (STATUS.equals(newRenderMode)) {
			renderer = "jsp/status.jsp";
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

	public static String performTesturl(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String url = rs.getParameter("url");
		SiteMapURL bean;
		synchronized (testedURL) {
			bean = testedURL.get(url);
			if (bean == null) {
				bean = new SiteMapURL();
				testedURL.put(url, bean);
			}
		}		
		synchronized (bean) {
			if (bean.getResponseTime() < 0) {
				long beforeTime = System.currentTimeMillis();
				HttpURLConnection connection = null;
				try {
					URL urlAccess = new URL(url);
					urlAccess = NetHelper.followURL(urlAccess);
					connection = (HttpURLConnection) urlAccess.openConnection();
					connection.setRequestMethod("GET");
					connection.connect();					
					bean.setResponseCode(connection.getResponseCode());
					bean.setResponseTime(System.currentTimeMillis()-beforeTime);
				} finally {
					ResourceHelper.closeResource(connection);
				}
			}
		}
		
		ctx.getAjaxData().put("responsecode", bean.getResponseCode());
		ctx.getAjaxData().put("responsetime", bean.getResponseTime());
	
		return null;
	}

}
