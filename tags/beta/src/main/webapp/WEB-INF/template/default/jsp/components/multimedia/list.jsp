<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!-- portfolio -->
<section class="portfolio secondary container_12 clearfix">
	<!-- portfolio list -->
	<ul id="portfolio" class="v3">
	<c:forEach var="resource" items="${resources}" varStatus="status">	
		<!-- portfolio item -->
		<li class="web">
			<figure class="grid_3">
				<a rel="${resource.relation}" class="box" title="${resource.title}" href="${resource.URL}"><img alt="${resource.title}" src="${fn:replace(resource.previewURL,'/preview/','/little-list/')}" /></a>
				
			</figure>
		</li>
		<!-- portfolio item -->
	</c:forEach>	
</ul>
</section>
