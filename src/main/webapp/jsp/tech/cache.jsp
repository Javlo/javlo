<%@page import="org.javlo.context.ContentContext"%>
<%@page import="org.javlo.cache.ICache"%>
<%@page import="org.javlo.context.GlobalContext"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%>
<%
ContentContext ctx = ContentContext.getContentContext(request, response);
if (ctx.getCurrentEditUser() == null) {
	%>Security error.<%
	return;	
}
GlobalContext gc = GlobalContext.getInstance(request);
%>
<ul>
<li>ehCache ? : <%=gc.isEhCache()%>
<%for (ICache cache : gc.getAllCache()) {%>
<li><b><%=cache.getName()%> : </b><%=cache.getSize() %> items</li><%
}%>
</ul>