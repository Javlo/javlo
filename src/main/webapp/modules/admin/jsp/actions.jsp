<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<a class="action-button more edit-static-config" href="${info.currentURL}?webaction=EditStaticConfig"><span>${i18n.edit['admin.edit-static-config']}</span></a>
<div class="special">
	<form id="form-create-site" action="${info.currentURL}" method="post">
		<input type="hidden" name="webaction" value="createSite" />
		<input class="label-inside label" type="text" name="context" value="${i18n.edit['admin.button.createsite']}..." />
		<input type="submit" value="${i18n.edit['global.ok']}" />		
	</form>
</div>
<a class="action-button clear-cache" href="${info.currentURL}?webaction=clearcache"><span>${i18n.edit['admin.clear-cache']}</span></a>
<div class="clear">&nbsp;</div>

