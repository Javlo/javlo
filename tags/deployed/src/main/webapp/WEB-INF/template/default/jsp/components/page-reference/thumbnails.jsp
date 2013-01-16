<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!-- portfolio list -->
<ul class="portfolio">
	<c:forEach items="${pages}" var="page" varStatus="status">
	<c:set var="image" value="${page.images[0]}" />
	<!-- portfolio item -->
		<li>			
			<figure>
				<a title="${page.title}" href="${page.url}">
					<img src="${image.url}" alt="${image.description}" />
				</a>
				<figcaption>
					<a title="${page.title}" href="${page.url}"><h4>${page.title}</h4></a>
					<p>${page.description}</p>
				</figcaption>
			</figure>			
		</li>
		<!-- / portfolio item -->	
	</c:forEach>
</ul>
