<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${not empty currentModule.breadcrumbTitle}">
	<span class="title">${currentModule.breadcrumbTitle}
	
	
	</span>
</c:if>
<c:if test="${empty currentModule.breadcrumbList}">
	<c:forEach var="page" items="${info.pagePath}">
		<c:set var="link" value='<a href="${page.url}">' />
		${empty param.previewEdit?link:'<span class="title">'}${page.info.title}${empty param.previewEdit?'</a>':'</span>'}		
		<c:if test="${fn:length(page.children) > 1}">
			<div class="children">
			<div class="container">
				<ul>
				<c:forEach var="child" items="${page.children}">
					<li class="${child.selected?'selected':''} ${child.realContent?'real-content':''}"><a href="${child.url}">${child.info.title}</a></li>
				</c:forEach>
				</ul>
			</div>
			</div>
		</c:if>		
	</c:forEach>
	<a class="selected" href="${info.currentURL}">${info.pageTitle}</a>
		<c:if test="${fn:length(info.page.children) > 0}">
			<div class="children">
			<div class="container">
				<ul>
				<c:forEach var="child" items="${info.page.children}">
					<li ${child.selected?'class="selected"':''}><a href="${child.url}">${child.info.title}</a></li>
				</c:forEach>
				</ul>
			</div>
			</div>
		</c:if>
</c:if>
<c:if test="${not empty currentModule.breadcrumbList}">
	<c:forEach var="link" items="${currentModule.breadcrumbList}" varStatus="status">	
		<a ${link.selected?'class="selected"':''} href="${link.url}" title="${link.title}">${link.legend}</a>
		<c:if test="${fn:length(link.children) > 1 || (status.last && fn:length(link.children) > 0)}">
			<div class="children">
			<div class="container">
				<ul>
				<c:forEach var="child" items="${link.children}">
					<li><a href="${child.url}" title="${child.title}">${child.legend}</a></li>
				</c:forEach>
				</ul>
			</div>
			</div>
		</c:if>				
	</c:forEach>		
			
</c:if>		


