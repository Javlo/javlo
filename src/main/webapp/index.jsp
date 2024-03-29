<%@page contentType="text/html"
%><%@page import="org.javlo.data.InfoBean"
%><%@page import="org.javlo.helper.URLHelper"
%><%@page import="org.javlo.helper.StringHelper"
%><%@page import="org.javlo.helper.NetHelper"
%><%@page import="jakarta.servlet.http.HttpServletResponse"
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
	lg = request.getLocale().getLanguage();
}

if (lg.trim().length() == 0 || !globalContext.getLanguages().contains(lg)) {
	if (globalContext != null) {
		lg = globalContext.getDefaultLanguage();
	}
}

if (!globalContext.getLanguages().contains(lg)) {
	lg = globalContext.getLanguages().iterator().next();
}

ctx.setAllLanguage(lg);
i18nAccess.changeViewLanguage(ctx);

ContentService content = ContentService.getInstance(globalContext);
Template template = ctx.getCurrentTemplate();
InfoBean.updateInfoBean(ctx);
if ((template != null)&&(template.getHomeRenderer(globalContext) != null)) {
%><jsp:include page="<%=template.getHomeRendererFullName(globalContext)%>"></jsp:include><%	
} else {
	ctx.setFormat("html");
	ctx = new ContentContext(ctx);
	ctx.setViewPrefix(true);
	String forcePathPreview = ctx.getPathPrefix(request);
	ctx.setForcePathPrefix("");
	ctx.setInternalURL(true);	
	String url = URLHelper.createURLWithtoutContext(ctx, ctx.getPath());
	/*if (!ctx.getGlobalContext().getStaticConfig().isURIWithContext()) {
		url = URLHelper.mergePath(request.getContextPath(),url);
	}*/
	ctx.setForcePathPrefix(request,forcePathPreview);	
	//request.getRequestDispatcher(url).forward(request, response);
	//response.sendRedirect(url);
	//NetHelper.sendRedirectTemporarily(response, url);
	//url = URLHelper.createForwardURL(ctx, url);
	request.getRequestDispatcher(url).forward(request, response);
	%>cp=<%=ctx.getRequest().getContextPath()%> <a href="<%=url%>"><%=url%></a><%
}%>