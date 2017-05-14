<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<div class="content">
<c:if test="${not empty param.templateh}"><h2>${param.templateh}</h2></c:if>
<div class="tree">
<ul class="tree">
<li>
<a href="${info.currentURL}?webaction=changeRenderer&list=hierarchy"><span class="glyphicon glyphicon-home"></span></a>
<c:forEach var="template" items="${htemps}">	
	<c:if test="${fn:length(template.children)>0}">
		<c:set var="htemps" value="${template.children}" scope="request" />
		<jsp:include page="hierarchy_rec.jsp" />				
	</c:if>
</c:forEach>
</li>
</ul>
	
</div>
</div>