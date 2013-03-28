<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:forEach items="${links}" var="link">
<ul class="subtitle-link">
<c:if test="${link.level ==  '2'}">
	<li><a href="${link.url}">${link.label}</a>
</c:if>
</ul>
</c:forEach>