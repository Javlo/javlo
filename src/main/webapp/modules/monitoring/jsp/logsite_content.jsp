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
			<c:when test="${line.level == 'TEMPORARY'}">
				<c:set var="notificationClass" value="notification msgsuccess" />
			</c:when>
			<c:otherwise>
				<c:set var="notificationClass" value="notification msginfo" />
			</c:otherwise>
		</c:choose>
		<div class="log-line ${notificationClass} group-${line.group} level-${line.level}">
			<pre class="log-text">${line.sortableTime} - [${line.group}] >>> <c:out value="${line.text}" escapeXml="true" /></pre>
			<div class="stacktrace">
				<select>
				<c:forEach var="stackElement" items="${line.stackTrace}" varStatus="status">
					<c:if test="${status.index>1}">
					<option>${stackElement}</option>
					</c:if>
				</c:forEach>
				</select>			
			</div>
			
		</div>
	</c:set>
	<c:if test="${not status.last}">
		<c:out value="${buffer}" escapeXml="false" />
	</c:if>
</c:forEach>
