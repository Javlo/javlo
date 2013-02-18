<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:choose>
<c:when test="${url eq '#'}">
<figure>
<img src="${previewURL}" alt="${not empty label?label:description}" />
<c:if test="${empty param.no-label}"><figcaption>${not empty label?label:description}</figcaption></c:if>
</figure>
</c:when>
<c:otherwise>
<figure>
<a rel="shadowbox" class="${type}" href="${url}" title="${not empty label?label:description}">
	<img src="${previewURL}" alt="${not empty description?description:label}" />
	<c:if test="${empty param.nolabel}"><figcaption>${not empty label?label:description}</figcaption></c:if>
</a>
</figure>
</c:otherwise>
</c:choose>