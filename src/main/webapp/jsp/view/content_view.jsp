<%@page contentType="text/html"
        import="
        java.util.Map,
        java.util.Stack,
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
		org.javlo.component.container.IContainer,
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
	%><div id="one-component-edit"></div><%
}

if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
	%><div id="comp_0" class="free-edit-zone editable-component"><span>&nbsp;</span></div><%
}

String area = request.getParameter("area");
if (area != null) {
	ctx.setArea(area);
} else {
	ctx.setArea(ComponentBean.DEFAULT_AREA);
	area=ComponentBean.DEFAULT_AREA;
}
request.setAttribute("area", area);

String path = request.getParameter("_wcms_content_path");
if (path!=null) {
	ctx.setPath(path);
}

Boolean removeRepeat = StringHelper.isTrue(request.getParameter("_no-repeat"));

AdminUserSecurity security = AdminUserSecurity.getInstance();

if ( ctx.getSpecialContentRenderer() != null && area.equals(ComponentBean.DEFAULT_AREA)) {
	%>
<jsp:include page="<%=ctx.getSpecialContentRenderer()%>" /><%
} else {
MenuElement currentPage = ctx.getCurrentPage();

Template template = ctx.getCurrentTemplate();
Stack<IContainer> containers = new Stack<IContainer>();

if ( (ctx.getSpecialContentRenderer() == null || !area.equals(ComponentBean.DEFAULT_AREA) ) || template.getAreasForceDisplay().contains(area)) { // display only if page contains only repeat content (supose it is teaser)

Map<String, String> replacement = currentPage.getReplacement();

IContentComponentsList elems = currentPage.getContent(ctx);
IContentVisualComponent elem = null;

	boolean languageChange = !ctx.getContentLanguage().equals(ctx.getLanguage()); 
	if (languageChange) {
		%><div lang="<%=ctx.getContentLanguage()%>"><%
	}
	
	while (elems.hasNext(ctx)) {
		pageEmpty = false;
		elem = elems.next(ctx);
		if (!(removeRepeat && elem.isRepeat() && !elem.getPage().equals(currentPage))) {
			elem.clearReplacement();
			elem.replaceAllInContent(replacement);
		
			out.flush(); /* needed for jsp include */
			
			String savedValue = elem.getValue(ctx);
			String value = elem.getValue(ctx);
			
			savedValue = elem.getValue(ctx);
			value = elem.getValue(ctx);
			
			if (elem instanceof IContainer) {
				IContainer container = (IContainer)elem;
				if (container.isOpen(ctx)) {
					containers.push(container);
				} else {
					if (!containers.empty()) {
						containers.pop();
					}
				}
			}
			
			%>
<%=elems.getPrefixXHTMLCode(ctx)
%><%=elem.getPrefixViewXHTMLCode(ctx)%>
<%=elem.getXHTMLCode(ctx)%>
<%=elem.getSuffixViewXHTMLCode(ctx)%>
<%=elems.getSufixXHTMLCode(ctx)%><%		
			elem.setValue(savedValue);
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
	}
	while (!containers.empty()) {
		%><%=containers.pop().getCloseCode(ctx)%><%
	}
	if (languageChange) {
		%></div><%
	}
}
currentPage.endRendering(ctx);
} /* end else getSpecialContentRenderer() */

%>