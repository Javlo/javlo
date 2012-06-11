<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<a class="action-button delete" onclick="document.getElementById('form-select-user').submit(); return false;" href="#"><span>${i18n.edit['edit-users.action.delete']}</span></a>
<div class="special">
<form class="form_default" id="form-add-user" action="${info.currentURL}" method="post">
<div>
<input type="hidden" name="webaction" value="createUser" />
<input class="label-inside label" type="text" name="user" value="${i18n.edit['user.create-new-user']}..." />
<input type="submit" class="action-button add-user" value="${i18n.edit['global.ok']}" />
</div>
</form>
</div>
<div class="clear">&nbsp;</div>

