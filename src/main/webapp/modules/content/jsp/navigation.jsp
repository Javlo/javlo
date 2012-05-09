<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">	
	<li class="current">	
		<a class="editor" title="parent page" href="<c:url value="${info.parentPageURL}" />">${info.pageTitle}</a>
	</li>
	<li><ul class="children">
	<c:forEach var="child" items="${info.page.children}">	
	<li class="${fn:length(child.children) > 0?'have-children ':''}${child.info.realContent?'real-content':''}"><a href="${child.url}">${child.info.title}</a></li>
	</c:forEach>
	</ul></li>
</ul>