<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"
%><script>
function acceptCookie() {
	actionCookie(1);	
	var xhttp = new XMLHttpRequest();<c:url var="accepturl" value="${info.currentURL}" context="/"><c:param name="webaction" value="view.acceptcookies" /></c:url>
	xhttp.open("GET", "${accepturl}", true);
	xhttp.send();
	return false;
}

function refuseCookie() {
	actionCookie(0);
	var xhttp = new XMLHttpRequest();<c:url var="refuseurl" value="${info.currentURL}" context="/"><c:param name="webaction" value="view.refusecookies" /></c:url>
	xhttp.open("GET", "${refuseurl}", true);
	xhttp.send();	
	return false;
}

function actionCookie(action) {
	 cc = document.body.querySelectorAll('._cookie-cache');
	 i = 0;
	 for( i=0; i < cc.length; i++ ) {
	       if (cc[i].dataset.status==action) {
			   if (cc[i].dataset.html != null) {
	    	   	cc[i].outerHTML = cc[i].dataset.html;
			   }
			   if (cc[i].dataset.function != null) {
				window[cc[i].dataset.function]();
			   }
	       }
	 }
	 cn = document.body.querySelectorAll('._cookie-nochoice');
	 i = 0;
	 for( i=0; i < cn.length; i++ ) {
       cn[i].remove(cn[i].parentElement);
	 }
	 if (action == 1) {
		 cn = document.body.querySelectorAll('._cookie-notacceptedchoice');
		 i = 0;
		 for( i=0; i < cn.length; i++ ) {
	       cn[i].remove(cn[i].parentElement);
		 }
	 }
	 document.body.querySelector('#cookies-message').className="cookie-close";
}
</script>
<div id="cookies-message">	
	<p>${i18n.view['cookies.message']}</p>
	<div class="actions">
		<form action="${info.currentURL}" method="post">
			<button type="button" name="webaction" value="view.acceptcookies" class="btn btn-sm btn-primary" onclick="return acceptCookie();">${i18n.view['cookies.accept']}</button>
			<button type="button" name="webaction" value="view.refusecookies" class="btn btn-sm btn-secondary" onclick="return refuseCookie();">${i18n.view['cookies.refuse']}</button>
			<c:if test="${not empty globalContext.cookiesPolicyUrl}"><a class="btn btn-sm btn-secondary" target="_blank" href="${info.cookiesPolicyUrl}">${i18n.view['global.more']}</a></c:if>
		</form>
	</div>	
</div>