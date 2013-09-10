<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul name="children-link">
<c:forEach var="child" items="${children}" varStatus="status">
	<li ${child.selected?' class="selected"':''}><a href="${child.url}">${child.fullLabel}</a>
	<c:if test="${fn:length(child.children) > 0 && child.selected}">
		<c:set var="children" value="${child.children}" scope="request" />
		<jsp:include page="children_link_depth_rec.jsp" />
	</c:if>
	</li>
</c:forEach>
</ul>