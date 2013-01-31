<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="post" action="${info.currentURL}" class="standard-form js-change-submit">
<fieldset>
<legend>Import page</legend>
<input type="webaction" value="macro-import-zip.import" />
<c:forEarch var="page" items="${pages}">
<input type="submit" name="${page.name}" value="${page.label}" />
</c:forEarch>

</fieldset>
</form>


