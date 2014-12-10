package org.javlo.module.dropbox;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.javlo.actions.IModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.user.User;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxSessionStore;
import com.dropbox.core.DbxStandardSessionStore;
import com.dropbox.core.DbxWebAuth;

public class DropboxAction implements IModuleAction {

	private static DropboxThread dropboxThread = null;

	public static class DropboxConfig {

		public DropboxConfig(String data) {
			fromString(data);
		}

		public DropboxConfig() {
		}

		private String token;
		private String localFolder = "";
		private String dropboxFolder = "";

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getLocalFolder() {
			return localFolder;
		}

		public void setLocalFolder(String localFolder) {
			this.localFolder = localFolder;
		}

		public String getDropboxFolder() {
			return dropboxFolder;
		}

		public void setDropboxFolder(String dropboxFolder) {
			this.dropboxFolder = dropboxFolder;
		}

		@Override
		public String toString() {
			Collection<String> data = new LinkedList<String>();
			data.add(token);
			data.add(localFolder);
			data.add(dropboxFolder);
			return StringHelper.collectionToString(data, ",");
		}

		public void fromString(String data) {
			List<String> dataList = StringHelper.stringToCollection(data, ",");
			token = dataList.get(0);
			localFolder = dataList.get(1);
			dropboxFolder = dataList.get(2);
		}
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {

		try {
			DbxAppInfo info = getAppInfo(ctx);
			if (info == null || info.host == null) {
				return "Error, dropbox not configured.";
			}
		} catch (Throwable t) {
			return "Error, dropbox not configured.";
		}

		DropboxConfig config = getConfig(ctx);
		if (dropboxThread != null && dropboxThread.isAlive()) {
			ctx.getRequest().setAttribute("dropboxThread", dropboxThread);
		} else {
			dropboxThread = null;
		}
		if (config == null) {
			if (getWebAuth(ctx.getRequest().getSession()) == null) {
				initWebAuth(ctx);
			}
			Map<String, String> params = new HashMap<String, String>();
			params.put("webaction", "dropbox.askToken");
			String dropboxUrl = URLHelper.createURL(ctx, params);
			ctx.getRequest().setAttribute("dropboxUrl", dropboxUrl);
		} else {
			ctx.getRequest().setAttribute("config", config);
			DbxClient client = new DbxClient(new DbxRequestConfig("Javlo/2.0", Locale.getDefault().toString()), config.getToken());
			ctx.getRequest().setAttribute("linkedAccount", client.getAccountInfo().displayName);
		}

		return null;
	}

	public static DropboxConfig getConfig(ContentContext ctx) {
		String configKey = "dropbox-" + ctx.getCurrentUserId() + "-config";
		if (ctx.getGlobalContext().getData(configKey) != null) {
			return new DropboxConfig(ctx.getGlobalContext().getData(configKey));
		} else {
			return null;
		}
	}

	public static void setConfig(ContentContext ctx, DropboxConfig config) {
		String configKey = "dropbox-" + ctx.getCurrentUserId() + "-config";
		if (config != null) {
			ctx.getGlobalContext().setData(configKey, config.toString());
		} else {
			ctx.getGlobalContext().removeData(configKey);
		}
	}

	private DbxAppInfo getAppInfo(ContentContext ctx) {
		return new DbxAppInfo(ctx.getGlobalContext().getStaticConfig().getDropboxAppKey(), ctx.getGlobalContext().getStaticConfig().getDropboxAppSecret());
	}

	private static void setWebAuth(HttpSession session, DbxWebAuth inAuth) {
		session.setAttribute(DbxWebAuth.class.getName(), inAuth);
	}

	private static DbxWebAuth getWebAuth(HttpSession session) {
		return (DbxWebAuth) session.getAttribute(DbxWebAuth.class.getName());
	}

