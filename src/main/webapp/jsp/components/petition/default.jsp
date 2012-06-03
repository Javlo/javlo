<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${not empty msg}">
	<div class="message">
		<div class="${msg.typeLabel}"><p>${msg.message}</p></div>
	</div>
</c:if>
<c:if test="${empty valid}">
<form action="${info.currentURL}" method="post">
	<div>
		<input type="hidden" name="webaction" value="petition.sign" />
		<input type="hidden" name="comp_id" value="${comp.id}" />
	</div>
	
	
	
	<fieldset>
	<legend>${pi18n['fields.user']}</legend>
	<div class="line ${not empty param.webaction && empty param.firstname?'error':''}">
		<label for="firstname">${pi18n['field.firstname']}*</label>
		<input type="text" id="firstname" name="firstname" value="${param.firstname}" />
	</div>
	<div class="line ${not empty param.webaction && empty param.lastname?'error':''}">
		<label for="lastname">${pi18n['field.lastname']}*</label>
		<input type="text" id="firstname" name="lastname" value="${param.lastname}" />
	</div>
	<div class="line  ${not empty param.webaction && empty param.email || not empty error_email?'error':''}">
		<label for="email">${pi18n['field.email']}*</label>
		<input type="text" id="email" name="email" value="${param.email}" />
	</div>
	<div class="line ${not empty param.webaction && empty param.country?'error':''}">
		<label for="country">${pi18n['field.country']}*</label>
		<select id="country" name="country">
		<option value=""></option>
		<c:forEach var="country" items="${i18n.countries}" varStatus="status">
			<option value="${country.key}" ${country.key eq param.country?'selected="selected"':''}>${country.value}</option>
		</c:forEach>
		</select>
	</div>	
	<div class="line">
		<label for="organization">${pi18n['field.organization']}</label>
		<input type="text" id="organization" name="organization" value="${param.organization}" />
	</div>	
	</fieldset>
	
	<fieldset>
	<legend>${pi18n['field.comment']}</legend>
	<textarea class="large" name="comment">${param.comment}</textarea>
	</fieldset>
	
	<fieldset>
	<legend>${pi18n['field.captcha']}*</legend>
	<div class="captcha ${not empty param.webaction && empty param.captcha || not empty error_captcha?'error':''}">
		<label for="captcha"><img src="${info.captchaURL}" alt="captcha" /></label>
		<input type="text" id="captcha" name="captcha" value="" />
	</div>
	</fieldset>
	<div class="action">
		<input type="submit" name="sign" value="${pi18n['action.sign']}" />
	</div>	
</form>
</c:if>