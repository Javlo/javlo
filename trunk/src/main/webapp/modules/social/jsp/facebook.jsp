<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<form class="standard-form ajax" action="${info.currentURL}" method="post">
	<fieldset>
		<legend>${network.name}</legend>
		<input type="hidden" name="webaction" value="updateNetwork" />
		<input type="hidden" name="name" value="${network.name}" />
		<div class="line">
			<label for="login">login</label>
			<input type="text" name="login" id="login" value="${network.login}" />
		</div>
		<div class="line">
			<label for="token">token</label>
			<input type="text" name="token" id="token" value="${network.token}" />
		</div>
		<div class="line">
			<label for="url">url</label>
			<input type="text" name="url" id="url" value="${network.URL}" />
		</div>		
		<div class="line" style="display:none;" id="loginFB">
			<label for="fb">activation</label>
			<input id="fb" type="button" onclick="goLogin()" value="Facebook login" />
		</div>	
		<div class="action">			
			<input type="submit" name="ok" value="${i18n.view["global.ok"]}" />
			<div style="display:none;" id="loginFB">
			<input type="button" onclick="goLogin()" value="Facebook login" />
			</div>
		</div>			
	</fieldset>
</form>


<div id="fb-root"></div>
<script language="javascript">
 // Load the SDK Asynchronously 
  (function(d){
	 var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
	 if (d.getElementById(id)) {return;}
	 js = d.createElement('script'); js.id = id; js.async = true;
	 js.src = "//connect.facebook.net/fr_FR/all.js";
	 ref.parentNode.insertBefore(js, ref);
   }(document));

  // Init the SDK upon load
  window.fbAsyncInit = function() {
	FB.init({
	  appId      : '537650426253495', // App ID
	  //channelUrl : '//'+window.location.hostname+'/channel.php', // Path to your Channel File
	  status     : true, // check login status
	  cookie     : true, // enable cookies to allow the server to access the session
	  xfbml      : true  // parse XFBML
	});
	
	FB.getLoginStatus(function(response) {
	  if (response.status === 'connected') {
		isLoggued(response);
	  } else {
		notLoggued();
	  }
	 });

	// listen for and handle auth.statusChange events
	FB.Event.subscribe('auth.statusChange', function(response) {
	  if (response.authResponse) {
		isLoggued(response);
	  } else {
		// user has not auth'd your app, or is not logged into Facebook
		notLoggued();
	  }
	});
  } 
  
  function goLogin() {
		FB.login(function(response) {
		   // handle the response
		 }, {scope: 'manage_pages,offline_access'});
	}

	function notLoggued() { 
		document.getElementById('loginFB').style.display='block';		
	}
	
	function isLoggued(response) {
		var uid = response.authResponse.userID;
		var accessToken = response.authResponse.accessToken;
		document.getElementById('token').value = accessToken;
		// enregistre en ajax les data
		// appel la liste des pages générées l'utilisateur		
		// la liste des pages
		// https://graph.facebook.com/me/account?access_token=HIUGIUIU
		// ensuite, avec le token de la page:
		// https://graph.facebook.com/idpage/feed?access_token=HIUHUIHHUI
		
	}
	
	
</script>