	private void initWebAuth(ContentContext ctx) {
		DbxRequestConfig requestConfig = new DbxRequestConfig("text-edit/0.1", ctx.getGlobalContext().getEditLanguage(ctx.getRequest().getSession()));

		javax.servlet.http.HttpServletRequest request = ctx.getRequest();
		javax.servlet.http.HttpSession session = request.getSession(true);
		String sessionKey = "dropbox-auth-csrf-token";
		DbxSessionStore csrfTokenStore = new DbxStandardSessionStore(session, sessionKey);

		Map<String, String> params = new HashMap<String, String>();
		params.put("webaction", "dropbox.backToken");
		params.put("module", "dropboxsynchro");
		String redirectUri = URLHelper.createURL(ctx.getContextForAbsoluteURL(), "/", params);
		DbxWebAuth auth = new DbxWebAuth(requestConfig, getAppInfo(ctx), redirectUri, csrfTokenStore);
		setWebAuth(ctx.getRequest().getSession(), auth);

	}

	public DropboxAction() {
	}

	@Override
	public String getActionGroupName() {
		return "dropbox";
	}

	@Override
	public String performSearch(ContentContext ctx, ModulesContext modulesContext, String query) throws Exception {
		return null;
	}

	@Override
	public Boolean haveRight(HttpSession session, User user) throws ModuleException {
		return true;
	}

	/**
	 * action
	 **/

	public static String performConfig(ContentContext ctx, RequestService rs) {
		if (rs.getParameter("reset", null) != null) {
			setConfig(ctx, null);
		} else {
			String localFolder = rs.getParameter("localFolder", null);
			String dropboxFolder = rs.getParameter("dropboxFolder", null);
			if (localFolder == null || dropboxFolder == null) {
				return "bad request structure.";
			} else {
				DropboxConfig config = getConfig(ctx);
				if (config == null) {
					return "error, config not found.";
				} else {
					config.setLocalFolder(localFolder);
					config.setDropboxFolder(dropboxFolder);
					setConfig(ctx, config);
				}
			}
		}
		return null;
	}

	public static String performAskToken(HttpSession session, HttpServletResponse response) throws IOException {
		if (getWebAuth(session) == null) {
			return "Authentification module was not initialized.";
		}
		// Start authorization.
		String authorizePageUrl = getWebAuth(session).start();

		// Redirect the user to the Dropbox website so they can approve our
		// application.
		// The Dropbox website will send them back to
		// "http://my-server.com/dropbox-auth-finish"
		// when they're done.
		response.sendRedirect(authorizePageUrl);
		return null;
	}

	public static String performBackToken(ContentContext ctx, HttpSession session, RequestService rs, HttpServletRequest request, HttpServletResponse response) throws IOException {
		DbxAuthFinish authFinish;
		try {
			if (getWebAuth(session) == null) {
				return "Authentification module was not initialized.";
			}
			Map<String, String[]> parameterMap = rs.getParametersMap();
			authFinish = getWebAuth(session).finish(parameterMap);
			String accessToken = authFinish.accessToken;
			DropboxConfig dropboxConfig = new DropboxConfig();
			dropboxConfig.setToken(accessToken);
			setConfig(ctx, dropboxConfig);
		} catch (DbxWebAuth.BadRequestException ex) {
			ex.printStackTrace();
			response.sendError(400);
			return "On /dropbox-auth-finish: Bad request: " + ex.getMessage();
		} catch (DbxWebAuth.BadStateException ex) {
			ex.printStackTrace();
			return ex.getMessage();
		} catch (DbxWebAuth.CsrfException ex) {
			ex.printStackTrace();
			return "On /dropbox-auth-finish: CSRF mismatch: " + ex.getMessage();
		} catch (DbxWebAuth.NotApprovedException ex) {
			ex.printStackTrace();
			return "Access not accepted.";
		} catch (DbxWebAuth.ProviderException ex) {
			ex.printStackTrace();
			response.sendError(503, "Error communicating with Dropbox.");
			return "On /dropbox-auth-finish: Auth failed: " + ex.getMessage();
		} catch (DbxException ex) {
			ex.printStackTrace();
			response.sendError(503, "Error communicating with Dropbox.");
			return "On /dropbox-auth-finish: Error getting token: " + ex.getMessage();
		}
		return null;
	}

	public static String performToLocal(RequestService rs, ContentContext ctx) {
		if (dropboxThread == null || !dropboxThread.isAlive()) {
			dropboxThread = new DropboxThread(ctx, getConfig(ctx));
			dropboxThread.start();
		} else {
			return "thread already running.";
		}
		return null;
	}

}
