<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><c:set var="template" value="${areaStyle}" scope="request"
/><c:if test="${not empty areaStyle.name}">
<td id="${areaStyle.name}" class="_area responsive-${row.responsive}" width="${areaStyle.finalWidth}" valign="top" align="left" >
<c:if test="${not empty areaStyle.finalPadding}"><table style="width: 100%; border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;"><tr class="padding"><td colspan="3" height="${areaStyle.finalPadding}" style="height: ${areaStyle.finalPadding}; font-size: 1px; margin: 0; padding: 0;">&nbsp;</td></tr></c:if>
<c:if test="${not empty areaStyle.finalPadding}"><tr><td  class="padding" width="${areaStyle.finalPadding}" style="width: ${areaStyle.finalPadding}; font-size: 1px; margin: 0; padding: 0;">&nbsp;</td><td></c:if>
<jsp:include page="/jsp/view/content_view.jsp?area=${areaStyle.name}" />
<c:if test="${not empty areaStyle.finalPadding}"></td><td class="padding" width="${areaStyle.finalPadding}" style="width: ${areaStyle.finalPadding}; font-size: 1px; margin: 0; padding: 0;">&nbsp;</td></tr></c:if>
<c:if test="${not empty areaStyle.finalPadding}"><tr class="padding"><td colspan="3" height="${areaStyle.finalPadding}" style="height: ${areaStyle.finalPadding}; font-size: 1px; margin: 0; padding: 0;">&nbsp;</td></tr></table></c:if>
</td>
</c:if>