<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%>
<p>#image=${fn:length(imagesTransforming)}</p>
<table>
	<tr>
		<th>name</th>
		<th>size</th>
		<th>c. time</th>
		<th>context</th>
		<th>path</th>
	</tr>
	<c:forEach var="image" items="${imagesTransforming}">
		<tr>
		<td>${image.value.name}</td>
		<td>${image.value.fileSizeText}</td>
		<td>${image.value.transformTimeText}</td>
		<td>${image.value.context}</td>
		<td>${image.value.path}</td>
		</tr>
	</c:forEach>
</table>