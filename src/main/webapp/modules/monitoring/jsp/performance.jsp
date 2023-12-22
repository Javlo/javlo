<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div class="content">
	<b>total time : ${totalEventTime} [${totalPercentage}]</b>
	<ul>
		<c:forEach var="timeEvent" items="${timeEvents}">
		<li><b>${timeEvent.label} : </b>${timeEvent.timeLabel} [${timeEvent.partOfTotalTime}]</li>
		</c:forEach>
	</ul>
</div>
