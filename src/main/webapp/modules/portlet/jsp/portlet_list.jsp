<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<div class="content">
	<div class="one_half">
		<ul>
		<c:forEach var="portlet" items="${portlets}" varStatus="status">
			<li>${portlet.name}</li>
		</c:forEach>
		</ul>
	</div>
</div>