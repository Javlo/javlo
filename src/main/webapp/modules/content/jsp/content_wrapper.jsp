<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="area-label">${info.area}</div>
<div id="content-edit" class="full-height ${not empty userInterface.light?'light':''}">

<c:url var="formURL" value="${info.currentURL}" context="/"><c:if test="${not empty requestService.parameterMap.previewEdit}"><c:param name="previewEdit" value="true" /></c:if></c:url>

<form role="form" id="form-content" class="components ${empty requestService.parameterMap.previewEdit?'ajax':''}" action="${formURL}" enctype="multipart/form-data" method="post">
	<input type="hidden" name="webaction" value="edit.save" />
	<input type="hidden" name="render-mode" value="1" /><c:if test="${requestService.parameterMap.lightEdit}"><input type="hidden" name="lightEdit" value="true" /></c:if>
	
	<jsp:include page="content.jsp${not empty requestService.parameterMap.	previewEdit?'?firstLine=true':''}" />
	
	<div class="insert-line action">
		<c:set var="saveItem" value="${i18n.edit['global.save']}" />
		<c:if test="${not empty param.previewEdit}">
			<c:set var="saveItem" value="${i18n.edit['preview.save']}" />
		</c:if>
		<input id="button-content-submit" type="submit" class="btn btn-default btn-xs" value="${saveItem}"  name="save"/>
	</div>
</form>
<c:if test="${not empty cleanClipBoard && not info.editPreview}">
	<form id="form-clear-clipboard" action="${info.currentURL}" method="post">
	<input type="hidden" name="webaction" value="clearClipboard" />
	<div class="insert-line action">
		<input id="button-content-submit" type="submit" class="btn btn-default btn-xs" value="${i18n.edit['action.clean-clipBoard']}" />
	</div>
	</form>
</c:if>
	 
</div>