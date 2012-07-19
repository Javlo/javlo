<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="widgetbox page-properties">
<h3><span>${i18n.edit['item.title']}</span></h3>
<div class="content">

<div class="template-preview">
	<img src="${templateImageUrl}" alt="template preview" lang="en" />
</div>
	
<form id="form-page-properties" class="standard-form js-change-submit" action="${info.currentURL}" method="post">

<div>
	<input type="hidden" name="webaction" value="pageProperties" />
	<input type="hidden" name="name" value="${page.name}" />
</div>

<div class="line template">		
	<label for="template">${i18n.edit['item.template']}</label>	
	<select id="template" name="template">
		<option value="">${i18n.edit['global.inherited']}</option>
		<c:forEach var="template" items="${templates}">
		<option value="${template.name}" ${template.name eq page.templateId?'selected="selected"':''} >${template.name}</option>
		</c:forEach>
	</select>	
</div>

<div class="line">
	<label>${i18n.edit['global.title']}</label>
	<span>${page.info.title}</span>
</div>

<div class="line">
	<label for="new_name">${i18n.edit['item.name']}</label>
	<input type="text" id="new_name" name="new_name" value="${page.name}" />
</div>

<div class="line">
	<label for="page_visible">${i18n.edit['item.visible']}</label>
	<input type="checkbox" id="page_visible" name="view" ${page.info.visible?'checked="checked"':''} value="true" />
</div>

<div class="line">
	<label>${i18n.edit['item.path']}</label>
	<span>${page.path}</span>
</div>

<div class="line">
	<label>${i18n.edit['item.modificator']}</label>
	<span>${page.latestEditor}</span>
</div>

<div class="line">
	<label>${i18n.edit['item.creation-date']}</label>
	<span>${page.creationDate}</span>
</div>

<div class="line">
	<label>${i18n.edit['item.modification-date']}</label>
	<span>${page.modificationDate}</span>
</div>

<div class="action">
    <a href="${info.currentURL}?webaction=deletePage&page=${page.id}" class="action-button warning needconfirm" title="${i18n.edit['global.delete']}"><span>${i18n.edit['global.delete']}</span></a>
	<input type="submit" value="${i18n.edit['global.ok']}" />
</div>

</form>


</div>
</div>

