<div id="content-edit" class="auto-height">
	<form id="form-content" class="components" action="${info.currentURL}" enctype="multipart/form-data" method="post">
		<input type="hidden" name="webaction" value="save" />
		<jsp:include page="content.jsp" />
		<div class="insert-line">
			<input type="submit" class="action-button" value="${i18n.edit['global.save']}"/>
		</div>
	</form>
</div>