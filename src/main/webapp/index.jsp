<%@page contentType="text/html"
%><%@page import="org.javlo.helper.URLHelper"
%><%@page import="org.javlo.helper.StringHelper"
%><%@page import="org.javlo.helper.NetHelper"
%><%@page import="javax.servlet.http.HttpServletResponse"
%><%@page import="org.javlo.context.GlobalContext"
%><%@page import="org.javlo.i18n.I18nAccess"
%><%@page import="org.javlo.template.Template"
%><%@page import="org.javlo.context.ContentContext,org.javlo.config.StaticConfig"
%><%@page import="org.javlo.service.ContentService"%><%

GlobalContext globalContext = GlobalContext.getInstance(request);
I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());

ContentContext ctx = ContentContext.getContentContext(request, response);

String lg=ctx.getCookieLanguage();
if (lg == null) {
	lg = "";
}

if (lg.trim().length() == 0) {
	if (globalContext != null) {
		lg = globalContext.getDefaultLanguages().iterator().next();
	}
}

if (lg.trim().length() == 0) {
	lg = request.getLocale().getLanguage();
}

if (!globalContext.getLanguages().contains(lg)) {
	lg = globalContext.getLanguages().iterator().next();
}

ctx.setLanguage(lg);
i18nAccess.changeViewLanguage(ctx);

ContentService content = ContentService.getInstance(globalContext);
Template template = ctx.getCurrentTemplate();
if ((template != null)&&(template.getHomeRenderer(globalContext) != null)) {
%><jsp:include page="<%=template.getHomeRendererFullName(globalContext)%>"></jsp:include><%	
} else {
	ctx.setFormat("html");
	String url = URLHelper.createURL(ctx);	
	response.sendRedirect(url);
	%><a href="<%=url%>"><%=url%></a><%
}%>