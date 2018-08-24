 <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><%@ taglib prefix="fn"
	uri="http://java.sun.com/jsp/jstl/functions"%><%@page
	contentType="text/html"
%><c:url var="mobileURL" value="${info.currentURL}" context="/">
	<c:param name="__preview_only_mode" value="true" />
</c:url>
<div id="_mobile_preview" class="collapse">
	<div class="_mobile_header">
		<div class="_mobile_action">
			<a href="#" onclick="document.getElementById('_mobile_iframe').width=320; return false;">Phone</a>
			<a href="#" onclick="document.getElementById('_mobile_iframe').width=480; return false;">Phablet</a>
			<a href="#" onclick="document.getElementById('_mobile_iframe').width=768; return false;">Tablet</a>
		</div>
	</div>	
	<div class="_mobile_wrapper" style="background:url('${info.viewAjaxLoaderURL}') center center no-repeat; background-size: 150px 150px;">
		<iframe id="_mobile_iframe" width="320" height="auto" seamless></iframe>
	</div>
	<div class="_mobile_footer">
		<a class="_mobile_micro" href="#" onclick="document.getElementById('_mobile_iframe').src = '${mobileURL}';return false;"></a>
	</div>
</div>
<script>
	pjq('#_btn-mobile').on('click', function () {
		if (document.getElementById('_mobile_iframe').src.length==0) {
			document.getElementById('_mobile_iframe').src='${mobileURL}';
		}
	});	
</script>
