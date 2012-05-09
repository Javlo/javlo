<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
<div class="template-preview">
	<img src="${currentTemplate.previewUrl}" alt="${currentTemplate.template.name}" />
</div>

<form id="form-edit-template" action="${info.currentURL}" class="standard-form" method="post">
	
	<div>
		<input type="hidden" name="webaction" value="editTemplate" />
		<input type="hidden" name="name" value="${currentTemplate.template.name}" />
	</div>
	
	<div class="line">
		<label for="author">${i18n.edit['global.author']}</label>
		<input type="text" id="author" name="author" value="${currentTemplate.template.authors}" />
	</div>
	
	<div class="line">
		<label for="date">${i18n.edit['template.creation-date']}</label>
		<input type="text" id="date" class="datepicker" name="date" value="${currentTemplate.creationDate}" />
	</div>
	
	<div class="action">
		<input type="submit" name="back" value="${i18n.edit['global.back']}" />
		<input type="submit" value="${i18n.edit['global.ok']}" />
	</div>
</form>
</div>

