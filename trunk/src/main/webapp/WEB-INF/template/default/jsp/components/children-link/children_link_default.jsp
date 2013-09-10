<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul name="children-link">
<c:forEach var="child" items="${children}" varStatus="status">
	<li><a href="${child.url}" ${child.selected?'class="selected"':''}>${child.fullLabel}</a>
	<c:if test="${fn:length(child.children) > 0}">
		<c:set var="children" value="${child.children}" />
		<jsp:include page="children_link_default.jsp" />
	</c:if>
	</li>
</c:forEach>
</ul>