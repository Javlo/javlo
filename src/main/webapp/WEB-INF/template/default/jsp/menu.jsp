<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@
taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${empty menuCurrentPage}"><c:set var="menuCurrentPage" value="${info.root}" scope="request" /></c:if>
<c:if test="${not empty menuDepth}"><c:set var="menuDepth" value="${menuDepth+1}" scope="request" /></c:if>
<c:if test="${empty menuDepth}"><c:set var="menuDepth" value="${1}" scope="request" /></c:if>
<c:if test="${fn:length(menuCurrentPage.children)>0}"><ul>
<c:set var="index" value="1" />
<c:forEach var="child" items="${menuCurrentPage.children}" varStatus="status">
	<c:if test="${child.visible}"><li class="depth-${menuDepth}${child.selected?' selected':''}${child.lastSelected?' last':''} item-${index}"><a href="${child.url}">${child.info.label}</a>
		<c:if test="${toDepth > menuDepth && (extended || child.selected)}"><c:set var="menuCurrentPage" value="${child}" scope="request" />
			<jsp:include page="menu.jsp"></jsp:include></c:if>		
	</li>
	<c:set var="index" value="${index+1}" />
	</c:if>
</c:forEach>
</ul></c:if><c:set var="menuDepth" value="${menuDepth-1}" scope="request" />