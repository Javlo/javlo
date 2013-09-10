<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${fn:length(children)>0}">
<c:if test="${not empty title}">
	<h3>${title}</h3>
</c:if>
<ul name="children-link">
<c:forEach var="child" items="${children}" varStatus="status">
	<li ${child.selected?'class="selected"':'class="unselected"'}><a href="${child.url}">${child.fullLabel}</a></li>
</c:forEach>
</ul>
</c:if> 