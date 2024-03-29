<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%>
<div id="transformation" class="content">
<c:if test="${fn:length(imagesTransforming) > 0}">
<table class="sTable">
	<tr>
		<th>name</th>
		<th>size</th>
		<th>time</th>
		<th>site</th>		
	</tr>
	<c:forEach var="image" items="${imagesTransforming}">
		<tr>
		<td>${image.value.name}</td>
		<td>${image.value.fileSizeText}</td>
		<td>${image.value.transformTimeText}</td>
		<td>${image.value.context}</td>		
		</tr>
	</c:forEach>
</table>
</c:if>
<c:if test="${fn:length(imagesTransforming) == 0}">
	no transformation.
</c:if>
</div>
