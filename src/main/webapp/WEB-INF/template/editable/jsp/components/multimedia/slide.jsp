<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div class="multimedia slides">
	<c:forEach var="resource" items="${resources}" varStatus="status">
	<img alt="${resource.title}" src="${fn:replace(resource.previewURL,'/preview/','/slide/')}" />	
	</c:forEach>
</div>
	