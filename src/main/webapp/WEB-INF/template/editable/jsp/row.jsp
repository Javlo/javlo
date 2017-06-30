<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:set var="rowEmpty" value="true" />
<c:forEach var="currentArea" items="${row.areas}"><c:if test="${empty info.areaEmpty[currentArea.name]}"><c:set var="rowEmpty" value="false" /></c:if></c:forEach>
<c:if test="${(not rowEmpty) || editPreview || param['_display-zone']}">
<c:set var="template" value="${row}" scope="request" /><table width="100%" style="border-collapse: collapse;" cellpadding="0" cellspacing="0" border="0"><tr id="${row.name}" class="responsive-${row.responsive}">
<c:forEach var="currentArea" items="${row.areas}"><c:set var="areaStyle" value="${currentArea}" scope="request" />
<jsp:include page="area.jsp" />
</c:forEach></tr></table>
</c:if>