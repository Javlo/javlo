<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%>
<c:if test="${field.first}">
	<div class="col-line">
</c:if>
<div class="col${field.width}${field.last?' lastcol':''}${field.first?' firstcol':''}">
	<div class="line form-group ${field.type}${not empty errorFields[field.name]?' error':''}">
		<c:set var="requireHTML"><abbr title="${ci18n['message.required']}" class="require">*</abbr></c:set>
		<c:choose>	
			<c:when test="${field.type eq 'text' || field.type eq 'email'}">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<input class="form-control" type="text" name="${field.name}" id="${field.name}" value="${requestService.parameterMap[field.name]}">
			</c:when>
			<c:when test="${field.type eq 'large-text'}">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<textarea class="form-control" name="${field.name}" id="${field.name}">${requestService.parameterMap[field.name]}</textarea>
			</c:when>
			<c:when test="${field.type eq 'number'}">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<input class="form-control" type="number" name="${field.name}" id="${field.name}" value="${requestService.parameterMap[field.name]}">
			</c:when>
			<c:when test="${field.type eq 'yes-no'}">
				<div class="radio">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<input type="radio" name="${field.name}" value="yes" id="${field.name}-yes"${requestService.parameterMap[field.name] eq 'yes'?' checked="checked"':''}><label class="line-label" for="${field.name}-yes">${i18n.view["global.yes"]}</label>
				<input type="radio" name="${field.name}" value="no" id="${field.name}-no"${requestService.parameterMap[field.name] eq 'no'?' checked="checked"':''}><label class="line-label" for="${field.name}-no">${i18n.view["global.no"]}</label>
				</div>
			</c:when>	
			<c:when test="${field.type eq 'file'}">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<input type="file" name="${field.name}" id="${field.name}">
			</c:when>	
			<c:when test="${field.type eq 'radio'}">
				<div class="field-label">
					<label>${field.label} ${field.require?requireHTML:''}</label>
				</div>
				<div class="field-control form-control">
					<c:forEach var="item" items="${field.list}" varStatus="iter"> 
						<div class="field-item">
							<input type="radio" name="${field.name}" value="${item}" id="${field.name}-${iter.index}"${requestService.parameterMap[field.name] eq item ? ' checked="checked"' : ''}>
							<label class="line-label" for="${field.name}-${iter.index}">${item}</label>
						</div>
					</c:forEach>
				</div>
			</c:when>		
			<c:when test="${field.type eq 'list'}">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<select class="form-control" name="${field.name}" id="${field.name}">
					<c:forEach var="item" items="${field.list}"> 
						<option${requestService.parameterMap[field.name] eq item?' selected="selected"':''}>${item}</option> 
					</c:forEach>
				</select>
			</c:when>		
			<c:when test="${field.type eq 'registered-list'}">
				<label for="${field.name}">${field.label} ${field.require?requireHTML:''}</label>
				<select name="${field.name}" id="${field.name}" class="form-control">
					<c:forEach var="item" items="${list[field.registeredList]}"> 
						<option${requestService.parameterMap[field.name] eq item?' selected="selected"':''} value="${item.key}">${item.value}</option> 
					</c:forEach>
				</select>
			</c:when>
			<c:when test="${field.type eq 'validation'}">
				<div class="checkbox">				
					<label for="${field.name}">
					<input type="checkbox" name="${field.name}" id="${field.name}" ${not empty requestService.parameterMap[field.name]?'checked="checked"':''}>
					${field.label} ${field.require?requireHTML:''}</label>
				</div>
			</c:when>
		</c:choose>
	</div>
</div>
<c:if test="${field.last}">
	</div> <!-- /col-line  -->
</c:if>