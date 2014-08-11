
<div class="popup upload">
<form action="${param.currentURL}" method="post" enctype="multipart/form-data">
	<input type="text" name="folder" placeholder="${i18n.edit["file.new-folder"]}" />
	<input type="hidden" name="webaction" value="upload" />	
	<input type="text" name="url" placeholder="url" />
	<input type="file" name="file" multiple="" />
	<input type="submit" />
</form>
</div>
