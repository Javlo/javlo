<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<div class="well drop-files">
	<c:url var="uploadUrl" value="${info.uploadURL}" context="/">
		<c:param name="provider" value="${provider.name}" />
	</c:url>
	<div class="upload-zone" data-url="${uploadUrl}" data-done="onDoneUpload">
	<h3>${i18n.edit['preview.upload-here']}</h3>
	<div class="picto"><i class="bi bi-box-arrow-in-down"></i></div>	
	</div>	
</div>

<script>
	function onDoneUpload() {
		editPreview.ajaxPreviewRequest('${refreshUrl}', null, null, null);
	}
</script>

<!-- <div id="paste" class="well"> -->
<%-- <h3>${i18n.edit['preview.paste-here']}</h3> --%>
<!-- <div class="picto"><span class="glyphicon glyphicon-paste" aria-hidden="true"></span></div> -->
<!-- </div> -->
<!-- <script type="text/javascript"> 
//   console.log(window.Clipboard);
//   var items = window.clipboardData.items;	  
//   var blob = null;
//   for (var i = 0; i < items.length; i++) {
//     if (items[i].type.indexOf("image/png") === 0) {
//       pjq('#paste').removeClass('hidden');
//     }
//   }	
</script> -->

<c:if test="${not empty provider && provider.search}">
<form id="shared-content-search-form" class="ajax" action="${info.currentURL}" method="post">
<div>
	<input type="hidden" name="webaction" value="shared-content.search" />
	<input type="hidden" name="provider" value="${provider.name}" />
	<div class="flex-line">
		<input type="text" id="shared-content-search-form-query" name="query" placeholder="${i18n.edit['content.search']}" class="form-control query" value="${sharedContentContext.searchQuery}"/>
		<input type="submit" class="btn btn-default" value="${i18n.edit['global.ok']}" />
		<input type="submit" name="reset" class="btn btn-default pull-right" value="${i18n.edit['global.reset']}" />
	</div>
</div>
</form>
</c:if>
<!-- input class="filter" type="text" placeholder="${i18n.edit['global.filter']}" onkeyup="filter(this.value, '#preview_command .shared-content .content-wrapper');" / -->
<form id="shared-content-form" class="js-submit" action="${info.currentURL}" method="post">
	<div>
		<input type="hidden" name="webaction" value="shared-content.choose" />
		<div class="form-group form-inline">
		<select name="provider" class="form-control ${!info.admin && empty provider.URL?'max-width':''}  ${empty provider.URL?'':'min-width'}">
			<c:forEach var="provider" items="${sharedContentProviders}">
				<c:set var="key" value="shared.${provider.name}" />
				<c:set var="providerLabel" value="${i18n.edit[key]}" /><c:if test="${providerLabel == key}"><c:set var="providerLabel" value="${provider.name}" /></c:if>
				<option value="${provider.name}" ${sharedContentContext.provider eq provider.name?'selected="selected"':''}>${providerLabel}</option>
			</c:forEach>
		</select>
		<c:url var="url" value="${info.currentEditURL}" context="/">
			<c:param name="module" value="shared-content" />			
			<c:param name="mode" value="3" />
			<c:param name="previewEdit" value="true" />
		</c:url>		
		<c:if test="${not empty provider.URL}">
		<a class="btn btn-default" title="link" lang="en" href="${provider.URL}" target="_blank"><span class="glyphicon glyphicon-circle-arrow-right" aria-hidden="true"></span></a>
		</c:if>
		<c:if test="${info.admin}">
		<button class="btn btn-default pull-right" title="add" lang="en" onclick="editPreview.openModal('${i18n.edit['global.shared-config']}', '${url}'); return false;"><i class="bi bi-plus-lg"></i></button>
		</c:if>
		</div>		
		<c:if test="${fn:length(sharedContentCategories)>1}">
		<div class="form-group">
		<select name="category" class="form-control">			
			<c:forEach var="category" items="${sharedContentCategories}">
				<option ${currentCategory eq category.key?'selected="selected"':''} value="${category.key}">${empty category.value?'&nbsp;':category.value}</option>
			</c:forEach>
		</select>		
		</div>
		</c:if>
		<input type="submit" />
	</div>
</form>
<c:if test="${not empty provider}">
<div id="shared-content-result" class="content shared-content ${provider.type} ${provider.uploadable?'upload-zone':'no-upload'}" data-url="${info.uploadSharedURL}">
<jsp:include page="shared_content_result.jsp"></jsp:include>
</div>
</c:if>
