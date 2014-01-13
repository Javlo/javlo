<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${not empty messages.globalMessage}">

<div class="message ${messages.globalMessage.typeLabel}">
	<c:if test="${not empty value}"><div class="message-info">${value}</div></c:if>
	<span>${messages.globalMessage.message}</span>
</div>
</c:if>

