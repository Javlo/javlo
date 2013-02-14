<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${(not userInterface.componentsList || not empty param.previewEdit) && empty param.noinsert}">
<c:set var="componentsList" value="true" />
<div class="special last"> <!-- components -->
<form id="form-component-list" action="${info.currentURL}" method="post" class="js-submit ajax">
<input type="hidden" name="webaction" value="changeComponent" />
<c:if test="${not empty param.previewEdit}"> 
<input type="hidden" name="previewEdit" value="true" />
<input type="hidden" name="comp_id" value="${param.comp_id}" />
</c:if>
<select name="type" class="with-title">
<c:forEach var="comp" items="${components}">
<c:if test="${comp.metaTitle}">${closeAccordion}
<option disabled="disabled" class="title">${i18n.edit[comp.value]}</option>
</c:if>
<c:if test="${not comp.metaTitle}">
<option ${comp.selected?' selected="selected"':''} value="${comp.type}">${comp.label}</option>
</c:if>
</c:forEach>
</select>
</form>
<c:if test="${empty param.previewEdit}">
<a class="close" href="${info.currentURL}?webaction=displayComponentsList">x</a>
</c:if>
</div>
</c:if>

<c:if test="${not empty param.languages and fn:length(info.contentLanguages) > 1 and empty param.previewEdit}">
<div class="special${empty componentsList?' last':''}">
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

<c:if test="${not empty param.areas and fn:length(areas) > 1 and empty param.previewEdit}">
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

<c:if test="${not empty param.button_edit and empty param.previewEdit}"><a class="action-button more edit" href="${info.currentURL}?webaction=changeMode&mode=1"><span>${i18n.edit['action.edit-content']}</span></a></c:if>
<%--<c:if test="${not empty param.button_preview and empty param.previewEdit}"><a class="action-button more preview" href="${info.currentURL}?webaction=changeMode&mode=2"><span>${i18n.edit['command.preview']}</span></a></c:if>--%>

<c:if test="${not empty param.button_page and empty param.previewEdit}"><a class="action-button more page" href="${info.currentURL}?webaction=changeMode&mode=3"><span>${i18n.edit['item.title']}</span></a></c:if>
<c:if test="${not empty param.button_copy}"><a class="action-button copy ajax more" href="${info.currentURL}?webaction=copyPage${not empty param.previewEdit?'&webaction=editPreview&PreviewEdit=true':''}"><span>${i18n.edit['action.copy-page']}</span></a></c:if>

<c:if test="${not empty param.button_publish and empty param.previewEdit}"><a class="action-button publish ajax" href="${info.currentURL}?webaction=publish"><span>${i18n.edit['command.publish']}</span></a></c:if>
<c:if test="${not empty param.button_preview and empty param.previewEdit}"><a class="action-button preview" href="${previewURL}"><span>${i18n.edit['command.preview']}</span></a></c:if>
<c:if test="${not empty param.button_save}"><a class="action-button save" href="#save" onclick="jQuery('#button-content-submit').click(); return false;"><span>${i18n.edit['action.update']}</span></a></c:if>

<c:if test="${not empty param.button_delete_page and empty param.previewEdit}"><a class="action-button delete-page needconfirm" href="${info.currentURL}?webaction=deletePage&page=${info.pageID}"><span>${i18n.edit['edit.action.delete-page']}</span></a></c:if>

<div class="clear">&nbsp;</div>
