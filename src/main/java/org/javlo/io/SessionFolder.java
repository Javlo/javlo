package org.javlo.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.owasp.encoder.Encode;

public class SessionFolder {
	
	public static final String SESSION_PATH_KEY = "_sid";
	
	public static final String SESSION_PATH_PREFIX = SESSION_PATH_KEY+'-';

	private static final String KEY = "sessionFolder";

	public static final String SESSION_FOLDER = "sessionFolder";

	private File sessionFolder = null;
	private String sessionId = null;
	private File image = null;
	
	public static void clearAllSessionFolder(GlobalContext globalContext) {
		File sessionFolder = new File(getSessionMainFolder(globalContext));
		ResourceHelper.deleteFolder(sessionFolder);
	}

	public static String getSessionMainFolder(GlobalContext globalContext) {
		return URLHelper.mergePath(globalContext.getDataFolder(), SESSION_FOLDER);
	}
	
	private SessionFolder(HttpSession session, GlobalContext globalContext) {
		this.sessionId = session.getId();
		sessionFolder = new File(getSessionMainFolder(globalContext), getSessionId());
	}

	public static final SessionFolder getInstance(HttpSession session, GlobalContext globalContext) {
		SessionFolder out = (SessionFolder) session.getAttribute(KEY);
		if (out == null) {
			out = new SessionFolder(session, globalContext);
			session.setAttribute(KEY, out);
		}
		return out;
	}
	
	public static final SessionFolder getInstance(ContentContext ctx) {
		return getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext());
	}

	public File getSessionFolder() {
		return sessionFolder;
	}

	public void addImage(String name, InputStream in) throws IOException {
		sessionFolder.mkdirs();
		this.image = new File(sessionFolder.getAbsoluteFile(), name);
		this.image = ResourceHelper.getFreeFileName(this.image);
		ResourceHelper.writeStreamToFile(in, this.image);
	}

	public File getImage() {
		return image;
	}
	
	public String getSessionId() {
		return SESSION_PATH_PREFIX+sessionId;
	}
	
	public String getImageFileId() {
		if (getImage() == null) {
			return null;
		}
		return Encode.forHtmlAttribute(getImage().getName().replace('.', '_'));
	}

	public void resetImage() {
		image = null;
	}
	
	public String correctAndcheckUrl(String pathInfo) {
		if (!pathInfo.contains(SESSION_PATH_KEY)) {
			return pathInfo;
		} else {
			if (pathInfo.contains(SESSION_PATH_PREFIX)) {
				String sessionid = StringHelper.extractItem(pathInfo, SESSION_PATH_PREFIX, "/").get(0);
				if (!sessionid.equals(this.sessionId)) {
					throw new SecurityException("bad session");
				}
				return pathInfo;
			} else {
				return pathInfo.replace(SESSION_PATH_KEY, SESSION_PATH_PREFIX+sessionId);
			}
		}
	}

}
