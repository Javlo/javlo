package org.javlo.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.owasp.encoder.Encode;

public class SessionFolder {

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
	
	public SessionFolder(ContentContext ctx) {
		this.sessionId = ctx.getSession().getId();
		sessionFolder = new File(getSessionMainFolder(ctx.getGlobalContext()), this.sessionId);
		sessionFolder.mkdirs();
	}

	public static final SessionFolder getInstance(ContentContext ctx) {
		SessionFolder out = (SessionFolder) ctx.getSession().getAttribute(KEY);
		if (out == null) {
			out = new SessionFolder(ctx);
			ctx.getSession().setAttribute(KEY, out);
		}
		return out;
	}

	public File getSessionFolder() {
		return sessionFolder;
	}

	public void addImage(String name, InputStream in) throws IOException {
		this.image = new File(sessionFolder.getAbsoluteFile(), name);
		this.image = ResourceHelper.getFreeFileName(this.image);
		ResourceHelper.writeStreamToFile(in, this.image);
	}

	public File getImage() {
		return image;
	}
	
	public String getSessionId() {
		return sessionId;
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

}
