<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<input type="hidden" name="webaction" value="macro-create-children.create" />
<legend>Create children</legend>
<div class="form-group"><textarea class="form-control" name="children"></textarea></div>
<button class="btn btn-default pull-right" type="submit" >${i18n.edit['global.create']}</button>
</fieldset>
</form>

<fieldset>
<legend>Export current children</legend>
<textarea class="form-control" name="export">${exportList}</textarea>
</fieldset>