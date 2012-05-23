<%@page contentType="text/html"
        import="
        java.util.Map,
        org.javlo.helper.StringHelper,        
        org.javlo.context.ContentContext,
        org.javlo.context.EditContext,
        org.javlo.context.ContentManager,
		org.javlo.template.Template,
        org.javlo.navigation.MenuElement,
		org.javlo.component.core.ContentElementList,
		org.javlo.component.core.IContentComponentsList,
		org.javlo.component.core.IContentVisualComponent,
		org.javlo.component.column.ColumnContext,
		org.javlo.context.GlobalContext,
		org.javlo.user.User,
		org.javlo.user.UserFactory,
		org.javlo.user.IUserFactory,
		org.javlo.user.AdminUserSecurity,
		org.javlo.component.core.ComponentBean,
		org.javlo.navigation.PageConfiguration,		
		org.javlo.helper.URLHelper"
%><%
ContentContext ctx = ContentContext.getContentContext ( request, response );
GlobalContext globalContext = GlobalContext.getInstance(request);
PageConfiguration pageConfig = PageConfiguration.getInstance(globalContext);

if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
	%>
<div id="one-component-edit"></div><%
}

String area = request.getParameter("area");
if (area != null) {
	ctx.setArea(area);
} else {
	ctx.setArea(ComponentBean.DEFAULT_AREA);
	area=ComponentBean.DEFAULT_AREA;
}

String path = request.getParameter("_wcms_content_path");
if (path!=null) {
	ctx.setPath(path);
}

AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
//GlobalContext globalContext = GlobalContext.getInstance(request); EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());

if ( ctx.getSpecialContentRenderer() != null && area.equals(ComponentBean.DEFAULT_AREA)) {
	%>
<jsp:include page="<%=ctx.getSpecialContentRenderer()%>" /><%
} else {
MenuElement currentPage = ctx.getCurrentPage();

Template template = pageConfig.getCurrentTemplate(ctx, currentPage);

if ( (ctx.getSpecialContentRenderer() == null || !area.equals(ComponentBean.DEFAULT_AREA) ) || template.getAreasForceDisplay().contains(area)) { // display only if page contains only repeat content (supose it is teaser)

Map<String, String> replacement = currentPage.getReplacement();

IContentComponentsList elems = currentPage.getContent(ctx);
//String[] userRoles = editCtx.getUserRoles();

boolean access = true;
//if ( userRoles.length > 0 ) {
	if ( currentPage.getUserRoles().length > 0 ) {
		IUserFactory userFactory = UserFactory.createUserFactory(globalContext, request.getSession());
		User user = userFactory.getCurrentUser(request.getSession());
		if ( user == null ) {
			access=false;
		} else {
			access=user.validForRoles(currentPage.getUserRoles());
		}
	}
//}

/*if ((ctx.getRenderMode() == ContentContext.PREVIEW_MODE)&&(security.haveRight((User)editCtx.getUserPrincipal(), "update"))) {
	ctx.setRenderMode(ContentContext.EDIT_MODE);	
	ctx.setRenderMode(ContentContext.PREVIEW_MODE);
}*/

IContentVisualComponent elem = null;

if ( !access ) {
	if (request.getAttribute("insert_only_one_login") == null) {
		request.setAttribute("insert_only_one_login", "anything");
		%><jsp:include page="/jsp/login.jsp" flush="true"/><%		
	} else {
		%>&nbsp;<%
	}
} else {
	boolean languageChange = !ctx.getContentLanguage().equals(ctx.getLanguage()); 
	if (languageChange) {
		%><div lang="<%=ctx.getContentLanguage()%>"><%
	}
	boolean inContainer = false;
	while (elems.hasNext(ctx)) {
		elem = elems.next(ctx);
		elem.clearReplacement();
		elem.replaceAllInContent(replacement);
		if (!elem.isContainer()) {%><%}
		out.flush(); /* needed for jsp include */
		
		String savedValue = elem.getValue(ctx);
		String value = elem.getValue(ctx);
		
		String keyword = request.getParameter("keyword");		
		savedValue = elem.getValue(ctx);
		value = elem.getValue(ctx);
		
		String XHTMLCode = elem.getXHTMLCode(ctx);
		if (!elem.isContainer()) {
			if (!inContainer) {
				XHTMLCode = elem.getPrefixViewXHTMLCode(ctx)+XHTMLCode+elem.getSufixViewXHTMLCode(ctx);
			}%>
<%=elems.getPrefixXHTMLCode(ctx)%>
<%=XHTMLCode%>
<%=elems.getSufixXHTMLCode(ctx)%><%
		} else {
			if (inContainer) {
				inContainer = false;
				%>
<%=elem.getSufixViewXHTMLCode(ctx)%>
<%=elems.getSufixXHTMLCode(ctx)%><%
			} else {
				inContainer = true;
				%>
<%=elems.getPrefixXHTMLCode(ctx)%>
<%=elem.getPrefixViewXHTMLCode(ctx)%><%
			}
		}
		elem.setValue(savedValue);

		/* close column sequence */
		if (elem.next() == null) {
			ColumnContext columnContext = ColumnContext.getInstance(request);
			if (columnContext.isOpen()) {
				if (columnContext.isWithTable()) {
					%></td></tr></table><%
				} else {
					%></div><div class="content_clear">&nbsp;</div><%
				}
			}

		}

	}
	if (languageChange) {
		%></div><%
	}
}

%><div class="content_clear"><span>&nbsp;</span></div> <!-- end of float elems --><%
}
currentPage.endRendering(ctx);
} /* end else getSpecialContentRenderer() */

%>