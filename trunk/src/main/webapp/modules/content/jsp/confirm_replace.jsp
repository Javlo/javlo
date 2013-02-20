<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="widgetbox page-properties">
<h3><span>${i18n.edit['confirm.title']} : ${resourceStatus.target.name}</span></h3>
<div class="content">
<form class="standard-form confirm-replace" method="post" action="${info.currentURL}" >
<p>${i18n.edit['edit.message.confirm']}</p>
<div>
<input type="hidden" name="webaction" value="edit.confirmReplace" />
<input type="hidden" name="source" value="${resourceStatus.source.id}" />
<input type="hidden" name="target" value="${resourceStatus.target.id}" />

<div class="file-list">
<div class="one_half">
<fieldset>
	<legend>${i18n.edit['confirm.local']} (${targetDate})</legend>
	${previewTargetCode}
	<input type="submit" name="cancel" value="${i18n.edit['confirm.keep']}" />
</fieldset>
</div>

<div class="one_half last">
<fieldset>
	<legend>${i18n.edit['confirm.remote']} (${sourceDate})</legend>
	${previewSourceCode}
	<input type="submit" name="confirm" value="${i18n.edit['confirm.replace']}" />
</fieldset>
</div>
</div>

</div>
</form>
</div>
</div>