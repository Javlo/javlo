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
		org.javlo.helper.URLHelper"
%><%
ContentContext ctx = ContentContext.getContentContext ( request, response );
GlobalContext globalContext = GlobalContext.getInstance(request);
boolean pageEmpty = true;

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

AdminUserSecurity security = AdminUserSecurity.getInstance();

if ( ctx.getSpecialContentRenderer() != null && area.equals(ComponentBean.DEFAULT_AREA)) {
	%>
<jsp:include page="<%=ctx.getSpecialContentRenderer()%>" /><%
} else {
MenuElement currentPage = ctx.getCurrentPage();

Template template = ctx.getCurrentTemplate();

if ( (ctx.getSpecialContentRenderer() == null || !area.equals(ComponentBean.DEFAULT_AREA) ) || template.getAreasForceDisplay().contains(area)) { // display only if page contains only repeat content (supose it is teaser)

Map<String, String> replacement = currentPage.getReplacement();

IContentComponentsList elems = currentPage.getContent(ctx);
IContentVisualComponent elem = null;

	boolean languageChange = !ctx.getContentLanguage().equals(ctx.getLanguage()); 
	if (languageChange) {
		%><div lang="<%=ctx.getContentLanguage()%>"><%
	}
	boolean inContainer = false;
	while (elems.hasNext(ctx)) {
		pageEmpty = false;
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
					%></div><%
				}
			}

		}

	}
	if (languageChange) {
		%></div><%
	}
}
currentPage.endRendering(ctx);
} /* end else getSpecialContentRenderer() */

%>