<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<c:if test="${not empty threadId}">	
	<div id="wait-zone" class="wait">
	<h2>wait synchronization....</h2>
	<img src="${info.ajaxLoaderURL}" alt="wait synchronization...." />
	</div>
	
	<script>
		var threadRun = true;
		checkStatus = function() {
			jQuery.ajax({
				url : "${checkThreadURL}",
				cache : false,				
				type : "get",
				dataType : "json",
				processData: false,
				contentType: false
			}).done(function(jsonObj) {				
				if (jsonObj['running'] == "false") {
					jQuery("#wait-zone").remove();
					jQuery("#form-zone").removeClass("hidden");
					clearInterval(intervalId);
				}
			});
		}
		var intervalId = setInterval(checkStatus, 2000);
	</script>
	
</c:if>

<div id="form-zone" class="${contentContext.editPreview?'preview ':'edit '}content wizard${not empty threadId?' hidden':''}">
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
		
		<a target="_blank" class="btn btn-defaut" href="${exportURL}">Export HTML</a>
		<c:url var="downloadURL" value="${exportURL}" context="/">
			<c:param name="download" value="true" />
		</c:url>
		<a target="_blank" class="btn btn-primary btn-color" href="${downloadURL}">Save as HTML</a>
		
	</form>
</div>
