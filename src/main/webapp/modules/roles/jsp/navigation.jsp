<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">	
	<c:forEach var="role" items="${info.adminRoles}">		
		<li class="${rolesContext.role eq role?'current':''}"><a href="${info.currentURL}?webaction=ChangeRole&role=${role}">${role}</a></li>
	</c:forEach>
</ul>