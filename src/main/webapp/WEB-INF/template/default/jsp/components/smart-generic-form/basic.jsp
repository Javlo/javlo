<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><input type="hidden" name="webaction" value="smart-form.submit" />
<input type="hidden" name="comp_id" value="${comp.id}" />
<input type="hidden" name="_form-code" value="${formCode}" />
<c:if test="${comp.captcha && not empty comp.recaptchaKey && not empty comp.recaptchaSecretKey}"><script src='https://www.google.com/recaptcha/api.js?hl=${info.requestContentLanguage}'></script></c:if>