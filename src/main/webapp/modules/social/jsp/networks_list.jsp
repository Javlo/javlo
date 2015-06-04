<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="content">
	<div class="row"><div class="col-md-6">	
	<c:forEach var="currentNetwork" items="${networks}" varStatus="status">
	<c:set var="network" value="${currentNetwork}" scope="request" />
	<c:if test="${(status.count)>1 && status.count%2 != 0}">
		</div><div class="col-md-6">
	</c:if>	
	<c:choose>
		<c:when test="${network.name == 'facebook'}">
			<jsp:include page="facebook.jsp"></jsp:include>
		</c:when>
		<c:otherwise>
			<jsp:include page="default.jsp"></jsp:include>
	</c:otherwise>
	</c:choose>
	<c:if test="${(status.count)>1 && status.count%2 != 0}">
		<div></div><div class="row"><div class="col-md-6">
	</c:if>
	</c:forEach>
	</div>
	</div>
</div>