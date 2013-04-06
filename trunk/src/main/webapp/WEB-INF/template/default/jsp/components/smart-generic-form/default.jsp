<div class="smart-form">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
	<c:if test="${not empty msg}">	<div class="message">
		<div class="${msg.typeLabel}"><p>${msg.message}</p></div>
	</div>	</c:if>	<c:if test="${empty valid}">
<h3>${comp.title}</h3>
<form method="post" action="${info.currentURL}" ${comp.file?' enctype="multipart/form-data"':''}>
<input type="hidden" name="webaction" value="gform.submit" />
<input type="hidden" name="comp_id" value="${comp.id}" />
<c:forEach var="field" items="${comp.fields}">

	<div class="line ${field.type}${not empty errorFields[field.name]?' error':''}">
	<c:choose>
		<c:when test="${field.type eq 'text' || field.type eq 'email'}">
			<label for="${field.name}">${field.label} ${field.require?'*':''}</label>
			<input type="text" name="${field.name}" value="${requestService.parameterMap[field.name]}" />
		</c:when>
		<c:when test="${field.type eq 'large-text'}">
			<label for="${field.name}">${field.label} ${field.require?'*':''}</label>
			<textarea name="${field.name}">${requestService.parameterMap[field.name]}</textarea>
		</c:when>
		<c:when test="${field.type eq 'yes-no'}">
			<label for="${field.name}">${field.label} ${field.require?'*':''}</label>
			<input type="radio" name="${field.name}" value="yes" id="${field.name}-yes" /><label class="line-label" for="${field.name}-yes">${i18n.view["global.yes"]}</label>
			<input type="radio" name="${field.name}" value="no" id="${field.name}-no" /><label class="line-label" for="${field.name}-no">${i18n.view["global.no"]}</label>
		</c:when>	
		<c:when test="${field.type eq 'file'}">
			<label for="${field.name}">${field.label} ${field.require?'*':''}</label>
			<input type="file" name="${field.name}" />
		</c:when>	
		<c:when test="${field.type eq 'list'}">
			<label for="${field.name}">${field.label} ${field.require?'*':''}</label>
			<select name="${field.name}">
				<c:forEach var="item" items="${field.list}">
					<option${requestService.parameterMap[field.name] = item?' selected="selected":''}>${item}</option> 
				</c:forEach>
			</select>
		</c:when>		
	</c:choose>
	</div>

</c:forEach>

<c:if test="${comp.captcha}">
<div class="line captcha">			
	<div class="${not empty requestService.parameterMap.webaction && empty requestService.parameterMap.captcha || not empty error_captcha?'error':''}">
		<label for="captcha"><img src="${info.captchaURL}" alt="captcha" /></label>
		<label for="captcha">${ci18n['label.captcha']}*</label>
		<input type="text" id="captcha" name="captcha" value="" />
	</div>
</div>
</c:if>

<div class="line action">
	<input type="submit" value="${i18n.view['global.send']}"/>
</div>
</form>
</c:if>
</div>