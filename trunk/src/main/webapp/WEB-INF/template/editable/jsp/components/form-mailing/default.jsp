<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<div class="mailing">
<c:if test="${not empty messages.globalMessage}">
	${messages.globalMessage.xhtml}
</c:if>
<c:if test="${empty messages.globalMessage || messages.globalMessage.type == 1 || messages.globalMessage.type == 2 || messages.globalMessage.type == 5}">
<form method="post">
<div class="line">
	<input class="hidden" name="comp-id" value="${comp.id}" />
	<input class="hidden" name="webaction" value="mailing-registration.submit" />
	<label for="email">${i18n.view['field.email']}</label>
	<input type="text" name="email" placeholder="${i18n.view['field.email']}" />	
</div>
<c:if test="${name}">
<div class="line">
	<label for="firstname">${i18n.view['field.firstname']}</label>
	<input type="text" id="firstname" name="firstname" placeholder="${i18n.view['field.firstname']}" />	
</div>
<div class="line">
	<label for="lastname">${i18n.view['field.lastname']}</label>
	<input type="text" id="lastname" name="lastname" placeholder="${i18n.view['field.lastname']}" />	
</div>
</c:if>
<div class="action">
<input type="submit" value="ok" />
</div>
</form>
</c:if>
</div>
