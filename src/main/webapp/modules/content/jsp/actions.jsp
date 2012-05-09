<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${not empty param.languages}">
<div class="special">
<form id="form-languages" action="${info.currentURL}" method="post">
<div class="select-languages form_default">
	<input type="hidden" name="webaction" value="changeLanguage" />
	<select name="language">
	<c:forEach var="lang" items="${info.contentLanguages}">
		<option value="${lang}"${lang eq info.contentLanguage?' selected="selected"':''}>${lang}</option>
	</c:forEach>
	</select>
	<input type="submit" name="ok" value="${i18n.edit['global.ok']}" />
</div>
</form>
</div>
</c:if>

<c:if test="${not empty param.button_edit}"><a class="action-button more edit" href="${info.currentURL}?webaction=changeMode&mode=1"><span>${i18n.edit['action.edit-content']}</span></a></c:if>
<c:if test="${not empty param.button_preview}"><a class="action-button more preview" href="${info.currentURL}?webaction=changeMode&mode=2"><span>${i18n.edit['command.preview']}</span></a></c:if>
<c:if test="${not empty param.button_page}"><a class="action-button more page" href="${info.currentURL}?webaction=changeMode&mode=3"><span>${i18n.edit['item.title']}</span></a></c:if>

<c:if test="${not empty param.button_publish}"><a class="action-button publish" href="#publish"><span>${i18n.edit['command.publish']}</span></a></c:if>
<c:if test="${not empty param.button_save}"><a class="action-button save" href="#save" onclick="document.getElementById('form-content').submit(); return false;"><span>${i18n.edit['action.update']}</span></a></c:if>

