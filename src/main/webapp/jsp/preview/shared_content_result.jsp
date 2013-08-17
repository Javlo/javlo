<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:forEach var="content" items="${sharedContent}">
<div class="content-wrapper ${not empty content.description?'width-description':'without-description'}">
<div class="content" data-shared="${content.id}">
	<h4><span>${content.title}</span></h4>
	<figure><img src="${content.imageURL}" /></figure>
	<c:if test="${not empty content.description}">
		<div class="description">${content.description}</div>
	</c:if>	
</div>
</div>
</c:forEach>

