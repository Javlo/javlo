<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${not empty messages.globalMessage}">
<div class="message ${messages.globalMessage.typeLabel}">
	<span>${messages.globalMessage.message}</span>
</div>
</c:if>

<c:if test="${empty user}">
<div class="login">	
	<form name="login" method="post">
	<fieldset>
	<legend>${i18n.view['user.login']}</legend>
	<div class="one_half">
	<div class="line">
		<label for="login">${i18n.view['form.login']}</label>
	    <div class="input"><input id="login" type="text" name="j_username" value="" /></div>
	</div>
	</div><div class="one_half last">
	<div class="line">
		<label for="password">${i18n.view['form.password']} : </label>
		<div class="input"><input id="password" type="password" name="j_password" value="" /></div>
	</div>		
	</div>
	<div class="action">
	<input type="submit" value="${i18n.view['form.submit']}" />
	</div>
	</fieldset>
	</form>
</div>
</c:if>

<c:if test="${not empty user}">
<form id="user-logout" class="standard-form" action="${info.currentURL}" method="post">
<div>
	<div class="one_half"><h3>${i18n.view['user.welcome']}, <span>${user.name}</span></h3></div>
	<div class="one_half last">
	<input type="hidden" name="${info.staticData.compId}" value="${comp.id}" />
	<input type="hidden" name="webaction" value="user-registration.logout" />
	<input type="submit" name="logout" value="${i18n.view['form.logout']}" />
	</div>
</div>
</form>
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
		<label for="login">login</label>
		<input type="text" id="login" name="login" value="${userInfoMap["login"]}" /> 
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
		<label for="firstName">firstName</label>
		<input type="text" id="firstName" name="firstName" value="${userInfoMap["firstName"]}" /> 
	</div>
	<div class="line">
		<label for="lastName">lastName</label>
		<input type="text" id="lastName" name="lastName" value="${userInfoMap["lastName"]}" /> 
	</div>
	<div class="line">
		<label for="email">email</label>
		<input type="text" id="email" name="email" value="${userInfoMap["email"]}" /> 
	</div>	
	<div class="line">
		<label for="mobile">mobile/phone</label>
		<input type="text" id="mobile" name="mobile" value="${userInfoMap["mobile"]}" /> 
	</div>
</div>
<div class="one_half">
	<div class="line">
		<label for="address">address</label>
		<input type="text" id="address" name="address" value="${userInfoMap["address"]}" /> 
	</div>
	<div class="line">
		<label for="postCode">postCode</label>
		<input type="text" id="postCode" name="postCode" value="${userInfoMap["postCode"]}" /> 
	</div>
	<div class="line">
		<label for="city">city</label>
		<input type="text" id="city" name="city" value="${userInfoMap["city"]}" /> 
	</div>
	<div class="line">
		<label for="country">country</label>
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

<c:if test="${not empty user}">

<div class="widgetbox edit-user">
<h3><span>${i18n.view['user.change-password']}</span></h3>
<div class="content">
<form id="form-edit-user" class="standard-form" action="${info.currentURL}" method="post">
<div>
	<input type="hidden" name="webaction" value="user-registration.changePassword" />
	<input type="hidden" name="user" value="${user.name}" />
</div>

<div class="line">
	<label for="password">${i18n.view['user.current-password']}</label>
	<input type="password" id="password" name="password" value="" /> 
</div>
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


