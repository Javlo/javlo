
<div class="popup upload">
<c:url var="backURL" value="${param.currentURL}">
	<c:param name="${BACK_PARAM_NAME}" value="${param[BACK_PARAM_NAME]}" />
</c:url>
<form action="${backURL}" method="post" enctype="multipart/form-data">
	<input type="text" name="folder" placeholder="${i18n.edit["file.new-folder"]}" />
	<input type="hidden" name="webaction" value="upload" />	
	<input type="text" name="url" placeholder="url" />
	<input type="file" name="file" multiple="" />
	<input type="submit" />
</form>
</div>
