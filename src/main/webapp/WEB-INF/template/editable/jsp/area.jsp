<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><c:set var="template" value="${areaStyle}" scope="request"
/><c:if test="${not empty areaStyle.name}"><td id="${areaStyle.name}" class="_area" width="${areaStyle.finalWidth}" valign="top" align="left" ><jsp:include page="/jsp/view/content_view.jsp?area=${areaStyle.name}" /></td></c:if>