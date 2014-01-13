<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="colsClass" value="cols" />
<c:forEach var="content" items="${sharedContent}">
	<c:if test="${not empty content.description}">
		<c:set var="colsClass" value="no-cols" />
	</c:if>
</c:forEach>

<div class="${colsClass} provider-${provider.name} privider-type-${provider.type}">
<c:forEach var="content" items="${sharedContent}">
<div class="content-wrapper ${not empty content.description?'width-description':'without-description'}">
<div class="content" data-shared="${content.id}">
	<h3><span>${content.title}</span></h3>
	<c:if test="${not empty content.imageURL}">
		<figure><img src="${content.imageURL}" /></figure>
	</c:if>
	<c:if test="${not empty content.description}">
		<div class="description">${content.description}</div>
	</c:if>	
</div>
</div>
</c:forEach>
</div>

