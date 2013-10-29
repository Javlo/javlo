<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${not empty messages.globalMessage}">
<div class="message ${messages.globalMessage.typeLabel}">
	<span>${messages.globalMessage.message}</span>
</div>
</c:if>

<c:set var="basketPage" value="${info.pageByName.basket}" />
<c:if test="${not empty basketPage && empty param.basketLink}"><a class="back-basket" href="${basketPage.url}">${i18n.view['ecom.back-basket']}</a></c:if>


<c:if test="${empty param.pwkey}">

<c:if test="${not empty user}">
<form id="user-logout" class="standard-form" action="${info.currentURL}" method="post">
<div>
	<div class="one_half">
		<c:set var="displayName" value="${user.userInfo.firstName} ${user.userInfo.lastName}" />
		<h3>${i18n.view['user.welcome']}, <span>${empty user.userInfo.firstName && empty user.userInfo.lastName?user.name:displayName}</span></h3>		
	</div>
	<div class="one_half last">
	<c:if test="${!user.userInfo.externalLoginUser}">	
		<input type="hidden" name="${info.staticData.compId}" value="${comp.id}" />
		<input type="hidden" name="webaction" value="user-registration.logout" />
		<input type="submit" name="logout" value="${i18n.view['form.logout']}" />	
	</c:if>
	<c:if test="${user.userInfo.externalLoginUser}">
		<a class="facebook-app" href="https://www.facebook.com/bookmarks/apps">facebook</a>
	</c:if>
	</div>
</div>
</form>
</c:if>

<c:if test="${empty user}">
<c:if test="${empty user}"><jsp:include page="fb_login.jsp" /></c:if>
<jsp:include page="${info.rootTemplateFolder}/jsp/login.jsp" />
<c:if test="${empty user}">
<a id="forget-password-id" href="#" onclick="javascript:hideshow(document.getElementById('forget-password-id'));javascript:hideshow(document.getElementById('reset-password-with-email'));return false;">${i18n.view['user.message.forget-password']}</a>
<div id="reset-password-with-email" class="reset-password">
	<form name="reset-password" method="post">
	<fieldset>
	<legend>${i18n.view['user.reset-password']}</legend>
	<input type="hidden" name="webaction" value="user-registration.resetPasswordWithEmail" />
	<div class="cols">
	<div class="one_half">
	<div class="line">
		<label for="login">${i18n.view['form.email']}</label>
	    <div class="input"><input id="reset-email" type="text" name="email" value="" /></div>
	</div>
	</div>
	</div>
	<div class="action">
		<input type="submit" value="${i18n.view['form.submit']}" />
	</div>	
	</fieldset>
	</form>
</div>
<script type="text/javascript">
function hideshow(which){
if (!document.getElementById)
return
if (which.style.display=="block")
which.style.display="none"
else
which.style.display="block"
}
document.getElementById('reset-password-with-email').style.display="none";
document.getElementById('forget-password-id').style.display="block";
</script>
</c:if>

</c:if>

<div class="widgetbox edit-user">
<c:if test="${empty noform}">
<form id="form-edit-user" class="standard-form" action="${info.currentURL}" method="post">

<div>
	<c:if test="${empty user}"><input type="hidden" name="webaction" value="user-registration.register" /></c:if>	
	<c:if test="${not empty user}"><input type="hidden" name="webaction" value="user-registration.update" /></c:if>
	<input type="hidden" name="user" value="${user.name}" />
	<input type="hidden" name="${info.staticData.compId}" value="${comp.id}" />
</div>

