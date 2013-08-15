<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
<div class="template-preview">
	<img src="${currentTemplate.previewUrl}" alt="${currentTemplate.name}" />	
</div>

<form id="form-edit-template" action="${info.currentURL}" class="standard-form" method="post">

	<div>
		<input type="hidden" name="webaction" value="editTemplate" />
		<input type="hidden" name="name" value="${currentTemplate.name}" />		
	</div>
	
	<div class="properties">
	
		<div class="one_half">
		
			<div class="line">
				<label for="author">${i18n.edit['global.author']}</label>
				<input type="text" id="author" name="author" value="${currentTemplate.authors}" />
			</div>	
			
			<div class="line">
				<label for="parent">${i18n.edit['template.label.parent']}</label>
				<input type="text" id="parent" name="parent" value="${currentTemplate.parent}" />
			</div>	
			
		</div><div class="one_half">
		
			<div class="line">
				<label for="imageFilter">${i18n.edit['template.label.image-filter']}</label>
				<input type="text" id="imageFilter" name="imageFilter" value="${currentTemplate.imageFilter}" />
			</div>	
			
			<div class="line">
				<label for="date">${i18n.edit['template.creation-date']}</label>
				<input type="text" id="date" class="datepicker" name="date" value="${currentTemplate.creationDate}" />
			</div>
	
		</div>
	
	</div>
	
	<fieldset>
	<legend>${i18n.edit['edit.title.area']}</legend>
	
	<c:forEach var="area" items="${currentTemplate.areas}">
	<div class="inline">
		<h4>${area}</h4><span class="delete-area"><a href="${info.currentURL}?webaction=deleteArea&area=${area}&templateid=${currentTemplate.id}" title="${i18n.edit['global.delete']}">${i18n.edit['global.delete']}</a></span>
		<ul>
			<c:set var="areaFound" value="false" scope="page" />			
			<c:forEach var="htmlID" items="${currentTemplate.HTMLIDS}">
				<c:if test="${currentTemplate.areasMap[area] eq htmlID}">
					<c:set var="areaFound" value="true" scope="page" />	
				</c:if>
				<li><input ${currentTemplate.areasMap[area] eq htmlID?'checked="checked"':''} type="radio" name="area-${area}" id="_${area}_${htmlID}" value="${htmlID}" /> <label class="suffix" for="_${area}_${htmlID}">${htmlID}</label></li>
			</c:forEach>
			<li><input type="text" name="free-area-${area}" value="${areaFound eq 'false'?currentTemplate.areasMap[area]:'' }" /></li>
			<c:set var="areaFound" value="false" scope="page" />			
		</ul>
	</div>
	</c:forEach>
	
	<div class="inline">
		<h4><input class="label-inside label" type="text" name="new-area" value="${i18n.edit['template.area.create']}..." /></h4>
		<ul>
			<c:set var="areaFound" value="false" scope="page" />			
			<c:forEach var="htmlID" items="${currentTemplate.HTMLIDS}">
				<c:if test="${currentTemplate.areasMap[area] eq htmlID}">
					<c:set var="areaFound" value="true" scope="page" />	
				</c:if>
				<li><input ${currentTemplate.areasMap[area] eq htmlID?'checked="checked"':''} type="radio" name="newarea-id" id="_new_${htmlID}" value="${htmlID}" /> <label class="suffix" for="_new_${htmlID}">${htmlID}</label></li>
			</c:forEach>
			<li><input type="text" name="free-area-new" value="${areaFound eq 'false'?currentTemplate.areasMap[area]:'' }" /></li>
			<c:set var="areaFound" value="false" scope="page" />			
		</ul>
	</div>
	
	</fieldset>
	
	<div class="action">
		<input type="submit" name="back" value="${i18n.edit['global.back']}" />
		<input type="submit" value="${i18n.edit['global.ok']}" />		
	</div>
</form>
</div>

