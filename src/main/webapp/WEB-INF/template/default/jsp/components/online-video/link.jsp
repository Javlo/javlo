<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<a href="${link}">
<c:if test="${not empty image}">
	<img src="${image}" alt="${title}" title="${label}" />
</c:if>
<c:if test="${empty image}">
	<img src="${previewURL}" alt="${title}" title="${label}" />
</c:if>
<c:if test="${empty image}">
    ${label}
</c:if>
</a>