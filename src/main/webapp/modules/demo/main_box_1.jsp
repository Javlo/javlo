<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<form id="form-demo" action="">
	<input type="hidden" name="webaction" value="test" method="post" />
	<input type="submit" value="test" />
</form>

<h2>Notifications</h2>

<ul>
<c:forEach var="notif" items="${notifications}">
	<li>${notif.read} - ${notif.notification.message} - ${notif.notification.type} - ${notif.notification.userId} </li>
</c:forEach>
</ul>