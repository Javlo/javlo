<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">

<c:if test="${empty event}">no event</c:if>
<c:if test="${not empty event}">
<c:if test="${fn:length(event.participants)==0}">
	<div class="alert alert-warning">${i18n.edit['global.empty-list']} ${fn:length(event.participants)}</div>
</c:if>

<c:if test="${not empty event.participantsFileURL}">
	<a href="${event.participantsFileURL}" class="btn btn-default">Download</a>
</c:if>

<ul class="users-list">	
	<c:forEach var="user" items="${event.participants}">		
		<c:url var="editURL" value="${info.currentURL}">
			<c:param name="webaction" value="user.edit" />
			<c:param name="module" value="users" />
			<c:param name="cuser" value="${user.encryptLogin}" />
		</c:url>
		<li><b>${user.lastName} ${user.firstName}</b> <span class="pull-right">[${user.login}]</span> </li>
	</c:forEach>
</ul></c:if>
</div>