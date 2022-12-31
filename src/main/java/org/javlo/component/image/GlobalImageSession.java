package org.javlo.component.image;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.io.SessionFolder;
import org.javlo.ztatic.StaticInfo;

public class GlobalImageSession extends GlobalImage {
	
	private static final long serialVersionUID = 1L;

	public static final String TYPE = "global-image-session";
	
	@Override
	protected boolean isFromShared(ContentContext ctx) {
		SessionFolder sessionFolder = SessionFolder.getInstance(ctx);
		if (sessionFolder.getImage() == null) {
			return super.isFromShared(ctx);
		} else {
			return false;
		}
	}
	
	@Override
	protected String hashForImage(ContentContext ctx) {
		SessionFolder sessionFolder = SessionFolder.getInstance(ctx);
		if (sessionFolder.getImage() == null || !ctx.isAsViewMode()) {
			return super.hashForImage(ctx);
		} else {
			StaticInfo si;
			try {
				si = StaticInfo.getInstance(ctx, sessionFolder.getImage());
				return ""+si.getFocusZoneX(ctx)+"_"+si.getFocusZoneY(ctx) +"_"+ si.getFile().lastModified();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
		}
	}
	
	@Override
	public String getFileDirectory(ContentContext ctx) {
		SessionFolder sessionFolder = SessionFolder.getInstance(ctx);
		if (sessionFolder.getImage() == null || !ctx.isAsViewMode()) {
			return super.getFileDirectory(ctx);
		} else {
			return SessionFolder.getSessionMainFolder(ctx.getGlobalContext());
		}
	}
	
	@Override
	public String getFileName(ContentContext ctx) {
		if (ctx != null && SessionFolder.getInstance(ctx).getImage() != null && ctx.isAsViewMode()) {
			return SessionFolder.getInstance(ctx).getImage().getName();
		} else {
			return super.getFileName(ctx);
		}
	}
	
	@Override
	public String getDecorationImage(ContentContext ctx) {
		SessionFolder sessionFolder = SessionFolder.getInstance(ctx);
		if (sessionFolder.getImage() == null || !ctx.isAsViewMode()) {
			return super.getDecorationImage(ctx);
		} else {
			return sessionFolder.getImage().getName();
		}
	}
	
	@Override
	public String getDirSelected(ContentContext ctx) {
		SessionFolder sessionFolder = SessionFolder.getInstance(ctx);
		if (sessionFolder.getImage() == null || !ctx.isAsViewMode()) {
			return super.getDirSelected(ctx);
		} else {
			return sessionFolder.getSessionId();
		}
	}
	
	@Override
	protected String getRelativeFileDirectory(ContentContext ctx) {
		SessionFolder sessionFolder = SessionFolder.getInstance(ctx);
		if (sessionFolder.getImage() == null || !ctx.isAsViewMode()) {
			return super.getRelativeFileDirectory(ctx);
		} else {
			return SessionFolder.SESSION_FOLDER;
		}
	}
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_ADMIN;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}

}
