
<div class="popup">
<form action="${param.currentURL}" method="post" enctype="multipart/form-data">
	<input type="hidden" name="webaction" value="upload" />	
	<input type="hidden" name="context" value="${param.context}" />
	<input type="file" name="file" />
	<input type="submit" />
</form>
</div>
