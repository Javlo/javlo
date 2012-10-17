<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
	
	<c:if test="${not empty title}">
		<h2>${title}</h2>
	</c:if>
	
<div class="carousel">
    <ul class="slides">	
	<c:forEach items="${pages}" var="page" varStatus="status">
	<c:set var="image" value="${null}" />
	<c:if test="${fn:length(page.images)>0}">
		<c:set var="image" value="${page.images[0]}" />
	</c:if>
	<c:set var="url" value="${page.url}" />
	<c:set var="target" value="" />
	<c:if test="${!page.realContent && not empty page.linkOn}">
		<c:set var="url" value="${page.linkOn}" />
		<c:if test="${info.openExternalLinkAsPopup}">
			<c:set var="target" value=" target=\"_blank\"" />
		</c:if>
	</c:if>
	
	<c:if test="${not empty image}">
	<li class="slide">
		<a href="${url}" title="${page.title}"${target}>
		<figure><img src="${image.url}" alt="${image.description}"><figcaption>${page.title}</figcaption></figure>
		</a>		
	</li>
	</c:if>
	</c:forEach>
	</ul>
</div>