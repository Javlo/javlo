<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="post" action="${info.currentURL}" class="standard-form" enctype="multipart/form-data">
<fieldset>
<legend>Import content</legend>
<input type="hidden" name="webaction" value="macro-import-content.import" />
<input type="hidden" name="module" value="content" />

<div class="line">
	<label for="encoding">encoding</label>
	<input type="text" id="encoding" name="encoding" value="iso-8859-1" />
</div>

<div class="line">
	<label for="file">upload file</label>
	<input type="file" id="file" name="file" />
</div>

<div class="line">
	<label for="url">file url</label>
	<input type="text" id="url" name="url" />
</div>

<div class="action">
	<input type="submit" value="import" />
</div>

</fieldset>
</form>


