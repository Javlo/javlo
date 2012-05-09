<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="box preview">
<h3><span>${i18n.edit['command.preview']} <a target="_blank" class="preview-link" href="${previewURL}" title="${i18n.edit['preview.popup']}">${i18n.edit['preview.popup']}</a></span></h3>
<div id="preview" class="content auto-height">
<iframe src="${previewURL}"></iframe>
</div>
</div>
