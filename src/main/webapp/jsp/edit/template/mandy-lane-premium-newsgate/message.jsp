<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${not empty messages.globalMessage && messages.globalMessage.type > 0 && !empty messages.globalMessage.message}"><c:if test="${ messages.globalMessage.type == 3 && !info.preview}">
	<script type="text/javascript">jQuery.jGrowl("${fn:escapeXml(messages.globalMessage.message)}", { life: 5000, position: "top-right"});</script>
</c:if><c:if test="${messages.globalMessage.type != 3}"><div class="notification msg${ messages.globalMessage.typeLabel}"><a class="close" title="${i18n.edit['global.close']}"></a>${ messages.globalMessage.message}</div></c:if></c:if>