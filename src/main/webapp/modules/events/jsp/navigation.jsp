<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">	
	<c:forEach var="eventItem" items="${events}">		
		<li class="${event.id eq eventItem.id?'current':''}"><a href="${info.currentURL}?event=${eventItem.id}">${eventItem.summary}</a></li>
	</c:forEach>
</ul>