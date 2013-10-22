<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${not empty social.facebook.clientId}">
<div id="fb-login-button"></div>
<form id="fb-login-form" action="${info.currentURL}" method="post">
<input name="token" type="text" id="fbtoken" />
<input type="hidden" name="webaction" value="social.facebookLogin" />
<input type="submit" />
</form>
<script src="http://connect.facebook.net/${contentContext.requestContentLanguage}/all.js" ></script>
<div id="fb-root"></div>
<script type="text/javascript">
function handleFacebook() { 
    FB.init({appId: '${social.facebook.clientId}', xfbml: true, cookie: true});
    FB.getLoginStatus(function(response) {
            onStatus(response); // once on page load
            FB.Event.subscribe('auth.statusChange', onStatus); // every status change
        });
 
}
/**
* This will be called once on page load, and every time the status changes.
*/
function onStatus(response) {
    console.info('onStatus', response);
    if (response.status === 'connected') {
    	var uid = response.authResponse.userID;
    	console.info('uid = '+uid);
    	 if (document.getElementById('fbtoken') != null) {
         	document.getElementById('fbtoken').value = response.authResponse.accessToken;
         }
    	showAccountInfo(uid);
    } else {
    	showLoginButton()
    }
}
/**
* This assumes the user is logged out, and renders a login button.
*/
function showLoginButton() {
    var button = '<fb:login-button perms="email,user_birthday" />';
    document.getElementById('fb-login-button').innerHTML = button;
    FB.XFBML.parse(document.getElementById('fb-login-button'));
}
function showAccountInfo(uid) {
	document.getElementById('fbtoken').value = response.authResponse.accessToken;
	document.getElementById('fb-login-form').submit();	
}
handleFacebook();
showLoginButton();
</script>
</c:if>