<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><c:if
	test="${fn:length(pages)>0}">
	
	<c:if test="${not empty firstPage}">
		<div class="first-page-complete">${firstPage}</div>
	</c:if>
	
	<c:if test="${not empty title}">
		<h2>${title}</h2>
	</c:if>

	<ul class="products short">
		<c:forEach items="${pages}" var="page" varStatus="status">			
			<li class="item-${status.index+1}">
				<div class="body">
					<c:if test="${globalContext.collaborativeMode && not empty page.creator}">
						<div class="authors">${page.creator}</div>
					</c:if>
					<c:if test="${page.contentDate}"><span class="date">${page.date}</span></c:if> <a title="${page.title}"
						href="${page.url}">${page.title}</a>
				</div></li>
			<!-- / portfolio item -->
		</c:forEach>
	</ul>
</c:if>