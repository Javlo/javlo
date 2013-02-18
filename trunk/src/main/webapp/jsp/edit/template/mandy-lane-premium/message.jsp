<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${not empty message}"><c:if test="${message.type == 3 && !info.editPreview && !info.preview}">
	<script type="text/javascript">jQuery.jGrowl("${fn:escapeXml(message.message)}", { life: 5000, position: "top-right"});</script>
</c:if><c:if test="${message.type != 3 || info.editPreview}"><div class="notification msg${message.typeLabel}"><a class="close" title="${i18n.edit['global.close']}"></a>${message.message}</div></c:if></c:if>