<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="content">
	<div class="one_half">
	<c:forEach var="service" items="${ecomServices}" varStatus="status">	
	<c:if test="${(status.count-1)==(fn:length(ecomServices)/2)}">
		</div><div class="one_half">
	</c:if>	
	<c:choose>
		<c:when test="${service.name == '_paypal'}">
			<jsp:include page="paypal.jsp"></jsp:include>
		</c:when>
		<c:otherwise>
		<c:set var="service" value="${service}" scope="request"/>
			<jsp:include page="default.jsp"></jsp:include>
	</c:otherwise>
	</c:choose>
	</c:forEach>
	</div>
</div>