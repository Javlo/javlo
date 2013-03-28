<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty links}">
	<c:set var="links" value="${moduleContext.navigation}" scope="request" />
</c:if>
<ul class="navigation">	
<c:forEach var="link" items="${links}">	
<li ${moduleContext.currentLink eq link.name?'class="current"':''}>
	<c:if test="${not empty link.renderer}"><a href="${info.currentURL}${link.url}"></c:if>
	<c:if test="${empty link.renderer}"><span></c:if>
	${link.label}
	<c:if test="${empty link.renderer}"></span></c:if>
	<c:if test="${not empty link.renderer}"></a></c:if>	
<c:if test="${not empty link.children}">
	<c:set var="links" value="${link.children}" scope="request" />
	<jsp:include page="navigation.jsp"></jsp:include>
</c:if>
</li>
</c:forEach>	
</ul>