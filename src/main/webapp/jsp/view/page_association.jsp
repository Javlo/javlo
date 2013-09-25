<%@page import="org.javlo.helper.URLHelper"
%><%@page import="org.javlo.template.TemplateFactory"
%><%@page import="org.javlo.template.Template"
%><%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.navigation.MenuElement"
%><%
ContentContext ctx = ContentContext.getContentContext(request, response);
MenuElement currentPage = ctx.getCurrentPage();

request.setAttribute("pageAssociation", true);
for (MenuElement child : currentPage.getChildMenuElements()) {
	Template childTemplate = TemplateFactory.getTemplate(ctx, child);
	ctx.setCurrentPageCached(child);
	ctx.setCurrentTemplate(childTemplate);
	String jspURI = childTemplate.getRendererFullName(ctx);
	if (child.getPreviousBrother() == null && child.getNextBrother() != null) {
		jspURI = URLHelper.addAllParams(jspURI, "no-close-body=true");
	} else if (child.getPreviousBrother() != null && child.getNextBrother() == null) {
		jspURI = URLHelper.addAllParams(jspURI, "no-open-body=true");
	} else {
		jspURI = URLHelper.addAllParams(jspURI,  "no-close-body=true", "no-open-body=true");
	}
	
	%><jsp:include page="<%=jspURI%>" /><%
}%>
