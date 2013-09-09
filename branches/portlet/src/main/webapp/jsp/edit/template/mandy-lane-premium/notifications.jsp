<%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.context.GlobalContext"
%><%@page import="org.javlo.service.NotificationService"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%
GlobalContext globalContext = GlobalContext.getInstance(request);
NotificationService notifService = NotificationService.getInstance(globalContext);						
ContentContext ctx = ContentContext.getContentContext(request, response);
request.setAttribute("notifications", notifService.getNotifications(ctx.getCurrentUserId() , 9, true));
notifService.getNotifications(ctx.getCurrentUserId() , 9999999, true); // mark all as read
%>
<div class="messagelist">
    <h4>Notifications</h4>
    <ul>
    	<c:forEach var="notif" items="${notifications}">
			<li class="${notif.notification.typeLabel}${!notif.read?' current':''}">
				<c:if test="${not empty notif.notification.url}"><a href="${notif.notification.url}">${notif.notification.displayMessage}</a></c:if>
				<c:if test="${empty notif.notification.url}"><span>${notif.notification.displayMessage}</span></c:if>        		        		
            	<small>${notif.notification.timeLabel}</small>
        </li>
        </c:forEach>
   	</ul>   
</div>