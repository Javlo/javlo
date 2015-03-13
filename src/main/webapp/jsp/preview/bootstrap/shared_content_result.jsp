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
<c:forEach var="content" items="${sharedContent}">
<div class="col-md-6 ${not empty content.description?'width-description':'without-description'}">
<div class="thumbnail shared-content-item" data-shared="${content.id}">	
	<c:if test="${not empty content.imageURL}">
		<figure><img src="${content.imageURL}" /></figure>
	</c:if>	
	<div class="caption">${content.title}</div>	
</div>
</div>
</c:forEach>
</div>
</div>

