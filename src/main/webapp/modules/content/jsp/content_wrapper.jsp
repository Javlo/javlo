<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="content-edit" class="full-height ${not empty lightInterface?'light':''}">
<div class="area-label">${info.area}</div>
<c:set var="previewParams" value="?webaction=editPreview&_closeFrame=true&previewEdit=true&comp_id=${param.comp_id}" />
	<form id="form-content" class="components ${empty param.previewEdit?'ajax':''}" action="${info.currentURL}${not empty param.previewEdit?'?previewEdit=true':''}" enctype="multipart/form-data" method="post">
		<input type="hidden" name="webaction" value="edit.save" />
		<jsp:include page="content.jsp${not empty param.previewEdit?'?firstLine=true':''}" />
		<div class="insert-line">
			<c:set var="saveItem" value="${i18n.edit['global.save']}" />
			<c:if test="${not empty param.previewEdit}">
				<c:set var="saveItem" value="${i18n.edit['preview.save']}" />
			</c:if>
			<input id="button-content-submit" type="submit" class="action-button" value="${saveItem}"  name="save"/>
		</div>
	</form>
	<c:if test="${not empty cleanClipBoard && not empty editPreview}">
		<form id="form-clear-clipboard" action="${info.currentURL}" method="post">
		<input type="hidden" name="webaction" value="clearClipboard" />
		<div class="insert-line">
			<input id="button-content-submit" type="submit" class="action-button" value="${i18n.edit['action.clean-clipBoard']}" />
		</div>
		</form>
	</c:if>
	 
</div>