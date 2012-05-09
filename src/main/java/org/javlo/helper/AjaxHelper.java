package org.javlo.helper;

import java.io.IOException;

import javax.servlet.ServletException;

import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.module.Module.Box;

public class AjaxHelper {
	
	public static final void updateBox(ContentContext ctx, Box box) throws ServletException, IOException {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());		
		ctx.getRequest().setAttribute("box", box);
		String xhtml = ServletHelper.executeJSP(ctx, editCtx.getBoxTemplate());
		ctx.addAjaxZone(box.getId(), xhtml);
	}

}
