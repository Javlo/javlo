<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:choose>
<c:when test="${url eq '#'}">
<figure>
<span class="nolink">
<img src="${previewURL}" alt="${not empty label?cleanLabel:cleanDescription}" />
<c:if test="${empty param.nolabel}"><figcaption>${not empty label?label:description}</figcaption></c:if>
</span>
</figure>
</c:when>
<c:otherwise>
<figure>
<c:set var="rel" value="${fn:startsWith(url,'http://')?'external':'shadowbox'}" />
<c:set var="rel" value="${fn:endsWith(url,'.pdf')?'pdf':rel}" />
<a rel="${rel}" class="${type}" href="${url}" title="${not empty label?cleanLabel:cleanDescription}">
	<c:if test="${contentContext.asPreviewMode && filter != 'raw'}">
		<c:set var="imageId" value="i${info.randomId}" />
		<img id="${imageId}" src="${info.ajaxLoaderURL}" alt="${not empty description?cleanDescription:cleanLabel}" />
	</c:if>
	<c:if test="${not (contentContext.asPreviewMode && filter != 'raw')}">
		<c:set var="imageWidthTag" value='width="${imageWidth}" ' />
		<img ${not empty imageWidth && filter!='raw'?imageWidthTag:''}src="${previewURL}" alt="${not empty description?cleanDescription:cleanLabel}" />
	</c:if>
</a>
<c:if test="${empty param.nolabel}"><figcaption>${not empty label?label:description}</figcaption></c:if>
</figure>
</c:otherwise>
</c:choose>

<c:if test="${contentContext.asPreviewMode && filter != 'raw'}">
<script type="text/javascript">
jQuery("#${imageId}").attr("src", "${previewURL}");
jQuery("#${imageId}").load(function() {	
	if (jQuery(this).src != "${info.ajaxLoaderURL}" && !jQuery(this).hasClass("refreshed") && jQuery(this).attr("src").indexOf("/transform/")>=0) {		
		jQuery.post( "${info.currentAjaxURL}", { webaction: "global-image.dataFeedBack", compid: "${compid}", height: jQuery("#${imageId}").height(), width: jQuery("#${imageId}").width()}, {dataType: "json"}).done(function(data) {
			jQuery("#${imageId}").addClass("refreshed");			
			jQuery("#${imageId}").attr("src", data.data.previewURL);
		});
	}
});
</script>
</c:if>