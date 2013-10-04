<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${empty webaction}">
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
</c:if>

<div class="widgetbox edit-user">
<c:if test="${empty webaction}"><h3><span>${i18n.edit['user.title.edit']} : ${user.name}</span></h3></c:if>
<div class="content">
<c:if test="${not empty messages.globalMessage && not empty webaction}">
<div class="message ${messages.globalMessage.typeLabel}">
	<span>${messages.globalMessage.message}</span>
</div>
</c:if>


<form id="form-edit-user" class="standard-form" action="${info.currentURL}" method="post">

<div>
	<c:if test="${empty webaction}"><input type="hidden" name="webaction" value="updateCurrent" /></c:if>
	<c:if test="${not empty webaction}"><input type="hidden" name="webaction" value="${webaction}" /></c:if>
	<input type="hidden" name="user" value="${user.name}" />
</div>

<fieldset>
<legend>${i18n.edit['user.info']}</legend>
<div class="cols">
<div class="one_half">
	<div class="line">
		<label for="login">login</label>
		<input type="text" id="login" name="login" value="${userInfoMap["login"]}" /> 
	</div>
	<c:if test="${not empty webaction}">
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
</div>
</div>
</fieldset>

<div class="action">
	<input type="submit" name="back" value="${i18n.edit['global.back']}" />
	<input type="submit" name="ok" value="${i18n.edit['global.ok']}" />
</div>

</form>

</div>
</div>

