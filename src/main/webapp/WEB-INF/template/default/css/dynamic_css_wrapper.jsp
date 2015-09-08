<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><c:set var="rows" value="${info.template.rows}" scope="request" />
<c:set var="dynamicCSSTemplate" value="${info.template}" scope="request" />
<jsp:include page="dynamic_css.jsp" />
<c:forEach var="page" items="${info.page.children}" varStatus="status">
<c:set var="dynamicCSSTemplate" value="${page.template}" scope="request"/>
<jsp:include page="dynamic_css.jsp?prefix=.page-${status.index+1} " />
</c:forEach>