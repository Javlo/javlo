<%@page import="org.javlo.helper.URLHelper"%>
<%@page import="org.javlo.context.ContentContext"%>
<%@page import="org.javlo.navigation.MenuElement"%>
<%@page import="java.util.Map"%>
<%@page import="org.javlo.context.GlobalContext"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
 
%><%
GlobalContext globalContext = GlobalContext.getInstance(request);
ContentContext ctx = ContentContext.getContentContext(request, response);
if (ctx.getCurrentEditUser() == null) {
	%>NO ACCESS<%
} else {
%>
<br />
<h1>URL Analysis</h1>
<ul>
	<li>request URI : <%=request.getRequestURI()%></li>
	<li>request encoding : <%=request.getCharacterEncoding()%></li>
	<li>ctx Path : <%=ctx.getPath()%></li>
	<li>Current Page : <%=ctx.getCurrentPage()%></li>
	<li>URL Factory : <%=ctx.getGlobalContext().getURLFactoryClass()%></li>	
	<%if (ctx.getCurrentPage() != null) {%>
	<li><ul>
		<li><b>page details</b></li>
		<li>Page path : <%=ctx.getCurrentPage().getPath()%></li>
		<li>Page URL : <%=URLHelper.createURL(ctx)%></li>
	</ul></li>	
	<%if (ctx.getGlobalContext().getURLFactory(ctx) != null) {
		String pageURL = ctx.getGlobalContext().getURLFactory(ctx).createURL(ctx, ctx.getCurrentPage());
	%>
	<li><ul>
		<li><b>URL factory</b></li>
		<li> pageURL = <%=pageURL%></li>
		<li>createURL = <%=ctx.getGlobalContext().getURLFactory(ctx).createURL(ctx, ctx.getCurrentPage()) %></li>
		<li>createURLKey = <%=ctx.getGlobalContext().getURLFactory(ctx).createURLKey(pageURL)%></li>
		<li>format = <%=ctx.getGlobalContext().getURLFactory(ctx).getFormat(ctx, pageURL) %></li>
	</ul></li>
	<%} }%>
</ul><%
}
%>
