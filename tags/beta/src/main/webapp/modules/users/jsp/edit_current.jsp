<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="widgetbox edit-user">
<h3><span>${i18n.edit['user.change-password']}</span></h3>
<div class="content">
<form id="form-edit-user" class="standard-form" action="${info.currentURL}" method="post">
<div>
	<input type="hidden" name="webaction" value="changePassword" />
	<input type="hidden" name="user" value="${user.name}" />
</div>
<div class="one_half">
<div class="line">
	<label for="password">${i18n.edit['user.current-password']}</label>
	<input type="password" id="password" name="password" value="" /> 
</div>
</div><div class="one_half">
<div class="line">
	<label for="newpassword">${i18n.edit['user.new-password']}</label>
	<input type="password" id="newpassword" name="newpassword" value="" />
	<div class="action"><input type="submit" name="ok" value="${i18n.edit['global.ok']}" /></div> 
</div>
</div>
</form>
</div>
</div>

<div class="widgetbox edit-user">
<h3><span>${i18n.edit['user.title.edit']} : ${user.name}</span></h3>
<div class="content">

<form id="form-edit-user" class="standard-form" action="${info.currentURL}" method="post">

<div>
	<input type="hidden" name="webaction" value="updateCurrent" />
	<input type="hidden" name="user" value="${user.name}" />
</div>

<fieldset>
<legend>${i18n.edit['user.info']}</legend>
<div class="one_half">
	<div class="line">
		<label for="login">login</label>
		<input type="text" id="login" name="login" value="${userInfoMap["login"]}" /> 
	</div>
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
		<label for="organization">organization</label>
		<input type="text" id="organization" name="organization" value="${userInfoMap["organization"]}" /> 
	</div>
	<div class="line">
		<label for="function">function</label>
		<input type="text" id="function" name="function" value="${userInfoMap["function"]}" /> 
	</div>
	<div class="line">
		<label for="phone">phone</label>
		<input type="text" id="phone" name="phone" value="${userInfoMap["phone"]}" /> 
	</div>
	<div class="line">
		<label for="mobile">mobile</label>
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
		<input type="text" id="country" name="country" value="${userInfoMap["country"]}" /> 
	</div>
	<div class="line">
		<label for="preferredLanguageRaw">preferred Language</label>
		<input type="text" id="preferredLanguageRaw" name="preferredLanguageRaw" value="${userInfoMap["preferredLanguageRaw"]}" /> 
	</div>
	<div class="line">
		<label for="token">token</label>
		<c:if test="${fn:length(userInfoMap['token']) > 0}">
			<a href="${info.homeAbsoluteURL}?token=${userInfoMap['token']}">
			<span id="token" class="value">${userInfoMap['token']}</span>
			</a>
		</c:if>
		<input class="action-button" type="submit" name="token" value="${i18n.edit['global.reset']}" /> 
		<c:if test="${fn:length(userInfoMap['token']) > 0}">
			<input class="action-button" type="submit" name="notoken" value="${i18n.edit['global.delete']}" />
		</c:if>
	</div>
	<div class="line">
		<label for="info">info</label>
		<textarea id="info" name="info">${userInfoMap["info"]}</textarea>
	</div>
</div>
</fieldset>

<fieldset>
<legend>${i18n.edit['user.social']}</legend>
	<div class="line">
		<label for="facebook">facebook</label>
		<input type="text" id="facebook" name="facebook" value="${userInfoMap["facebook"]}" /> 
	</div>
	<div class="line">
		<label for="googleplus">google+</label>
		<input type="text" id="googleplus" name="googleplus" value="${userInfoMap["googleplus"]}" /> 
	</div>
	<div class="line">
		<label for=linkedin">linkedin</label>
		<input type="text" id="linkedin" name="linkedin" value="${userInfoMap["linkedin"]}" /> 
	</div>
</fieldset>

<div class="action">
	<input type="submit" name="back" value="${i18n.edit['global.back']}" />
	<input type="submit" name="ok" value="${i18n.edit['global.ok']}" />
</div>

</form>

</div>
</div>

