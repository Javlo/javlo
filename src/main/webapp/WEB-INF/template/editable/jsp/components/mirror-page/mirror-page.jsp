<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${style == 'focus'}">
	<div style="margin: 10px; padding: 10px; background-color: #EDCBBA; border: 1px #333333 solid;">
</c:if><c:if test="${style == 'important'}">
	<div style="padding: 10px; border: 1px #333333 solid; font-weight: bold; font-size: 1.2em;">
</c:if><c:if test="${style == 'agenda'}">
	<div style="padding: 10px; border-left: 100px #000000 solid; border-right: 100px #000000 solid; border-top: 1px #000000 solid; border-bottom: 1px #000000 solid; font-size: 0.9em;">
</c:if><c:if test="${style == 'contact'}">
	<div style="padding: 10px; background-color: #81210B; color: #6A7962; border: 1px #333333 solid; font-size: 0.8em;">
</c:if>

${xhtml}

<c:if test="${style == 'focus' || style == 'important' || style == 'agenda' || style == 'contact'}">
	</div>
</c:if>
