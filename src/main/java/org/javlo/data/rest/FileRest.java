package org.javlo.data.rest;

import java.io.File;

import jakarta.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.servlet.ImageTransformServlet;
import org.javlo.servlet.ResponseErrorException;
import org.javlo.ztatic.StaticInfo;

public class FileRest implements IRestFactory {
	
	public FileRest() {
	}

	@Override
	public String getName() {
		return "file";
	}

	@Override
	public IRestItem search(ContentContext ctx, String path, String query, int max) throws Exception {
		File file = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), path));
		if (!file.exists()) {
			return null;
		} else {
			StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
			if (staticInfo.canRead(ctx, ctx.getCurrentUser(), ctx.getRequest().getParameter(ImageTransformServlet.RESOURCE_TOKEN_KEY))) {
				return staticInfo;
			} else {
				throw new ResponseErrorException(HttpServletResponse.SC_FORBIDDEN);
			}
		}
	}
}