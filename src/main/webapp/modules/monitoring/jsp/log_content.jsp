<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib uri="jakarta.tags.functions" prefix="fn"%>
<c:set var="buffer" value="" />
<c:forEach var="line" items="${logLines}" varStatus="status">
	<c:set var="buffer">
		<c:choose>
			<c:when test="${empty line.level}">
				<c:set var="notificationClass" value="notification" />
			</c:when>
			<c:when test="${line.level == 'INFO'}">
				<c:set var="notificationClass" value="notification msginfo" />
			</c:when>
			<c:when test="${line.level == 'SEVERE'}">
				<c:set var="notificationClass" value="notification msgerror" />
			</c:when>
			<c:when test="${line.level == 'WARNING'}">
				<c:set var="notificationClass" value="notification msgalert" />
			</c:when>
			<c:otherwise>
				<c:set var="notificationClass" value="notification msginfo" />
			</c:otherwise>
		</c:choose>
		<div class="log-line ${notificationClass}">
			<pre class="log-text"><c:out value="${line.text}" escapeXml="true" /></pre>
		</div>
	</c:set>
	<c:if test="${not status.last}">
		<c:out value="${buffer}" escapeXml="false" />
	</c:if>
</c:forEach>
<div id="log-next-lines">
	<c:out value="${buffer}" escapeXml="false" />
	<form class="ajax" action="${info.currentURL}" method="post">
		<input type="hidden" name="lastLine" value="${logLastLine - 1}" />
		<input class="ajax" type="submit" value="${i18n.edit['monitoring.log.refresh']}" />
	</form>
</div>

