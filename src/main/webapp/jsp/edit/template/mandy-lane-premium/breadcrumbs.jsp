<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${not empty modulesContext.currentModule.breadcrumbTitle}">
	<span class="title">${modulesContext.currentModule.breadcrumbTitle}</span>
</c:if>
<c:if test="${empty modulesContext.currentModule.breadcrumbList}">
	<c:forEach var="page" items="${info.pagePath}">
		<c:set var="link" value='<a href="${page.url}">' />
		${empty param.previewEdit?link:'<span class="title">'}${page.info.title}${empty param.previewEdit?'</a>':'</span>'}
		
			<c:if test="${fn:length(page.children) > 1}">
			<div class="children">
			<div class="container">
				<ul>
				<c:forEach var="child" items="${page.children}">
					<li ${child.selected?'class="selected"':''}><a href="${child.url}">${child.info.title}</a></li>
				</c:forEach>
				</ul>
			</div>
			</div>
			</c:if>
		
		</c:forEach>
	<span>${info.pageTitle}</span>
</c:if>
<c:if test="${not empty modulesContext.currentModule.breadcrumbList}">
	<c:forEach var="link" items="${modulesContext.currentModule.breadcrumbList}" varStatus="status">
	<c:if test="${ status.count < fn:length(modulesContext.currentModule.breadcrumbList) }">
		<a href="${link.url}" title="${link.title}">${link.legend}</a>		
	</c:if>			
	<c:if test="${ status.count == fn:length(modulesContext.currentModule.breadcrumbList) }">
		<span>${link.legend}</span>
	</c:if>
	</c:forEach>		
			
</c:if>		


