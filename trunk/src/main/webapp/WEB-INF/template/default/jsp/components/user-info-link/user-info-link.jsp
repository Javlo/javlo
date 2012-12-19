<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="user-info-link">
<c:forEach var="user" items="${users}">
	<div class="vcard">		
		<div class="fn">${user.firstName} ${user.lastName}</div>		
		<c:if test="${not empty user.function}"><div class="role">${user.function}</div></c:if>
		<c:if test="${not empty user.email}"><div class="email"><a href="mailto:${user.email}">${user.email}</a></div></c:if>
		<c:if test="${not empty user.organization}"><div class="org">${user.organization}</div></c:if>
		<c:if test="${not empty user.phone}"><div class="tel">${user.phone}</div></c:if>
		<c:if test="${not empty user.mobile}"><div class="tel mobile">${user.mobile}</div></c:if>
	</div>
</c:forEach>
</div>