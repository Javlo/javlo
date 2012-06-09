<div id="content-edit" class="full-height">
	<form id="form-content" class="components ${empty param.previewEdit?'ajax':''}" action="${info.currentURL}${not empty param.previewEdit?'?closeFrame=true':''}" enctype="multipart/form-data" method="post">
		<input type="hidden" name="webaction" value="edit.save" />
		<jsp:include page="content.jsp${not empty param.previewEdit?'?firstLine=true':''}" />
		<div class="insert-line">
			<input type="submit" class="action-button" value="${i18n.edit['global.save']}"/>
		</div>
	</form> 
</div>