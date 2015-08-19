<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="${contentContext.editPreview?'preview ':'edit '}content wizard">
	<div class="form-group">
	<textarea class="form-control" rows="20" cols="20">${content}</textarea>
	</div>
	<form class="standard-form" action="${info.currentURL}" method="get">
		<div>
			<input type="hidden" name="webaction" value="wizard" />
			<input type="hidden" name="box" value="${box.name}" />
			<c:if test="${contentContext.editPreview}"><input type="hidden" name="previewEdit" value="true" /></c:if>
		</div>
		<div class="pull-right">
		<button class="btn btn-defaut btn-back" type="submit" name="wizardStep" value="1">back</button>
		</div>
		
		<a target="_blank" class="btn btn-defaut" href="${exportURL}">link to export.</a>
		<c:url var="downloadURL" value="${exportURL}" context="/">
			<c:param name="download" value="true" />
		</c:url>
		<a target="_blank" class="btn btn-primary btn-color" href="${downloadURL}">download export.</a>
		
	</form>
</div>
