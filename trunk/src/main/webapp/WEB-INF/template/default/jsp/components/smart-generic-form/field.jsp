<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%>
<div class="col${field.width}${field.last?' lastcol':''}${field.first?' firstcol':''}">
	<div class="line ${field.type}${not empty errorFields[field.name]?' error':''}">
		<c:set var="requireHTML"><abbr title="${ci18n['message.required']}" class="require">*</abbr></c:set>
		<c:choose>	
			<c:when test="${field.type eq 'text' || field.type eq 'email'}">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<input type="text" name="${field.name}" value="${requestService.parameterMap[field.name]}" />
			</c:when>
			<c:when test="${field.type eq 'large-text'}">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<textarea name="${field.name}">${requestService.parameterMap[field.name]}</textarea>
			</c:when>
			<c:when test="${field.type eq 'yes-no'}">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<input type="radio" name="${field.name}" value="yes" id="${field.name}-yes"${requestService.parameterMap[field.name] eq 'yes'?' checked="checked"':''} /><label class="line-label" for="${field.name}-yes">${i18n.view["global.yes"]}</label>
				<input type="radio" name="${field.name}" value="no" id="${field.name}-no"${requestService.parameterMap[field.name] eq 'no'?' checked="checked"':''} /><label class="line-label" for="${field.name}-no">${i18n.view["global.no"]}</label>
			</c:when>	
			<c:when test="${field.type eq 'file'}">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<input type="file" name="${field.name}" />
			</c:when>	
			<c:when test="${field.type eq 'list'}">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<select name="${field.name}">
					<c:forEach var="item" items="${field.list}"> 
						<option${requestService.parameterMap[field.name] eq item?' selected="selected"':''}>${item}</option> 
					</c:forEach>
				</select>
			</c:when>		
			<c:when test="${field.type eq 'registered-list'}">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<select name="${field.name}">
					<c:forEach var="item" items="${list[field.registeredList]}"> 
						<option${requestService.parameterMap[field.name] eq item?' selected="selected"':''} value="${item.key}">${item.value}</option> 
					</c:forEach>
				</select>
			</c:when>
		</c:choose>
	</div>
</div>