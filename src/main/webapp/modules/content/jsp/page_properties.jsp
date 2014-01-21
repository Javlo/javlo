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

<c:if test="${!userInterface.light}">
<div class="line template">		
	<label for="template">${i18n.edit['item.template']}</label>	
	<select id="template" name="template">
		<c:if test="${not empty inheritedTemplate}">
		<option value="">${i18n.edit['global.inherited']} (${inheritedTemplate.name})</option>
		</c:if>
		<c:forEach var="template" items="${templates}">
		<option value="${template.name}" ${template.name eq page.templateId?'selected="selected"':''} >${template.name}</option>
		</c:forEach>
	</select>	
</div>
</c:if>
<c:if test="${userInterface.light}">
<c:forEach var="template" items="${templates}">
<c:if test="${template.name eq page.templateId}"><div class="line"><label>${i18n.edit['item.template']} : </label>${template.name}</div></c:if>
</c:forEach>
</c:if>

<div class="line">
	<label for="new_name">${i18n.edit['item.name']}</label>
	<input type="text" id="new_name" name="new_name" value="${page.humanName}" />
</div>

<fieldset>
<legend>${i18n.edit['item.time-range']} (${i18n.edit['item.time-published']}:${page.insideTimeRange})</legend>
<div class="cols">
<div class="one_half">
<div class="line">
	<label for="start_publish">${i18n.edit['item.start-publish']}</label>
	<input class="datepicker" type="text" id="start_publish" name="start_publish" value="${page.startPublish}" />
</div>
</div>
<div class="one_half last">
<div class="line">
	<label for="end_publish">${i18n.edit['item.end-publish']}</label>
	<input class="datepicker" type="text" id="end_publish" name="end_publish" value="${page.endPublish}" />
</div>
</div>
</div>
</fieldset>

<div class="cols">
<div class="one_half">

<div class="line">
	<label>${i18n.edit['global.title']}</label>
	<span>${page.info.title}</span>
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

<div class="line">
	<label>${i18n.edit['item.path']}</label>
	<span>${page.path}</span>
</div>

<div class="line">
	<label>${i18n.edit['item.last-access']}</label>
	<span>${page.lastAccess}</span>
</div>

</div>
<div class="one_half last">

<div class="line">
	<input type="checkbox" id="page_visible" name="view" ${page.info.visible?'checked="checked"':''} value="true" />
	<label class="suffix" for="page_visible">${i18n.edit['item.visible']}</label>
</div>

<c:if test="${fn:length(info.template.pageTypes) > 1}">
<fieldset>
	<legend>${i18n.edit['item.types']}</legend>
	<c:forEach var="type" items="${info.template.pageTypes}">
	<div class="line">				
		<input type="radio" id="page_type_${type}" name="page_type" value="${type}"${page.info.type == type?' checked="checked"':''} />
		<label class="suffix" for="page_type_${type}">${type}</label>		
	</div>
	</c:forEach>
</fieldset>
</c:if>

<c:if test="${!userInterface.light}">

<div class="line">
	<input type="hidden" name="special_input" value="true" />	
	<input type="checkbox" id="break_repeat" name="break_repeat" ${page.info.breakRepeat?'checked="checked"':''} value="true" />
	<label class="suffix" for="break_repeat">${i18n.edit['item.break-repeat']}</label>
</div>

<div class="line">
	<input type="checkbox" id="association" name="association" ${page.info.childrenAssociation?'checked="checked"':''} value="true" />
	<label class="suffix" for="association">${i18n.edit['item.children-association']}</label>
</div>

<c:if test="${globalContext.collaborativeMode}">
<div class="line">
	<input type="checkbox" id="changeNotification" name="changeNotification" ${page.info.changeNotification?'checked="checked"':''} value="true" />
	<label class="suffix" for="changeNotification">${i18n.edit['item.change-notification']}</label>
</div>
</c:if>

<div class="line">
	<label>${i18n.edit['item.short-url']}</label>
	<c:if test="${!page.allreadyShortURL}">
		<input class="action-button" type="submit" id="shorturl-url-creator" name="shorturl" value="${i18n.edit['edit.action.short-url']}" />
	</c:if>
	<c:if test="${page.allreadyShortURL}">
		<span class="noclipboard"><a href="${info.shortURL}">${page.shortURL}</a></span>
		<input class="clipboard" type="button" onclick="clipboardCopy('${info.shortURL}');" value="copy : ${page.shortURL}" />
	</c:if>
</div>
</c:if>

<c:if test="${sharedContent}">
<div class="line">
	<label for="shared_name">${i18n.edit['item.shared']}</label>
	<input type="text" id="shared_name" name="share" ${page.info.visible?'checked="checked"':''} value="${page.info.sharedName}" />
</div>
</c:if>

</div>
</div>

<div class="roles">
<c:if test="${fn:length(adminRoles) > 0}">
<div class="one_half">
	<fieldset>
	<legend>${i18n.edit['item.title.admin-roles']}</legend>
	<c:forEach var="role" items="${adminRoles}">
		<div class="inline">		
		<input type="checkbox" name="admin-${role}" id="admin-${role}" ${not empty page.adminRoles[role]?'checked="checked"':''} />
		<label class="suffix" for="admin-${role}">${role}</label>
		</div> 
	</c:forEach>
	</fieldset>
</div>
</c:if> 
<c:if test="${fn:length(info.roles) > 0}">
<div class="one_half">
	<fieldset>
	<legend>${i18n.edit['item.title.view-roles']}</legend>	
	<c:forEach var="role" items="${info.roles}">
		<div class="inline">
		<input type="checkbox" name="user-${role}" id="user-${role}" ${not empty page.roles[role]?'checked="checked"':''} />
		<label class="suffix" for="user-${role}">${role}</label>
		</div>
	</c:forEach>	
	</fieldset>
</div>
</c:if>
</div>

<div class="action">
	<input type="submit" value="${i18n.edit['global.ok']}" />
</div>

</form>

</div>
</div>

