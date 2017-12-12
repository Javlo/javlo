<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">	
	<li class="${calendarPage?'current':''}"><a href="${info.currentURL}?calendar=true">Calendar</a></li>
	<c:forEach var="eventItem" items="${events}">		
		<li class="${event.id eq eventItem.id && !calendarPage?'current':''}"><a href="${info.currentURL}?event=${eventItem.id}">${eventItem.summary}</a></li>
	</c:forEach>
</ul>