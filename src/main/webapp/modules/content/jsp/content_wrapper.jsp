<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div class="area-label">${info.area}</div>
<div id="content-edit" class="full-height ${not empty userInterface.light?'light':''}">
<c:url var="formURL" value="${info.currentURL}" context="/"><c:if test="${not empty requestService.parameterMap.previewEdit}"><c:param name="previewEdit" value="true" /></c:if></c:url>
<form role="form" id="form-content" class="components ajax" action="${formURL}" enctype="multipart/form-data" method="post">
	<input type="hidden" name="webaction" value="edit.save" />
	<c:if test="${not empty requestService.parameterMap.forward_anchor}"><input type="hidden" name="forward_anchor" value="${requestService.parameterMap.forward_anchor}" /></c:if>
	<input type="hidden" name="render-mode" value="1" /><c:if test="${requestService.parameterMap.lightEdit}"><input type="hidden" name="lightEdit" value="true" /></c:if>
	
	<jsp:include page="content.jsp${not empty requestService.parameterMap.previewEdit?'?firstLine=true':''}" />
	
	<div class="insert-line action">
		<c:set var="saveItem" value="${i18n.edit['global.save']}" />
		<c:if test="${not empty param.previewEdit}">
			<c:set var="saveItem" value="${i18n.edit['preview.save']}" />
		</c:if>
		<button id="button-content-submit" type="submit" class="btn btn-default btn-xs" name="save" ><span class="text">${saveItem}</span>
		<span class="loader"><div class="_jv_spinner" role="status"><span class="sr-only" lang="en">Loading...</span></div></span>
		</button>
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
<%if (request.getParameter("pushcomp") != null) {%>
<script type="text/javascript">
	jQuery( window ).load(function() {
	scrollToFirstQuarter(jQuery('#content-edit'),jQuery('#comp-<%=request.getParameter("pushcomp")%> .component-title a'));
	});
</script>
<%}%>