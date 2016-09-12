<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%>
<center class="center" style="text-align: center;">

<c:set var="imageId" value="i${compid}" />
<c:if test="${contentContext.asPreviewMode && filter != 'raw'}">
<script type="text/javascript">
var localJQ = jQuery;
if (typeof(pjq) !== 'undefined') {
	localJQ = pjq;
}
	
function loadImage${imageId}() {
	var img = localJQ("#${imageId}");	
	if (img.src != "${info.ajaxLoaderURL}" && !img.hasClass("refreshing") && !img.hasClass("refreshed") && img.attr("src").indexOf("/transform/")>=0) {		
		img.addClass("refreshing");		
		localJQ.post( "${info.currentAjaxURL}", { webaction: "global-image.dataFeedBack", compid: "${compid}", height: img.height(), width: img.width()}, {dataType: "json"}).done(function(data) {
			img.addClass("refreshed");
			img.removeClass("refreshing");
			if (typeof data.data != "undefined") {
				img.attr("src", data.data.previewURL);
			}
		});
	}
}
</script>
</c:if>
<c:set var="display" value="inline" /><c:if test="${info.device.code == 'pdf'}"><c:set var="display" value="block" /></c:if>
<c:set var="styleWidth" value=' style="display: ${display};"' /><c:if test="${not empty componentWidth && !param['clean-html']}"><c:set var="styleWidth" value=' style="display: ${display}; width: ${componentWidth};"' /></c:if>
<c:choose>
<c:when test="${link eq '#'}">
<figure>
<span class="nolink">
<img src="${previewURL}" ${styleWidth} />
<c:if test="${empty param.nolabel}"><figcaption>${not empty label?label:description}</figcaption></c:if>
</span>
</figure>
</c:when>
<c:otherwise>
<figure>
<c:set var="rel" value="${fn:startsWith(url,'http://')?'external':'shadowbox'}" />
<c:set var="rel" value="${fn:endsWith(url,'.pdf')?'pdf':rel}" />
<a rel="${rel}" class="${type}" href="${url}">
	<c:set var="imageWidthTag" value='width="${imageWidth}" ' />
	<c:set var="loadEvent" value="" />
	<c:if test="${contentContext.asPreviewMode && filter != 'raw'}"><c:set var="loadEvent" value=' id="${imageId}" onLoad="loadImage${imageId}();"' /></c:if>
	<img ${not empty imageWidth && filter!='raw'?imageWidthTag:''}src="${previewURL}" />	
</a>
<c:set var="copyrightHTML" value="" />
<c:if test="${not empty copyright}"><c:set var="copyrightHTML" value='<span class="copyright">${copyright}</span>' /></c:if>
<c:if test="${empty param.nolabel || not empty copyright}"><figcaption>${not empty label?label:description}${copyrightHTML}</figcaption></c:if>
</figure>
</c:otherwise>
</c:choose>

</center> 