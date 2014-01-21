<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div id="content" class="content nopadding">
<table class="sTable3" width="100%" cellspacing="0" cellpadding="0">
<thead><tr><td>name</td><td>#categories</td><td>#content</td><td>link</td><td>active</td><td>&nbsp;</td></tr></thead>
<tbody>
<c:forEach var="provider" items="${providers}">
	<tr>
		<td>${provider.name}</td>
		<td>${fn:length(provider.categories)}</td>
		<td>${fn:length(provider.content)}</td>
		<td><a href="${provider.URL}">${provider.URL}</a></td>
		<td><input type="checkbox" name="active-${provider.name}" ${provider.active?'cheched="checked":''}/></td>
		<td><a class="action-button" href="${info.currentURL}?webaction=shared-content.refresh&provider=${provider.name}">refresh</a></td>
	</tr>
</c:forEach>
</tbody>
</table>
</div>
<div class="content">

<form class="standard-form" action="${info.currentURL}" method="post">
	<fieldset>	
	<legend>Automatic gallery importation (URL list)</legend>
	<input type="hidden" name="webaction" value="shared-content.URLList" />
	<textarea name="url-list" name="" rows="20" cols="120">${urls}</textarea>
	<input type="submit" />
	</fieldset>
</form>

</div>
