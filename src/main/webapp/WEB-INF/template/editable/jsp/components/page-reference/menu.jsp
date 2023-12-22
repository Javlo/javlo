<%@ taglib prefix="c" uri="jakarta.tags.core"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><c:if test="${fn:length(pages)>0}">
<c:if test="${not empty title}"><h4>${title}</h4></c:if>
<ul>
<c:forEach items="${pages}" var="page" varStatus="status">
<li ${page.selected?'class="selected"':''}><a title="${page.title}" href="${page.url}">${page.title}</a></li>
</c:forEach>
</ul>
</c:if>