<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<a class="action-button delete" onclick="document.getElementById('form-select-user').submit(); return false;" href="#"><span>${i18n.edit['edit-users.action.delete']}</span></a>
<c:if test="${not empty CSVLink}">
	<div class="link"><a href="${CSVLink}">${CSVName}</a><c:if test="${not empty ExcelLink}"> - <a href="${ExcelLink}">[excel]</a></c:if></div>
</c:if>
<c:url value="${info.absoluteURLPrefix}${currentModule.path}/jsp/upload.jsp" var="uploadURL">
	<c:param name="admin" value="${admin}" />
	<c:param name="currentURL" value="${info.currentURL}" />
	<c:if test="${not empty userContext.currentRole}">
		<c:param name="role" value="${userContext.currentRole}" />
	</c:if>
</c:url>
<div class="link"><a class="popup" href="${uploadURL}" title="${i18n.edit['edit.action.upload']}"><span>${i18n.edit['edit.action.upload']}</span></a></div>

<div class="special">
<c:if test="${fn:length(roles)>0}">
<form class="form_default js-change-submit" id="form-select-role" action="${info.currentURL}" method="post">
<div>
<input type="hidden" name="webaction" value="selectRole" />
<label for="select-roles">${i18n.edit['user.select-role']}</label>
<select id="select-roles" name="role">
<option value=""></option>
<c:forEach var="role" items="${roles}">
<option${userContext.currentRole == role?' selected="selected"':''}>${role}</option>
</c:forEach>
</select>
<input type="submit" class="action-button add-user" value="${i18n.edit['global.ok']}" />
</div>
</form>
</c:if>
<form class="form_default" id="form-add-user" action="${info.currentURL}" method="post">
<div>
<input type="hidden" name="webaction" value="createUser" />
<input class="label-inside label" type="text" name="user" value="${i18n.edit['user.create-new-user']}..." />
<input type="submit" class="action-button add-user" value="${i18n.edit['global.ok']}" />
</div>
</form>
</div>
<div class="clear">&nbsp;</div>

