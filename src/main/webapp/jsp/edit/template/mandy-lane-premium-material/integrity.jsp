<%@page import="org.javlo.service.integrity.IntegrityFactory"%>
<%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.context.GlobalContext"
%><%@page import="org.javlo.service.NotificationService"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%
ContentContext ctx = ContentContext.getContentContext(request, response);
if (request.getParameter("path") != null) {
	ctx.setPath(request.getParameter("path"));
}
GlobalContext globalContext = GlobalContext.getInstance(request);
IntegrityFactory integrityFactory = IntegrityFactory.getInstance(ctx);
request.setAttribute("integrities", integrityFactory.getChecker());
%>
<div class="messagelist">
    <h4>Integrities</h4>
    <ul>
    	<c:forEach var="checker" items="${integrities}">
			<li class="level-${checker.levelLabel}"><c:if test="${not empty checker.componentId}"><a href="${info.currentURL}?pushcomp=${checker.componentId}&area=${checker.area}&webaction=edit.changeArea"></c:if>
				<span>${checker.errorMessage}</span>      		        		
            	<small>${checker.levelLabel} (${checker.errorCount})</small>
            	<c:if test="${not empty checker.componentId}"></a></c:if>
        </li>
        </c:forEach>
   	</ul>   
</div>