<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><form method="post" action="${info.currentURL}?webaction=macro.executeInteractiveMacro&mode=3&macro=create-redirections&macro-create-redirections=create-redirections&previewEdit=true&module=macro" class="standard-form">
<fieldset>
<legend>Create redirections</legend>
<input type="hidden" name="webaction" value="macro-create-redirections.create" />
<input type="hidden" name="module" value="content" />
<input type="hidden" name="page" value="${info.pageName}" />
<textarea name="redirections" rows="20">${redirections}</textarea>
<div class="action">
	<input class="btn btn-primary pull-right" type="submit" value="create" />
</div>
</fieldset>
</form>