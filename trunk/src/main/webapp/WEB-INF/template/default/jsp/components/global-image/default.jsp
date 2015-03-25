<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><c:set var="styleWidth" value="" /><c:if test="${not empty componentWidth && !param['clean-html']}"><c:set var="styleWidth" value=' style="width: ${componentWidth};"' /></c:if>
<c:choose>
<c:when test="${link eq '#'}">
<figure>
<span class="nolink">
<img src="${previewURL}" alt="${not empty label?cleanLabel:cleanDescription}"${styleWidth} />
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
		<img id="${imageId}" src="${info.ajaxLoaderURL}" alt="${not empty description?cleanDescription:cleanLabel}"${styleWidth} />
	</c:if>
	<c:if test="${not (contentContext.asPreviewMode && filter != 'raw')}">
		<c:set var="imageWidthTag" value='width="${imageWidth}" ' />
		<img ${not empty imageWidth && filter!='raw'?imageWidthTag:''}src="${previewURL}" alt="${not empty description?cleanDescription:cleanLabel}"${styleWidth} />
	</c:if>
</a>
<c:if test="${empty param.nolabel}"><figcaption>${not empty label?label:description}</figcaption></c:if>
</figure>
</c:otherwise>
</c:choose>

<c:if test="${contentContext.asPreviewMode && filter != 'raw'}">
<script type="text/javascript">
var localJQ = jQuery;
if (typeof(pjq) !== 'undefined') {
	localJQ = pjq;
}
+function($) {
	var img = $("#${imageId}");
	img.attr("src", "${previewURL}");
	img.load(function() {	
		if (img.src != "${info.ajaxLoaderURL}" && !img.hasClass("refreshing") && !img.hasClass("refreshed") && img.attr("src").indexOf("/transform/")>=0) {		
			img.addClass("refreshing");
			$.post( "${info.currentAjaxURL}", { webaction: "global-image.dataFeedBack", compid: "${compid}", height: img.height(), width: img.width()}, {dataType: "json"}).done(function(data) {
				img.addClass("refreshed");
				img.removeClass("refreshing");
				if (typeof data.data != "undefined") {
					img.attr("src", data.data.previewURL);
				}
			});
		}
	});
}(localJQ);
</script>
</c:if>