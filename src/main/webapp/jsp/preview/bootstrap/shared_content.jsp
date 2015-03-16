<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="well drop-files">
	<h3>${i18n.edit['preview.upload-here']}</h3>	
	<div class="upload-zone" data-url="${info.uploadURL}"><span class="glyphicon glyphicon-download-alt" aria-hidden="true"></span></div>
</div><c:if test="${not empty provider && provider.search}">
<form id="shared-content-search-form" class="ajax" action="${info.currentURL}" method="post">
<div>
	<input type="hidden" name="webaction" value="shared-content.search" />
	<input type="hidden" name="provider" value="${provider.name}" />
	<div class="form-inline form-group">
		<input type="text" name="query" placeholder="${i18n.edit['content.search']}" class="form-control" />		
		<input type="submit" class="btn btn-default" value="${i18n.edit['global.ok']}" />
	</div>
</div>
</form>
</c:if>
<!-- input class="filter" type="text" placeholder="${i18n.edit['global.filter']}" onkeyup="filter(this.value, '#preview_command .shared-content .content-wrapper');" / -->
<form id="shared-content-form" class="js-submit" action="${info.currentURL}" method="post">
	<div>
		<input type="hidden" name="webaction" value="shared-content.choose" />
		<div class="form-group">		
		<select name="provider" class="form-control">
			<option class="placeholder" value="">${i18n.edit["preview.choose-provider"]}</option>
			<c:forEach var="provider" items="${sharedContentProviders}">
				<c:set var="key" value="shared.${provider.name}" />
				<option value="${provider.name}" ${sharedContentContext.provider eq provider.name?'selected="selected"':''}>${i18n.edit[key]}</option>
			</c:forEach>
		</select>
		</div>		
		<c:if test="${fn:length(sharedContentCategories)>1}">
		<div class="form-group">
		<select name="category" class="form-control">
			<c:forEach var="category" items="${sharedContentCategories}">
				<option ${currentCategory eq category.key?'selected="selected"':''} value="${category.key}">${category.value}</option>
			</c:forEach>
		</select>		
		</div>
		</c:if>
		<input type="submit" />
	</div>
</form>
<c:if test="${not empty provider}">
<div id="shared-content-result" class="content shared-content ${provider.type} ajax-loader">
<jsp:include page="shared_content_result.jsp"></jsp:include>
</div>
</c:if>
