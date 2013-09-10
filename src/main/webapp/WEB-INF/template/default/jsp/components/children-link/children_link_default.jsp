<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul name="children-link">
<c:forEach var="child" items="${children}" varStatus="status">
	<li><a href="${child.url}" ${child.selected?'class="selected"':''}>${child.fullLabel}</a></li>
</c:forEach>
</ul>