<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><div  id="comp-${compid}">

<table class="float-image ${not empty param.right?'right':'left'}">

<c:if test="${info.device.code != 'pdf'}">
<c:if test="${empty param.right}">
<tr><td class="image-wrapper"><c:set var="style" value="" />
<img src="${fn:replace(previewURL,'/full/', '/float/')}"${style} />
</td><td class="zone1" style="text-align: left;"><span class="container">${comp.firstText}</span></td></tr>
</c:if><c:if test="${not empty param.right}">
<tr><td class="zone1" style="text-align: left;"><span class="container">${comp.firstText}</span></td>
<td><c:set var="style" value="" />
<img src="${fn:replace(previewURL,'/full/', '/float/')}"${style} />
</td></tr>
</c:if>
<tr><td class="zone2" colspan="2" style="text-align: left; ${areaStyle.textStyle}"><span class="container">${comp.secondText}</span></td></tr>
</c:if>
<c:if test="${info.device.code == 'pdf'}">
<tr><td>
<div class="image-wrapper" style="float: ${not empty param.right?'right':'left'};">
<img src="${fn:replace(previewURL,'/full/', '/float/')}" />
</div><div class="text">${label}</div>
</td></tr>
</c:if>
</table>
<div class="source" style="display: none;"><span class="container">${label}</span></div>
<c:if test="${contentContext.asPreviewMode && comp.textAuto}">
<script type="text/javascript">
jQuery("#comp-${compid} img").load(function() {
	floatZone("#comp-${compid} .source .container", "#comp-${compid} .zone1 .container", "#comp-${compid} .zone2 .container", "#comp-${compid} img");
	var firstText=jQuery("#comp-${compid} .zone1 .container").html();
	var secondText = jQuery("#comp-${compid} .zone2 .container").html();
	jQuery.post( "${info.currentAjaxURL}", { webaction: "global-image.dataFeedBack", compid: "${compid}", firsttext: firstText, secondtext: secondText, height: jQuery("#comp-${compid} img").height(), width: jQuery("#comp-${compid} img").width()});
	
	//ajaxRequest(url, null, null);
});
</script></c:if></div>