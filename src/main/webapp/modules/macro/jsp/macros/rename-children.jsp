<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<legend>Create article</legend>
<input type="hidden" name="webaction" value="rename-children.rename" />
<input type="hidden" name="module" value="content" />

<div class="line">
	<label for="text">part of text to modify</label>
	<input type="text" id="text" name="text" />
</div>

<div class="line">
	<label for="new">new text</label>
	<input type="text" id="new" name="new" />
</div>

<div class="action">
	<input class="btn btn-primary pull-right" type="submit" value="rename" />
</div>

</fieldset>
</form>


