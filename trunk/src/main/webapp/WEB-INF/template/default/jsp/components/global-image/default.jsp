<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:choose>
<c:when test="${url eq '#'}">
<figure>
<span class="nolink">
<img src="${previewURL}" alt="${not empty label?label:description}" />
<c:if test="${empty param.nolabel}"><figcaption>${not empty label?label:description}</figcaption></c:if>
</span>
</figure>
</c:when>
<c:otherwise>
<figure>
<c:set var="rel" value="${fn:startsWith(url,'http://')?'external':'shadowbox'}" />
<c:set var="rel" value="${fn:endsWith(url,'.pdf')?'pdf':rel}" />
<a rel="${rel}" class="${type}" href="${url}" title="${not empty label?label:description}">
	<c:if test="${contentContext.asPreviewMode}">
		<img class="image-preview-loading" id="img-${compid}" src="${info.ajaxLoaderURL}" data-src="${previewURL}" alt="${not empty description?description:label}" />
		<script type="text/javascript">updateImagePreview();</script>
	</c:if>
	<c:if test="${not contentContext.asPreviewMode}">
		<img src="${previewURL}" alt="${not empty description?description:label}" />
	</c:if>
	<c:if test="${empty param.nolabel}"><figcaption>${not empty label?label:description}</figcaption></c:if>
</a>
</figure>
</c:otherwise>
</c:choose>