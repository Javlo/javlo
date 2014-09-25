<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<c:if test="${fn:length(files) > 0}">
<table class="table">
<thead>
	<tr>
		<th class="name">${i18n.view["files.header.name"]}</th>
		<th class="type">${i18n.view["files.header.type"]}</th>
		<th class="size">${i18n.view["files.header.size"]}</th>		
	</tr>
</thead>
<c:forEach var="file" items="${files}">
<tr>
		<td class="name"><a href="${file.URL}">${file.name}</a></td>
		<td class="type">${file.type}</td>
		<td class="size">${file.size}</td>			
</tr>
</c:forEach>
</table>
</c:if>