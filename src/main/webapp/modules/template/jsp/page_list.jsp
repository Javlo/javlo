<%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.module.template.TemplateAction"
%><%@page import="org.javlo.module.template.PageTemplateRef"
%><%@page import="java.util.Collection"
%><%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><%
ContentContext ctx = ContentContext.getContentContext(request, response);
Collection<PageTemplateRef> pages = TemplateAction.searchPageTemplate(ctx, request.getParameter("name"));
request.setAttribute("pages", pages);
%>
<style type="text/css">
.body {
	padding: 15px;
	width: 600px;
}
.body {
    margin: 0 15px;
}
</style>
<div class="body">
<h2>Usage of ${param.name}</h2>
<c:if test="${fn:length(pages) == 0}">
<p>template '${param.name}' never used.</p>
</c:if><c:if test="${fn:length(pages) > 0}">
<div class="pages-container">
<table class="pages table">
	<tr>
		<th>&nbsp;</th>		
		<th>page</th>
		<th>usage</th>
	</tr>
	<c:forEach var="page" items="${pages}">
	
	<c:if test="${context != page.context}">
	<c:set var="context" value="${page.context}" />
	<tr><th colspan="3">${page.context}</th></tr>
	</c:if>
	
	<tr>		
		<td>&nbsp;</td>
		<td><a href="${page.url}">${page.label}</a></td>
		<td>${page.usage}</td>
	</tr>
	</c:forEach>
</table>
</div></c:if>
</div>