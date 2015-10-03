<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">

<c:if test="${fn:length(users)==0}">
	<div class="alert alert-warning">${i18n.edit['global.empty-list']}</div>
</c:if>


<ul class="users-list">	
	<c:forEach var="user" items="${users}">		
		<c:url var="editURL" value="${info.currentURL}">
			<c:param name="webaction" value="user.edit" />
			<c:param name="module" value="users" />
			<c:param name="cuser" value="${user.encryptLogin}" />
		</c:url>
		<li><a href="${editURL}">[${user.login}]</a> ${user.lastName} ${user.firstName}</li>
	</c:forEach>
</ul>
</div>