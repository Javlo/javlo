<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%>
<c:set var="imageAlt" value="${not empty label?cleanLabel:cleanDescription}" />
<c:if test="${not empty file && not empty file.title || not empty file.description}">
    <c:if test="${not empty file.title && not empty file.description}">
		<c:set var="imageAlt" value="${file.title} : ${file.description}" />
	</c:if>
	<c:if test="${not empty file.title || not empty file.description}">
		<c:set var="imageAlt" value="${file.title}${file.description}" />
	</c:if>
</c:if>
<c:set var="imageId" value="i${compid}" />
<c:set var="styleWidth" value="" /><c:if test="${not empty componentWidth && !param['clean-html']}"><c:set var="styleWidth" value=' style="width: ${componentWidth};"' /></c:if>
<c:choose>
<c:when test="${link eq '#'}">
<figure>
<span class="nolink">
<img src="${previewURL}" alt="${imageAlt}"${styleWidth} />
<c:if test="${empty param.nolabel}"><figcaption>${not empty label?label:description}</figcaption></c:if>
</span>
</figure>
</c:when>
<c:otherwise>
<figure>
<c:set var="rel" value="${fn:startsWith(url,'http://')?'external':'shadowbox'}" />
<c:set var="rel" value="${fn:endsWith(url,'.pdf')?'pdf':rel}" />
<a rel="${rel}" class="${type}" href="${url}" title="${not empty label?cleanLabel:cleanDescription}">
	<c:set var="imageWidthTag" value='width="${imageWidth}" ' />
	<c:set var="loadEvent" value="" />
	<c:if test="${contentContext.asPreviewMode && filter != 'raw'}"><c:set var="imageClass" value='class="return-size" data-compid="${compid}"' /></c:if>
	<img ${imageClass} ${not empty imageWidth && filter!='raw'?imageWidthTag:''}src="${previewURL}" alt="${imageAlt}"${styleWidth} />	
</a>
<c:set var="copyrightHTML" value="" />
<c:if test="${not empty copyright}"><c:set var="copyrightHTML" value='<span class="copyright">${copyright}</span>' /></c:if>
<c:if test="${empty param.nolabel || not empty copyright}"><figcaption>${not empty label?label:description}${copyrightHTML}</figcaption></c:if>
</figure>
</c:otherwise>
</c:choose>