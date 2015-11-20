<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="content">
	<div class="row">	
	<c:forEach var="currentNetwork" items="${networks}" varStatus="status">
	<c:set var="network" value="${currentNetwork}" scope="request" />
	<div class="col-md-4">	
	<c:choose>
		<c:when test="${network.name == 'facebook'}">
			<jsp:include page="facebook.jsp"></jsp:include>
		</c:when>
		<c:otherwise>
			<jsp:include page="default.jsp"></jsp:include>
	</c:otherwise>
	</c:choose>
	</div>
	<c:if test="${(status.index)>1 && status.count%3 == 0}">
		</div><div class="row">
	</c:if>
	</c:forEach>	
	</div>
</div>