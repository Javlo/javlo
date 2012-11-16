<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="content-edit" class="full-height ${not empty lightInterface?'light':''}">
	<form id="form-content" class="components ${empty param.previewEdit?'ajax':''}" action="${info.currentURL}${not empty param.previewEdit?'?closeFrame=true':''}" enctype="multipart/form-data" method="post">
		<input type="hidden" name="webaction" value="edit.save" />
		<jsp:include page="content.jsp${not empty param.previewEdit?'?firstLine=true':''}" />
		<div class="insert-line">
			<input id="button-content-submit" type="submit" class="action-button" value="${i18n.edit['global.save']}"/>
		</div>
	</form>
	<c:if test="${not empty cleanClipBoard}">
		<form id="form-clear-clipboard" action="${info.currentURL}" method="post">
		<input type="hidden" name="webaction" value="clearClipboard" />
		<div class="insert-line">
			<input id="button-content-submit" type="submit" class="action-button" value="${i18n.edit['action.clean-clipBoard']}"/>
		</div>
		</form>
	</c:if>
	 
</div>