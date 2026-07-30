<%@ taglib uri="jakarta.tags.core" prefix="c" %>
	<c:if test="${not empty msg}">	<div class="message">
		<div class="${msg.typeLabel}"><p>${msg.message}</p></div>
	</div>	</c:if>	<c:if test="${empty valid}">
<form method="post" action="${info.currentURL}">
<input type="hidden" name="webaction" value="gform.submit" />
<input type="hidden" name="comp_id" value="${comp.id}" />
<div class="line">
	<label for="name">${ci18n['field.name']}</label>
	<input id="name" name="name" value="<c:out value="${param.name}" />" />
</div>
<div class="line">
	<label for="firstname">${ci18n['field.firstname']}</label>
	<input id="firstname" name="firstname" value="<c:out value="${param.firstname}" />" />
</div>
<div class="line${not empty errorFiles['Email']?' error':''}">
	<label for="email">${ci18n['field.email']}*</label>
	<input id="email" name="Email" value="<c:out value="${param.email}" />" />
</div>
<div class="line">
	<label for="company">${ci18n['field.company']}</label>
	<input id="company" name="company" value="<c:out value="${param.company}" />" />
</div>
<div class="line">
	<label for="message">${ci18n['field.message']}</label>
	<textarea id="message" name="message"><c:out value="${param.message}" /></textarea>
</div>

<fieldset>				
	<legend>${ci18n['field.captcha']}*</legend>			
	<div class="captcha ${not empty param.webaction && empty param.captcha || not empty error_captcha?'error':''}">
	<label for="captcha"><img src="${info.captchaURL}" alt="captcha" /></label>
		<input type="text" id="captcha" name="captcha" value="" />
	</div>
</fieldset>	

<div class="line action">
	<input type="submit" />
</div>

</form>
</c:if>