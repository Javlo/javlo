<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${not empty param.languages and fn:length(info.contentLanguages) > 1}">
<div class="special">
<form id="form-languages" action="${info.currentURL}" method="post" class="js-submit">
<div class="select-languages form_default">
	<input type="hidden" name="webaction" value="changeLanguage" />
	<select name="language">
	<c:forEach var="lang" items="${info.contentLanguages}">
		<option value="${lang}"${lang eq info.contentLanguage?' selected="selected"':''}>${lang}</option>
	</c:forEach>
	</select>
	<input class="action-button" type="submit" name="ok" value="${i18n.edit['global.ok']}" />
</div>
</form>
</div>
</c:if>

<c:if test="${not empty param.areas and fn:length(areas) > 1}">
<div class="special">
<form id="form-area" action="${info.currentURL}" method="post" class="js-submit">
<div class="select-area form_default">
	<input type="hidden" name="webaction" value="changeArea" />
	<select name="area">
	<c:forEach var="area" items="${areas}">
		<option ${currentArea eq area?' selected="selected"':''}>${area}</option>
	</c:forEach>
	</select>
	<input class="action-button" type="submit" name="ok" value="${i18n.edit['global.ok']}" />
</div>
</form>
</div>
</c:if>

<c:if test="${not empty param.button_edit}"><a class="action-button more edit" href="${info.currentURL}?webaction=changeMode&mode=1"><span>${i18n.edit['action.edit-content']}</span></a></c:if>
<c:if test="${not empty param.button_preview}"><a class="action-button more preview" href="${info.currentURL}?webaction=changeMode&mode=2"><span>${i18n.edit['command.preview']}</span></a></c:if>
<c:if test="${not empty param.button_page}"><a class="action-button more page" href="${info.currentURL}?webaction=changeMode&mode=3"><span>${i18n.edit['item.title']}</span></a></c:if>



<c:if test="${not empty param.button_publish}"><a class="action-button publish" href="${info.currentURL}?webaction=publish"><span>${i18n.edit['command.publish']}</span></a></c:if>
<c:if test="${not empty param.button_save}"><a class="action-button save" href="#save" onclick="jQuery('#form-content').submit(); return false;"><span>${i18n.edit['action.update']}</span></a></c:if>

<c:if test="${not empty param.button_delete_page}"><a class="action-button delete-page" href="${info.currentURL}?webaction=deletePage&page=${info.pageID}"><span>${i18n.edit['edit.action.delete-page']}</span></a></c:if>

