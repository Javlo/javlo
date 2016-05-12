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
<h1>viewPages cache content (size:<%= globalContext.viewPages.size()%>)</h1>
<ul>
<%for (Map.Entry<String, MenuElement> entry : globalContext.viewPages.entrySet()) {
	%><li><b><%=entry.getKey()%> - </b> name:<%=entry.getValue().getName()%> path:<%=entry.getValue().getPath()%></li><%
}%>
</ul><%
}
%>
