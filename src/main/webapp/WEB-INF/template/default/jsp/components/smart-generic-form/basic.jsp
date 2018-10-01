<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><input type="hidden" name="webaction" value="smart-form.submit" />
<input type="hidden" name="comp_id" value="${comp.id}" />
<input type="hidden" name="_form-code" value="${formCode}" /><c:if test="${editForm}">	
<input type="hidden" name="${inputLine}" value="${editLine}" /></c:if>
<c:if test="${not empty param.line}"><input type="hidden" name="line" value="${param.line}" /></c:if>
<c:if test="${comp.captcha && not empty comp.recaptchaKey && not empty comp.recaptchaSecretKey}"><script src='https://www.google.com/recaptcha/api.js?hl=${info.requestContentLanguage}'></script></c:if>

<c:if test="${info.template.config.message && not empty messages.globalMessage.message}">
		<div class="alert alert-${messages.globalMessage.bootstrapType}" role="alert">
			<span>${messages.globalMessage.message}</span>
		</div>
</c:if>