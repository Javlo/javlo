<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
 <c:forEach var="ticket" items="${tickets}">
 <c:if test="${not empty ticket.title}">
 <c:url var="ticketURL" value="${info.currentURL}" context="/">
  	<c:param name="id" value="${ticket.id}" />
 </c:url>
 <div class="panel${ticket.deleted?' deleted':''}  mobile-ticket">
 	<div class="panel-heading">
 		<a class="${ticket.status}" href="${ticketURL}">
 		<span class="status ${ticket.status}">${ticket.status}</span>
 		<span class="${ticket.read?'read':'unread'}">${empty ticket.title ? '?' : ticket.title}</span> 		
 		</a>
 	</div>
 	<div class="panel-body">
 	<p>${ticket.message}</p>
    <ul class="site">
		<li><span class="label">authors</span>${ticket.authors}</li>
		<li><span class="label">last update</span>${ticket.lastUpdateDateLabel}</li>		
		<li><span class="label">#comment</span>${fn:length(ticket.comments)}</li>
	</ul>
  	</div>
 </div>  
 </c:if>   
</c:forEach>
</div>