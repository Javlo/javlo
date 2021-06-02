<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><%@ taglib
	prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%><%@ taglib
	uri="/WEB-INF/javlo.tld" prefix="jv"%>

<link rel="stylesheet" href="${info.rootTemplateURL}/cookies.css" />

<div id="cookies-message" style="z-index: 100; font-size: 14px;">
	<div style="position: relative;">
		<div id="cookies-popup-plus"
			style="position: fixed; top: 0; left: 0; overflow: scroll; height: 100vh; width: 100vw; background-color: #fff; z-index: 100; display: none;">
			<div class="close-popup-cookies"
				style="float: right; margin-top: 15px; margin-right: 15px;">
				<a href="#" onclick="hideCookiesPopup()"> <svg
						xmlns="http://www.w3.org/2000/svg" width="16" height="16"
						fill="currentColor" class="bi bi-x-lg" viewBox="0 0 16 16">
			<path
							d="M1.293 1.293a1 1 0 0 1 1.414 0L8 6.586l5.293-5.293a1 1 0 1 1 1.414 1.414L9.414 8l5.293 5.293a1 1 0 0 1-1.414 1.414L8 9.414l-5.293 5.293a1 1 0 0 1-1.414-1.414L6.586 8 1.293 2.707a1 1 0 0 1 0-1.414z" />
		</svg>
				</a>
			</div>
			<div class="logo-popup-cookies"
				style="float: left; margin-top: 15px; margin-left: 15px;">
				<img src="${info.logoRawUrl}"
					style="max-height: 50px; max-width: 250px;" />
			</div>

			<div id="cookies_subselection">
				<div
					style="border: 1px #ddd solid; border-radius: 3px; background-color: #eee; width: 60%; min-width: 320px; margin: 80px auto; padding: 15px;">
					<h2 class="cookie-main-title">${vi18n['cookies.banner.title']}
					</h2>
					<p class="intro" style="text-align: justify;">${vi18n['cookies.banner.intro']}</p>

					<form id="_cookies_type_form" method="post"
						action="${info.currentURL}">
						<input type="hidden" name="webaction"
							value="view.acceptcookiestype" /> <label class="toggle"
							for="_cookies_technic"> <input type="checkbox"
							class="toggle__input" id="_cookies_technic" checked disabled />
							<span class="toggle-track"> <span class="toggle-indicator">
							</span>
						</span> ${vi18n["cookies.type.technic"]}
						</label>

						<c:forEach var="type" items="${globalContext.cookiesTypes}">
							<label class="toggle" for="_cookies_${type}"> <input
								type="checkbox" class="toggle__input" id="_cookies_${type}"
								name="cookies_${type}" /> <span class="toggle-track"> <span
									class="toggle-indicator"> </span>
							</span> <c:set var="i18nkey" value="cookies.type.${type}" />
								${vi18n[i18nkey]}
							</label>
						</c:forEach>

						<div class="cookies-actions"
							style="display: flex; justify-content: flex-end; margin-top: 1rem;">
							<button class="btn_cookies btn_secondary" type="submit">${vi18n['cookies.banner.submit']}</button>
							<button class="btn_cookies btn_secondary" type="submit"
								name="refuse" value="true">${vi18n['cookies.banner.refuse']}</button>
							<button class="btn_cookies btn_primary" type="submit"
								name="accept" value="true">${vi18n['cookies.banner.accept']}</button>
						</div>

					</form>
				</div>
				<div id="cookies-popup-content"
					style="width: 60%; min-width: 320px; margin: 60px auto;">
					<div style="text-align: center; margin: 15px;">
						<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32"
							fill="currentColor" class="bi bi-three-dots" viewBox="0 0 16 16">
					<path
								d="M3 9.5a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3zm5 0a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3zm5 0a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3z" />
				  </svg>
					</div>
				</div>
			</div>
		</div>
	</div>

	<div id="cookies-message-inside-wrapper">
		<p>${i18n.view['cookies.message']}</p>
		<div class="actions">
			<form action="${info.currentURL}" method="post">
				<a class="btn_cookies" target="_blank" href="#"
					onclick="return showCookiesPopup();">${i18n.view['global.more']}</a>
				<button type="button" name="webaction" value="view.refusecookies"
					class="btn_cookies" onclick="return refuseCookie();">${i18n.view['cookies.refuse']}</button>
				<button type="button" name="webaction" value="view.acceptcookies"
					class="btn_cookies btn_primary" onclick="return acceptCookie();">${i18n.view['cookies.accept']}</button>
			</form>
		</div>
	</div>
</div>

<script>

var _beforeCookieBodyOverflow = document.body.style;

function showCookiesPopup() {
	document.getElementById('cookies-popup-plus').style.display="block";
	_beforeCookieBodyOverflow = document.body.style;
	document.body.style.overflow="hidden";
	return false;
}

function hideCookiesPopup() {
	document.getElementById('cookies-popup-plus').style.display="none";
	document.body.style.overflow=_beforeCookieBodyOverflow;
}

function acceptCookie() {
	actionCookie(1);	
	var xhttp = new XMLHttpRequest();<c:url var="accepturl" value="${info.currentURL}" context="/"><c:param name="webaction" value="view.acceptcookies" /></c:url>
	xhttp.open("GET", "${accepturl}", true);
	xhttp.send();
	return false;
}

function acceptCookieType(type, accepted) {
	actionCookie(1);	
	var xhttp = new XMLHttpRequest();<c:url var="accepturl" value="${info.currentURL}" context="/"><c:param name="webaction" value="view.acceptcookiestype" /></c:url>
	xhttp.open("GET", "${accepturl}&type="+type+"&accepted="+accepted, true);
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

function addParamCokkies(url, params) {
	var isQuestionMarkPresent = url && url.indexOf('?') !== -1,
		separator = '';
	if (params) {
		separator = isQuestionMarkPresent ? '&' : '?';
		url += separator + params;
	}
	return url;
}

function getAreaForCookie(url, area) {
	url = addParamCokkies(url, "area-only="+area);
	fetch(url).then(function (response) {
		return response;
	});
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

<c:if test="${not empty globalContext.cookiesPolicyUrl}">
	fetch("${globalContext.cookiesPolicyUrl}?only-area=content").then(function (response) {
		return response.text();
	}).then(function (html) {
		document.getElementById('cookies-popup-content').innerHTML=html;
	});
</c:if>
</script>