<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="colsClass" value="cols" />
<c:forEach var="content" items="${sharedContent}">
	<c:if test="${not empty content.description}">
		<c:set var="colsClass" value="no-cols" />
	</c:if>
</c:forEach>
<div class="provider-${provider.name} privider-type-${provider.type} height-to-bottom">
<div class="row">
<c:if test="${fn:length(sharedContent)==0}">
<div class="col-md-12"><div class="alert alert-info" role="alert">${i18n.edit['global.empty-list']}</div></div>
</c:if>
<c:forEach var="content" items="${sharedContent}" varStatus="status">
<div class="col-md-6 ${not empty content.description?'width-description':'without-description'}">
<div class="thumbnail shared-content-item" data-shared="${content.id}">
<c:if test="${not empty content.editURL}">
<c:if test="${!content.editAsModal}">
<a href="${content.editURL}" title="edit" lang="en">
</c:if><c:if test="${content.editAsModal}">
<a href="${content.editURL}" title="edit" lang="en" onclick="editPreview.openModal('${content.title}', '${content.editURL}'); return false;">
</c:if></c:if>	
	<c:if test="${not empty content.imageURL}">
		<figure><img id="img-${status.index}" src="${info.ajaxLoaderURL}"/></figure>
	</c:if>	
	<div class="caption">${content.title}</div>
<c:if test="${not empty content.editURL}">
</a>
</c:if>	
</div>
</div>
<script type="text/javascript">
pjq("#img-${status.index}").attr("src", "${content.imageURL}");
</script>

</c:forEach>
</div>
</div>
