<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><div  id="comp-${compid}">

<table class="float-image ${not empty param.right?'right':'left'}">

<c:if test="${info.device.code != 'pdf'}">
<c:if test="${empty param.right}">
<tr><td style="width: 50%;" width="50%"><c:set var="style" value="" />
<img style="width: 100%;" src="${fn:replace(previewURL,'/full/', '/float/')}"${style} />
</td><td class="zone1" width="50%" style="width: 50%; text-align: left;"><span class="container">${comp.firstText}</span></td></tr>
</c:if><c:if test="${not empty param.right}">
<tr><td class="zone1" width="50%" style="width: 50%; text-align: left;"><span class="container">${comp.firstText}</span></td>
<td style="width: 50%;" width="50%"><c:set var="style" value="" />
<img style="width: 100%;" src="${fn:replace(previewURL,'/full/', '/float/')}"${style} />
</td></tr>
</c:if>
<tr><td class="zone2" colspan="2" style="text-align: left;"><span class="container">${comp.secondText}</span></td></tr>
</c:if>
<c:if test="${info.device.code == 'pdf'}">
<tr><td>
<div class="image-wrapper" style="width: 50%; float: ${not empty param.right?'right':'left'};">
<img style="width: 100%;" src="${fn:replace(previewURL,'/full/', '/float/')}" />
</div><div class="text">${label}</div>
</td></tr>
</c:if>

</table>


<div class="source" style="display: none;"><span class="container">${label}</span></div>
<c:if test="${contentContext.asPreviewMode && comp.textAuto && info.device.code != 'pdf'}">
<script type="text/javascript">
jQuery("#comp-${compid} img").load(function() {
	floatZone("#comp-${compid} .source .container", "#comp-${compid} .zone1 .container", "#comp-${compid} .zone2 .container", "#comp-${compid} img");
	<c:url var="url" value="${info.currentAjaxURL}">
		<c:param name="webaction" value="global-image.dataFeedBack" />
		<c:param name="comp-id" value="${compid}" />
	</c:url>	
	var url = "${url}&first-text="+jQuery("#comp-${compid} .zone1 .container").html()+"&second-text="+jQuery("#comp-${compid} .zone2 .container").html()+"&height="+jQuery("#comp-${compid} img").height()+"&width="+jQuery("#comp-${compid} img").width();	
	ajaxRequest(url, null, null);
});
</script></c:if></div>