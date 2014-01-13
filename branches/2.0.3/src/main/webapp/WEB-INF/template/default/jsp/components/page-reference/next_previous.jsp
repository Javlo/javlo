<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${fn:length(pages)>0}">
<c:if test="${not empty title}"><h2>${title}</h2></c:if>
<ul class="next-previous">
	<c:set var="previousPage" />
	<c:set var="pageFound" value="false" />	
	<c:forEach items="${pages}" var="page" varStatus="status">			
	<c:if test="${pageFound}">
		<c:set var="pageFound" value="false" />
		<c:if test="${fn:length(page.images)>0}">
			<c:set var="image" value="${page.images[0]}" />
			</c:if>
			<li class="next">
			<a href="${page.url}">${page.title}</a>
			<div class="info">
				<figure>
				<a title="${page.title}" href="${page.url}">
					<img src="${image.url}" class="frame" alt="${image.description}" />
				</a>
				</figure>
				<p class="description">${page.description}</p>
			</div>
			</li>
	</c:if>
	<c:if test="${page.id eq info.page.id}">
		<c:set var="pageFound" value="true" />
		<c:if test="${not empty previousPage}">
			<c:if test="${fn:length(previousPage.images)>0}">
			<c:set var="image" value="${previousPage.images[0]}" />
			</c:if>
			<li class="previous">
			<a href="${previousPage.url}">${previousPage.title}</a>
			<div class="info">
				<figure>
				<a title="${previousPage.title}" href="${previousPage.url}">
					<img src="${image.url}" class="frame" alt="${image.description}" />
				</a>
				</figure>
				<p class="description">${previousPage.description}</p>
			</div>
			</li>
		</c:if>
	</c:if>
	<c:set var="previousPage" value="${page}" />
	</c:forEach>
</ul>
</c:if>