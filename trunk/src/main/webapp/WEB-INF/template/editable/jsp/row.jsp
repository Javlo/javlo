<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:set var="template" value="${row}" scope="request" /><table width="100%" cellspacing="0" border="0"><tr id="${row.name}"><c:forEach var="currentArea" items="${row.areas}"><c:set var="areaStyle" value="${currentArea}" scope="request" />
<jsp:include page="area.jsp" />
</c:forEach></tr></table>