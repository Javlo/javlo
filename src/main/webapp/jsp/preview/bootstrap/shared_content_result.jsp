<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%>
<c:set var="colsClass" value="cols" />
<c:forEach var="content" items="${sharedContent}">
	<c:if test="${not empty content.description}">
		<c:set var="colsClass" value="no-cols" />
	</c:if>
</c:forEach>
<div class="provider-${provider.name} provider-type-${provider.type} height-to-bottom">
<div class="row">
<c:if test="${fn:length(sharedContent)==0}">
<div class="col-md-12"><div class="alert alert-info" role="alert">${i18n.edit['global.empty-list']}</div></div>
</c:if>
<c:forEach var="content" items="${sharedContent}" varStatus="status">
<div class="col-md-${provider.large?'12':'6'} ${not empty content.description?'width-description':'without-description'}">
<div class="thumbnail shared-content-item ${provider.large?'large':'small'}" data-shared="${content.id}">
<c:if test="${not empty content.editURL}">
<c:if test="${!content.editAsModal}">
<a href="${content.editURL}" title="edit" lang="en">
</c:if><c:if test="${content.editAsModal}">
<a href="${content.editURL}" title="edit" lang="en" onclick="editPreview.openModal('${content.title}', '${content.editURL}'); return false;">
</c:if></c:if>	
	<c:if test="${not empty content.imageURL}">
		<figure>
			<c:if test="${!contentContext.ajax}"><img id="img-${status.index}" data-src-on-visible-preview="${content.imageURL}" /></c:if>
			<c:if test="${contentContext.ajax}"><img id="img-${status.index}" src="${content.imageURL}" /></c:if>
		</figure>
	</c:if>	
	<div class="caption">${content.title}</div>
	<c:if test="${not empty content.photoPageLink}"><div class="links"><a target="_blank" href="${content.photoPageLink}"><i class="fa fa-external-link" aria-hidden="true"></i> download HD</a></div></c:if>
<c:if test="${not empty content.editURL}">
</a>
</c:if>	
</div>
</div>
</c:forEach>
</div>
</div>
