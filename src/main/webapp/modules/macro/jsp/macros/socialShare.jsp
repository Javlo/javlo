<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>

<div class="alert alert-warning" role="alert">${i18n.edit['macro.socialshare.warning-published']}</div>

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

<c:if test="${empty facebookID}">
<a class="btn btn-default btn-facebook" href="#" onClick="pop('https://www.facebook.com/sharer.php?u=${url}', 'Facebook'); return false;" >Facebook</a>
</c:if><c:if test="${not empty facebookID}">
<a class="btn btn-default" href="#" onClick="pop('https://www.facebook.com/dialog/share?app_id=${facebookID}&display=page&href=${url}&redirect_uri=${info.currentAbsoluteURL}', 'Facebook'); return false;">Facebook</a>
</c:if>

<a class="btn btn-default" href="#" onClick="pop('https://twitter.com/intent/tweet?url=${url}&text=${title}','Twitter'); return false;">Twitter</a>

<a class="btn btn-default btn-googleplus" href="#" onClick="pop('https://plus.google.com/share?url={url}${url}&text=${title}', 'Google+'); return false;">Google+</a>

<a class="btn btn-default btn-pinterest" href="#" onClick="pop('https://www.linkedin.com/shareArticle?url=${url}&title=${title}', 'LinkedIn'); return false;">LinkedIn</a>



<c:if test="${not empty img}">
<a class="btn btn-default btn-pinterest" href="#" onClick="pop('https://pinterest.com/pin/create/bookmarklet/?media=${img}&url=${url}&is_video=false&description=${title}', 'Pinterest');">Pinterest</a>
</c:if>

