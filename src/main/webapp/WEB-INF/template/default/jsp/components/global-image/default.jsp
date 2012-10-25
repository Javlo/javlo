<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:choose>
<c:when test="${url eq '#'}">
<img src="${previewURL}" alt="${not empty label?label:description}" />
</c:when>
<c:otherwise>
<a rel="shadowbox" class="${type}" href="${url}" title="${not empty label?label:description}">
	<img src="${previewURL}" alt="${not empty description?description:label}" />
</a>
</c:otherwise>
</c:choose>