<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">

<form id="form-properties-template" action="${info.currentURL}" class="standard-form js-change-submit" method="post">
	
	<div>
		<input type="hidden" name="webaction" value="updateFilter" />
		<input type="hidden" name="templateid" value="${currentTemplate.name}" />		
	</div>
	
	<div class="line">
		<label for="filter">${i18n.edit['template.label.filter']}</label>
		<select id="filter" name="filter">
		<option value="">${i18n.edit['template.filter.choose']}</option>
		<c:forEach var="filter" items="${filters}">
			<option${filter == param.filter?' selected="selected"':''}>${filter}</option>
		</c:forEach>
		</select>
	</div>
	
	<script type="text/javascript">	
		function imagePreview(area) {
			jQuery("#image-preview img").attr("src", "${info.staticRootURL}transform/${param.filter}/${currentTemplate.name}/"+area+"/images/demo.jpg?random="+Math.floor(Math.random()*10000));
			jQuery("#image-preview legend").html("preview : "+area);
		}
	</script>
	
	
	<c:if test="${not empty param.filter}">
	<div class="sTableWrapper">
	<table class="sTable" width="100%" cellspacing="0" cellpadding="0">		
		<thead>
		<tr>
		<th>area :</th>
		<th>${i18n.edit['global.all']}</th>
		<c:forEach var="area" items="${areas}">
			<th>${area} <a class="action-button" href="#image-preview" onclick="imagePreview('${area}');">preview</a></th>
		</c:forEach>
		</tr>
		</thead>
		<c:forEach var="prop" items="${textProperties}">
		<tr>
			<th>${prop}</th>
			<c:set var="key" value="${param.filter}.${prop}" />
			<td><input type="text" name="${key}" value="${values[key]}" placeholder="${allValues[key]}"/></td>
			<c:forEach var="area" items="${areas}">
				<c:set var="key" value="${param.filter}.${area}.${prop}" />
				<td><input type="text" name="${key}" value="${values[key]}" placeholder="${allValues[key]}" /></td>
			</c:forEach>
		</tr>			
		</c:forEach>		
		<c:forEach var="prop" items="${booleanProperties}">
		<tr>
			<th>${prop}</th>
			<c:set var="key" value="${param.filter}.${prop}" />
			<td>
				<input type="checkbox" name="${key}" ${values[key] == "true"?'checked="checked"':''} />
				<input type="checkbox" readonly="readonly" ${allValues[key] == "true"?'checked="checked"':''} />
				<input type="hidden" name="_${key}" value="true" />
			</td>
			<c:forEach var="area" items="${areas}">
				<c:set var="key" value="${param.filter}.${area}.${prop}" />
				<td>
					<input type="checkbox" name="${key}" ${values[key] == "true"?'checked="checked"':''} onchange="jQuery(this).append('<input type=\'hidden\' name=\'_${key}\' value=\'true\' />');" />
					<input type="checkbox" readonly="readonly" ${allValues[key] == "true"?'checked="checked"':''} />
					
				</td>
			</c:forEach>
		</tr>			
		</c:forEach>
	</table>
	</div>
	</c:if>
	
	<c:if test="${not empty param.filter}">
	<fieldset id="image-preview">
		<legend></legend>
		<figure>
			<img src="" />
		</figure>
	</fieldset>
	<script type="text/javascript">
		imagePreview("${not empty modifiedArea?modifiedArea:'content'}");
	</script>
	</c:if>
	
	
	<div class="action">
		<input type="submit" name="back" value="${i18n.edit['global.back']}" />
		<input type="submit" value="${i18n.edit['global.ok']}" />		
	</div>
	
</form>

</div>

