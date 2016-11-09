<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<c:if test="${not empty comp.value}"><h${contentContext.titleDepth}>${comp.value}</h${contentContext.titleDepth}></c:if>
<ul class="clouds-tag">
	<c:if test="${fn:length(cloudTag)==0}">${i18n.view['global.no-result']}</c:if>
	<c:forEach var="link" items="${cloudTag}">
		<li class="${link.style}"><a href="${link.url}">${link.label}</a></li>
	</c:forEach>	
</ul>