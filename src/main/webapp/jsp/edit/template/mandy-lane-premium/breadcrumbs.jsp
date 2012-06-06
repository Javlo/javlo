<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${not empty module.currentModule.breadcrumbTitle}">
	<span class="title">${module.currentModule.breadcrumbTitle}</span>
</c:if>
<c:if test="${empty module.currentModule.breadcrumbList}">
	<c:forEach var="page" items="${info.pagePath}">
	<c:set var="link" value='<a href="${page.url}">' />
	${empty param.previewEdit?link:'<span class="title">'}${page.info.title}${empty param.previewEdit?'</a>':'</span>'}
	</c:forEach>
	<span>${info.pageTitle}</span>
</c:if>
<c:if test="${not empty module.currentModule.breadcrumbList}">
	<c:forEach var="link" items="${module.currentModule.breadcrumbList}" varStatus="status">
	<c:if test="${ status.count < fn:length(module.currentModule.breadcrumbList) }">
		<a href="${link.url}" title="${link.title}">${link.legend}</a>
	</c:if>			
	<c:if test="${ status.count == fn:length(module.currentModule.breadcrumbList) }">
		<span>${link.legend}</span>
	</c:if>
	</c:forEach>		
			
</c:if>		