<fieldset>
<c:if test="${not empty user}">
<legend>${i18n.view['user.update']}</legend>
</c:if>
<c:if test="${empty user}">
<legend>${i18n.view['user.register']}</legend>
</c:if>
<div class="cols">
<div class="one_half">
	<div class="line">
		<label for="email-login">${i18n.view['form.email']}</label>
		<input type="text" id="email-login" name="email-login" value="${userInfoMap["email"]}" /> 
	</div>
	<c:if test="${empty user}">
		<div class="line">
			<label for="password">${i18n.view['form.password']}</label>
			<input type="password" id="password" name="password" value="" /> 
		</div>	
		<div class="line">
			<label for="password2">${i18n.view['form.password2']}</label>
			<input type="password" id="password2" name="password2" value="" />		 
		</div>
	</c:if>
	<div class="line">
		<label for="firstName">${i18n.view['form.firstName']}</label>
		<input type="text" id="firstName" name="firstName" value="${userInfoMap["firstName"]}" /> 
	</div>
	<div class="line">
		<label for="lastName">${i18n.view['form.lastName']}</label>
		<input type="text" id="lastName" name="lastName" value="${userInfoMap["lastName"]}" /> 
	</div>		
	<div class="line">
		<label for="mobile">${i18n.view['form.adress.phone']}</label>
		<input type="text" id="mobile" name="mobile" value="${userInfoMap["mobile"]}" /> 
	</div>
</div>
<div class="one_half">
	<div class="line">
		<label for="address">${i18n.view['form.adress']}</label>
		<input type="text" id="address" name="address" value="${userInfoMap["address"]}" /> 
	</div>
	<div class="line">
		<label for="postCode">${i18n.view['form.adress.zip']}</label>
		<input type="text" id="postCode" name="postCode" value="${userInfoMap["postCode"]}" /> 
	</div>
	<div class="line">
		<label for="city">${i18n.view['form.adress.city']}</label>
		<input type="text" id="city" name="city" value="${userInfoMap["city"]}" /> 
	</div>
	<div class="line">
		<label for="country">${i18n.view['form.adress.country']}</label>
		<c:if test="${empty list.countries}">
			<input type="text" id="country" name="country" value="${userInfoMap["country"]}" />
		</c:if>
		 <c:if test="${not empty list.countries}">
		 	<select id="country" name="country">
		 		<option></option>
		 		<c:forEach var="country" items="${list.countries}">
		 			<option value="${country.key}"${userInfoMap["country"] == country.key?' selected="selected"':''}>${country.value}</option>
		 		</c:forEach>		 		
		 	</select>
		 </c:if>
	</div>
	<div class="line">
		<label for="info">info</label>
		<textarea id="info" name="info">${userInfoMap["info"]}</textarea>
	</div>
</div>
</div>
</fieldset>

<div class="action">
	<input type="submit" name="back" value="${i18n.edit['global.back']}" />
	<input type="submit" name="ok" value="${i18n.edit['global.ok']}" />
</div>

</form>

</c:if>

</c:if>

<c:if test="${not empty user || not empty param.pwkey}">

<div class="widgetbox edit-user">
<h3><span>${i18n.view['user.change-password']}</span></h3>
<div class="content">
<form id="form-edit-user" class="standard-form" action="${info.currentURL}" method="post">
<div>
	<input type="hidden" name="webaction" value="user-registration.changePassword" />
	<input type="hidden" name="user" value="${user.name}" />
</div>

<c:if test="${empty param.pwkey}">
<div class="cols">
<div class="one_half">
<div class="line">
	<label for="password">${i18n.view['user.current-password']}</label>
	<input type="password" id="password" name="password" value="" /> 
</div>
</div><div class="one_half last">&nbsp;</div>
</div>
</c:if>
<c:if test="${not empty param.pwkey}">
	<input type="hidden" id="pwkey" name="pwkey" value="${param.pwkey}" />
</c:if>
<div class="one_half">
<div class="line">
	<label for="newpassword1">${i18n.view['user.new-password']}</label>
	<input type="password" id="newpassword1" name="newpassword1" value="" />
</div>
</div><div class="one_half last">
<div class="line">
	<label for="newpassword2">${i18n.view['user.new-password-confirm']}</label>
	<input type="password" id="newpassword2" name="newpassword2" value="" />
</div>
	<div class="action">		
		<input type="submit" name="ok" value="${i18n.view['global.ok']}" />	
	</div> 

</div>
</form>
</div>
</div>
</c:if>

</div>


