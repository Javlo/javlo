<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${style == 'focus'}">
    <div style="margin: 10px; padding: 10px; background-color: #aa2222; border: 1px #333333 solid;">
</c:if><c:if test="${style == 'border-left-top'}">
	<div style="padding: 10px; margin-bottom: 10px; border-left-width: 3px; border-left-color: #aa2222; border-left-style: solid; border-top-width: 3px; border-top-color: #aa2222; border-top-style: solid; font-size: 0.4em;">
</c:if><c:if test="${style == 'border-left-bottom'}">
	<div style="padding: 10px; margin-bottom: 10px; border-left-width: 3px; border-left-color: #aa2222; border-left-style: solid; border-bottom-width: 3px; border-bottom-color: #aa2222; border-bottom-style: solid; font-size: 0.4em;">
</c:if><c:if test="${style == 'border-right-top'}">
    <div style="padding: 10px; margin-bottom: 10px; border-right-width: 3px; border-right-color: #aa2222; border-right-style: solid; border-top-width: 3px; border-top-color: #aa2222; border-top-style: solid; font-size: 0.4em;">
</c:if><c:if test="${style == 'border-right-bottom'}">
    <div style="padding: 10px; margin-bottom: 10px; border-right-width: 3px; border-right-color: #aa2222; border-right-style: solid; border-bottom-width: 3px; border-bottom-color: #aa2222; border-bottom-style: solid; font-size: 0.4em;">
</c:if><c:if test="${style == 'border-all'}">
    <div style="padding: 10px; margin-bottom: 10px; border-width: 3px; border-color: #aa2222; border-style: solid; font-size: 0.4em;">
</c:if>
${text}
<c:if test="${style == 'focus' || style == 'border-left-top' || style == 'border-left-bottom' || style == 'border-right-top' || style == 'border-right-bottom' || style == 'border-all'}">
	</div>
</c:if>
