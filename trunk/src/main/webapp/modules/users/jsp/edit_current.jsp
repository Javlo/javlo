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
			<label for="password">${i18n.edit['user.new-password']}</label>
			<input type="password" id="password" name="password" value="" /> 
		</div>	
		<div class="line">
			<label for="password2">${i18n.edit['user.change-password-2']}</label>
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
		<label for="organization">organization</label>
		<c:if test="${empty list.organization}">
		<input type="text" id="organization" name="organization" value="${userInfoMap["organization"]}" />
		</c:if> 
		<c:if test="${not empty list.organization}">
		 	<select id="organization" name="organization">
		 		<option></option>
		 		<c:forEach var="organization" items="${list.organization}">
		 			<option value="${organization.key}"${userInfoMap["organization"] == organization.key?' selected="selected"':''}>${organization.value}</option>
		 		</c:forEach>		 		
		 	</select>
		 </c:if> 
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
		<label for="preferredLanguageRaw">preferred Language</label>
		<input type="text" id="preferredLanguageRaw" name="preferredLanguageRaw" value="${userInfoMap["preferredLanguageRaw"]}" /> 
	</div>
	<c:if test="${empty webaction}">
	<div class="line">
		<label for="token">token</label>
		<c:if test="${fn:length(userInfoMap['token']) > 0}">
			<a href="${info.homeAbsoluteURL}?j_token=${userInfoMap['token']}">
			<span id="token" class="value">${userInfoMap['token']}</span>
			</a>
		</c:if>
		<input class="action-button" type="submit" name="token" value="${i18n.edit['global.reset']}" /> 
		<c:if test="${fn:length(userInfoMap['token']) > 0}">
			<input class="action-button" type="submit" name="notoken" value="${i18n.edit['global.delete']}" />
		</c:if>
	</div>
	</c:if>
	<div class="line">
		<label for="info">info</label>
		<textarea id="info" name="info">${userInfoMap["info"]}</textarea>
	</div>
</div>
</div>
<div class="line">
		<label for="function">area of specialisation</label>
		<c:if test="${empty list.functions}">
		<input type="text" id="function" name="function" value="${userInfoMap["function"]}" />
		</c:if>
		 <c:if test="${not empty list.functions}">		 	
	 		<c:forEach var="function" items="${list.functions}" varStatus="status">
	 		<div class="inline-line">
	 			<input type="checkbox" name="function" id="function-${status.index}" value="${function.key}" ${not empty functions[function.key]?' checked="checked"':''}/><label class="suffix" for="function-${status.index}">${function.value}</label>
			</div>	 			
	 		</c:forEach>	 		
		 </c:if> 
	</div>
</fieldset>

<c:if test="${not empty webaction}">
	<div class="line">
		<label for="message">you message to the administrator of the site :</label>
		<textarea id="info" name="message">${param.message}</textarea>
	</div>
</c:if>


<fieldset>
<legend>${i18n.edit['user.social']}</legend>
	<div class="one_half">
		<div class="line">
			<label for="facebook">facebook</label>
			<input type="text" id="facebook" name="facebook" value="${userInfoMap["facebook"]}" /> 
		</div>
		<div class="line">
			<label for="googleplus">google+</label>
			<input type="text" id="googleplus" name="googleplus" value="${userInfoMap["googleplus"]}" /> 
		</div>
	</div>
	<div class="one_half">
	<div class="line">
		<label for=linkedin">linkedin</label>
		<input type="text" id="linkedin" name="linkedin" value="${userInfoMap["linkedin"]}" /> 
	</div>
	<div class="line">
		<label for=twitter">twitter</label>
		<input type="text" id="twitter" name="twitter" value="${userInfoMap["twitter"]}" /> 
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

