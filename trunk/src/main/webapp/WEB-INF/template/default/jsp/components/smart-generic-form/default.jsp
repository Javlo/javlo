<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><div class="smart-form">
	<c:if test="${not empty msg}">	<div class="message">
		<div class="${msg.typeLabel}"><p>${msg.message}</p></div>
	</div>	</c:if>	<c:if test="${empty valid}">
<h3>${comp.title}</h3>
<form method="post" action="${info.currentURL}" ${comp.file?' enctype="multipart/form-data"':''}>
<input type="hidden" name="webaction" value="smart-form.submit" />
<input type="hidden" name="comp_id" value="${comp.id}" />

<c:forEach var="localField" items="${comp.fields}">
	<c:set var="field" value="${localField}" scope="request" />
	<jsp:include page="field.jsp" />
</c:forEach>

<c:if test="${comp.captcha}">
<div class="line captcha">			
	<div class="${not empty requestService.parameterMap.webaction && empty requestService.parameterMap.captcha || not empty error_captcha?'error':''}">
		<label for="captcha"><img src="${info.captchaURL}" alt="captcha" /></label>		
		<label for="captcha">${ci18n['label.captcha']}<abbr title="${ci18n['message.required']}" class="require">*</abbr></label>
		<input type="text" id="captcha" name="captcha" value="" />
	</div>
</div>
</c:if>

<div class="line action">
	<a href="${info.currentURL}" class="input">${ci18n['message.reset']}</a>
	<input type="submit" value="${i18n.view['global.send']}"/>
</div>
</form>
</c:if>
</div>