<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div id="content" class="content nopadding">
<table class="sTable3" width="100%" cellspacing="0" cellpadding="0">
<thead><tr><td>name</td><td>#categories</td><td>#content</td><td>link</td><td>&nbsp;</td></tr></thead>
<tbody>
<c:forEach var="provider" items="${providers}">
	<tr>
		<td>${provider.name}</td>
		<td>${fn:length(provider.categories)}</td>
		<td>${fn:length(provider.content)}</td>
		<td><a href="${provider.URL}">${provider.URL}</a></td>
		<td><a class="action-button" href="${info.currentURL}?webaction=shared-content.refresh&provider=${provider.name}">refresh</a></td>
	</tr>
</c:forEach>
</tbody>
</table>

</div>