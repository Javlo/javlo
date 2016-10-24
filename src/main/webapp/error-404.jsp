<%@page contentType="text/html"
%><%@page import="java.net.URL"
%><%@page import="org.javlo.helper.URLHelper"
%><%@page import="org.javlo.helper.NetHelper"
%><%@page import="javax.servlet.http.HttpServletResponse"
%><%@page import="org.javlo.context.GlobalContext"
%><%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.service.ContentService"%><%
GlobalContext globalContext = GlobalContext.getInstance(request);
ContentContext ctx = ContentContext.getContentContext(request, response);
ctx = new ContentContext(ctx);
ctx.setFormat("html");
ctx.setViewPrefix(true);
ctx.setAbsoluteURL(true);
String url = URLHelper.createURL(ctx, "404");
String content404Page = "<p>404 page not found.</p>";
try {
	content404Page = NetHelper.readPageGet(new URL(url), false);
} catch (Exception e) {	
}
%><%=content404Page%>