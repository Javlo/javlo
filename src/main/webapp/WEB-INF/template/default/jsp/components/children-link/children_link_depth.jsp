<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${fn:length(children)>0}">
<c:if test="${not empty title}">
	<h3>${title}</h3>
</c:if>
<jsp:include page="children_link_depth_rec.jsp" />
</c:if>