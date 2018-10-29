<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>

<c:if test="${param.close}">
<script>
window.close();
</script>
</c:if>

<div class="alert alert-info" role="alert">${i18n.edit['macro.socialshare.warning-published']}</div>
<script>
function pop(url, title) {
	var w = 680;
	var h = 420;
    var dualScreenLeft = window.screenLeft != undefined ? window.screenLeft : screen.left;
    var dualScreenTop = window.screenTop != undefined ? window.screenTop : screen.top;
    var width = window.innerWidth ? window.innerWidth : document.documentElement.clientWidth ? document.documentElement.clientWidth : screen.width;
    var height = window.innerHeight ? window.innerHeight : document.documentElement.clientHeight ? document.documentElement.clientHeight : screen.height;
    var left = ((width / 2) - (w / 2)) + dualScreenLeft;
    var top = ((height / 2) - (h / 2)) + dualScreenTop;
    var newWindow = window.open(url, title, 'scrollbars=yes, width=' + w + ', height=' + h + ', top=' + top + ', left=' + left);
    if (window.focus) {
        newWindow.focus();
    }
}
</script>
<c:url var="closeURL" value="${info.currentAbsoluteURL}">
	<c:param name="close" value="true" />
</c:url>
<div class="flex-space-between">
<c:if test="${empty facebookID}"><a class="btn btn-default btn-facebook" href="#" onClick="pop('https://www.facebook.com/sharer.php?u=${url}', 'Facebook'); return false;" ><i class="fa fa-facebook" aria-hidden="true"></i> Facebook</a></c:if>
<c:if test="${not empty facebookID}"><a class="btn btn-default" href="#" onClick="pop('https://www.facebook.com/dialog/share?app_id=${facebookID}&display=page&href=${url}&redirect_uri=${closeURL}', 'Facebook'); return false;"><i class="fa fa-facebook" aria-hidden="true"></i> Facebook</a></c:if>
<a class="btn btn-default" href="#" onClick="pop('https://twitter.com/intent/tweet?url=${url}&text=${title}','Twitter'); return false;"><i class="fa fa-twitter" aria-hidden="true"></i> Twitter</a>
<a class="btn btn-default btn-linkedin" href="#" onClick="pop('https://www.linkedin.com/shareArticle?url=${url}&title=${title}', 'LinkedIn'); return false;"><i class="fa fa-linkedin" aria-hidden="true"></i> LinkedIn</a>
<c:if test="${not empty img}"><a class="btn btn-default btn-pinterest" href="#" onClick="pop('https://pinterest.com/pin/create/bookmarklet/?media=${img}&url=${url}&is_video=false&description=${title}', 'Pinterest');"><i class="fa fa-pinterest" aria-hidden="true"></i> Pinterest</a></c:if>
</div>

