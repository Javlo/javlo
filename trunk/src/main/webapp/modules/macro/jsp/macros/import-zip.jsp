<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="post" action="${info.currentURL}" class="standard-form js-change-submit">
<fieldset>
<legend>Import page</legend>
<input type="hidden" name="webaction" value="macro-import-zip.import" />
<c:forEach var="page" items="${pages}">
<input class="action-button" type="submit" name="file" value="${page.name}" />
</c:forEach>

</fieldset>
</form>


