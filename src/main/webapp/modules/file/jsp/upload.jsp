
<div class="popup upload">
<c:url var="backURL" value="${param.currentURL}">
	<c:param name="${BACK_PARAM_NAME}" value="${param[BACK_PARAM_NAME]}" />
</c:url>
<form action="${backURL}" method="post" enctype="multipart/form-data">

	<fieldset style="margin: 10px;">
		<legend>update from url</legend>
		<input type="text" name="url" placeholder="url" />
		<input type="text" name="filename" placeholder="file name" />
	</fieldset>

	<input type="text" name="folder" placeholder="${i18n.edit["file.new-folder"]}" />
	<input type="hidden" name="webaction" value="upload" />
	<input type="file" name="file" multiple="" />
	<input type="submit" />

</form>
</div>
