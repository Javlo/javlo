<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><c:if
	test="${fn:length(pages)>0}">
	
	<c:if test="${not empty firstPage}">
		<div class="first-page-complete">${firstPage}</div>
	</c:if>
	
	<c:if test="${not empty title}">
		<h2>${title}</h2>
	</c:if>
	<ul class="products">
		<c:forEach items="${pages}" var="page" varStatus="status">
			<c:set var="image" value="${null}" />
			<c:if test="${fn:length(page.images)>0}">
				<c:set var="image" value="${page.images[0]}" />
			</c:if>
			<!-- portfolio item -->
			<li><c:if test="${not empty image}">
					<figure>
						<a title="${page.title}" href="${page.url}"> <img
							src="${image.url}" class="frame" alt="${image.description}" />
						</a>
					</figure>
				</c:if>
				<div class="body">
					<span class="date">${page.date}</span> <a title="${page.title}"
						href="${page.url}">${page.title}</a>
					<c:if test="${not empty page.description}">
						<p class="description">${page.description}</p>
					</c:if>
				</div></li>
			<!-- / portfolio item -->

		</c:forEach>
	</ul>
</c:if>