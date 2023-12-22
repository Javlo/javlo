<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<a class="action-button delete" onclick="document.getElementById('form-select-user').submit(); return false;" href="#"><span>${i18n.edit['edit-users.action.delete']}</span></a>
<c:if test="${not empty CSVLink}">
	<div class="link"><a href="${CSVLink}">${CSVName}</a><c:if test="${not empty ExcelLink}"> - <a href="${ExcelLink}">[excel]</a></c:if></div>
</c:if>
<c:url value="${info.currentModuleURL}/jsp/upload.jsp" var="uploadURL" context="/">
	<c:param name="admin" value="${admin}" />
	<c:param name="currentURL" value="${info.currentURL}" />
	<c:if test="${not empty userContext.currentRole}">
		<c:param name="role" value="${userContext.currentRole}" />
	</c:if>
</c:url>
<div class="link"><a class="popup" href="${uploadURL}" title="${i18n.edit['edit.action.upload']}"><span>${i18n.edit['edit.action.upload']}</span></a></div>

<c:if test="${fn:length(roles)>0}">
<form class="form_default js-change-submit" id="form-select-role" action="${info.currentURL}" method="post">

<input type="hidden" name="webaction" value="selectRole" />
<input type="hidden" name="admin" value="${admin}" />
<label for="select-roles">${i18n.edit['user.select-role']}</label>
<select id="select-roles" name="role">
<option value="">&nbsp;</option>
<c:forEach var="role" items="${roles}">
<option${userContext.currentRole == role?' selected="selected"':''}>${role}</option>
</c:forEach>
</select>
<button type="submit" class="action-button add-user"><span>${i18n.edit['global.ok']}</span></button>
<button type="submit" style="display: inline;" class="action-button needconfirm" name="add" title="${i18n.edit['user.add-role']}"><span><i class="bi bi-plus"></i></span></button>
<button type="submit" style="display: inline;" class="action-button needconfirm" name="remove" title="${i18n.edit['user.remove-role']}"><span><i class="bi bi-dash"></i></span></button>

</form>
</c:if>
<form class="form_default" id="form-add-user" action="${info.currentURL}" method="post">
<input type="hidden" name="webaction" value="createUser" />
<input class="label-inside label" type="text" name="user" value="${i18n.edit['user.create-new-user']}..." />
<button type="submit" class="action-button add-user">${i18n.edit['global.ok']}</button>
</form>