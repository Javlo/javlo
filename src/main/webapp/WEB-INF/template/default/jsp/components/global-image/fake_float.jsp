<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"%><jv:changeFilter var="previewURL" url="${previewURL}" filter="full" newFilter="float" />
<c:set var="loadEvent" value="" />
<c:set var="styleWidth" value="" /><c:set var="styleWidthWidthoutStyle" value="" /><c:if test="${not empty componentWidth && !param['clean-html']}"><c:set var="styleWidthWidthoutStyle" value='width: ${componentWidth};' /><c:set var="styleWidth" value=' style="width: ${componentWidth};"' /></c:if>
<c:set var="styleOppositeWidth" value="" /><c:if test="${not empty componentOpositeWidth && !param['clean-html']}"><c:set var="styleOppositeWidth" value='width: ${componentOpositeWidth};' /></c:if>
<div id="comp-${compid}"><table class="${empty componentWidth?'float-image':'float-image-width'} ${param.right?'right':'left'}">
<c:if test="${contentContext.asPreviewMode}">
<div class="source" style="display: none;"><span class="container">${label}</span></div>
<script type="text/javascript">
var localJQ = jQuery;
if (typeof(pjq) !== 'undefined') {
	localJQ = pjq;
}
function loadImage${compid}() {
	img = localJQ("#comp-${compid} img");
	if (localJQ(img).attr("src") != "${info.ajaxLoaderURL}" && !localJQ(img).hasClass("refreshed") && localJQ(img).attr("src").indexOf("/transform/")>=0) {
		<c:if test="${info.device.code != 'pdf'}">
			editPreview.floatZone("#comp-${compid} .source .container", "#comp-${compid} .zone1 .container", "#comp-${compid} .zone2 .container", "#comp-${compid} img");
			var firstText=localJQ("#comp-${compid} .zone1 .container").html();
			var secondText = localJQ("#comp-${compid} .zone2 .container").html();
			localJQ.post( "${info.currentAjaxURL}", { webaction: "global-image.dataFeedBack", compid: "${compid}", firsttext: firstText, secondtext: secondText, height: localJQ("#comp-${compid} img").height(), width: localJQ("#comp-${compid} img").width()}, {dataType: "json"}).done(function(data) {
				localJQ("#comp-${compid} img").addClass("refreshed");		
				localJQ("#comp-${compid} img").attr("src", data.data.previewURL);
			});		
		</c:if><c:if test="${info.device.code == 'pdf'}">
			img.addClass("refreshing");		
			localJQ.post( "${info.currentAjaxURL}", { webaction: "global-image.dataFeedBack", compid: "${compid}", height: img.height(), width: img.width()}, {dataType: "json"}).done(function(data) {			
				img.addClass("refreshed");
				img.removeClass("refreshing");
				if (typeof data.data != "undefined") {
					img.attr("src", data.data.previewURL);
				}
			});
		</c:if>
	}
}
</script>
<c:set var="loadEvent" value=' onLoad="loadImage${compid}();"' />
</c:if>

<c:if test="${info.device.code != 'pdf'}">
<c:if test="${!param.right}">
<tr><td class="image-wrapper"${styleWidth}>
<c:if test="${link != '#'}">
<c:set var="rel" value="${fn:startsWith(url,'http://')?'external':'shadowbox'}" />
<c:set var="rel" value="${fn:endsWith(url,'.pdf')?'pdf':rel}" />
<a rel="${rel}" class="${type}" href="${url}"></c:if>
<img src="${previewURL}"${loadEvent} />
<c:if test="${link != '#'}"></a></c:if>
</td><td class="sep" style="width: 10px; font-size: 0;">&nbsp;</td><td class="zone1" style="text-align: left; ${not empty styleOppositeWidth?styleOppositeWidth:''}"><span class="container">${comp.firstText}</span></td></tr>
</c:if><c:if test="${param.right}">
<tr><td class="zone1" style="text-align: left; ${not empty styleOppositeWidth?styleOppositeWidth:''}"><span class="container">${comp.firstText}</span></td>
<td class="sep" style="width: 10px; font-size: 0;">&nbsp;</td>
<td class="image-wrapper"${styleWidth}>
<c:if test="${link != '#'}">
<c:set var="rel" value="${fn:startsWith(url,'http://')?'external':'shadowbox'}" />
<c:set var="rel" value="${fn:endsWith(url,'.pdf')?'pdf':rel}" />
<a rel="${rel}" class="${type}" href="${url}"></c:if>
<img ${not empty imageWidth?imageWidthTag:''}src="${previewURL}"${loadEvent} />
<c:if test="${link != '#'}"></a></c:if>
</td></tr>
</c:if>
<tr><td class="zone2" colspan="3" style="text-align: left; ${areaStyle.textStyle}${styleWidthWidthoutStyle}"><span class="container">${comp.secondText}</span></td></tr>
</c:if>
<c:if test="${info.device.code == 'pdf'}">
<tr><td>
<c:set var="imageWidthTag" value='width="${imageWidth}" ' />
<c:set var="imageWidthStyle" value='width:${imageWidth}px' />
<div class="image-wrapper" style="float: ${param.right?'right':'left'};${styleWidthWidthoutStyle}${param['clean-html']?imageWidthStyle:''}">
<c:if test="${link != '#'}">
<c:set var="rel" value="${fn:startsWith(url,'http://')?'external':'shadowbox'}" />
<c:set var="rel" value="${fn:endsWith(url,'.pdf')?'pdf':rel}" />
<a rel="${rel}" class="${type}" href="${url}"></c:if>
<img ${not empty imageWidth?imageWidthTag:''}src="${previewURL}"${loadEvent} />
<c:if test="${link != '#'}"></a></c:if>
</div><div class="text">${label}</div>
</td></tr>
</c:if>
</table>
</div>