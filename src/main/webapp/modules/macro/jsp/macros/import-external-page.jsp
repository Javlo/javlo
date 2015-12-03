<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="post" action="${info.currentURL}">
<fieldset>
<legend>Import content</legend>
<input type="hidden" name="webaction" value="macro-import-external-page.import" />
<input type="hidden" name="module" value="content" />

<div class="form-group">
	<label>url:
	<input class="form-control" name="url" value="" />
	</label>
</div>
<div class="action">
	<input class="btn btn-default pull-right" type="submit" value="import" />
</div>

</fieldset>
</form>