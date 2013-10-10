<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${fn:length(pages)>0}">
<c:if test="${not empty title}"><h4>${title}</h4></c:if>
<ul>
<c:forEach items="${pages}" var="page" varStatus="status">
<li class="${page.name}${page.selected?' selected':''}"><a title="${page.title}" href="${page.url}">${page.title}</a></li>
</c:forEach>
</ul>
</c:if>