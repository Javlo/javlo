<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<fieldset>
<legend>Import page</legend>
<table class="dyntable">
<c:if test="${fn:length(pages)==0}">
<div class="notification msgalert">
<a class="close"></a>
<p>no pages found for import.</p>
</div>
</c:if>
<c:forEach var="page" items="${pages}">
<form method="post" action="${info.currentURL}" class="standard-form js-change-submit">
<tr>
<input type="hidden" name="webaction" value="macro-import-zip.import" />
<input type="hidden" name="name" value="${page.name}" />
<td>${page.label}</td>
<td class="action"><input class="action-button" type="submit" name="file" value="import" /></td>
<td class="action"><input class="action-button warning" type="submit" name="remove-file" value="remove" /></td>
</tr>
</form>
</c:forEach>
</table>
</fieldset>


