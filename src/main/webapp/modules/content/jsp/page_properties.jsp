<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="widgetbox page-properties">
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
<div class="form-group template">		
	<label for="template">${i18n.edit['item.template']}</label>	
	<select id="template" name="template" class="form-control">
		<c:if test="${not empty inheritedTemplate}">
		<option value="">${i18n.edit['global.inherited']} (${inheritedTemplate.name})</option>
		</c:if>
		<c:set var="templateFound" value="${empty page.templateId}"/>
		<c:forEach var="template" items="${templates}">
		<option value="${template.name}" ${template.name eq page.templateId?'selected="selected"':''} >${template.name}</option>
		<c:if test="${template.name eq page.templateId}"><c:set var="templateFound" value="${true}"/></c:if>
		</c:forEach>
		<c:if test="${not templateFound}">
		<option value="${page.templateId}" selected="selected">*${page.templateId}</option>
		</c:if>
	</select>	
</div>
</c:if>
<c:if test="${userInterface.light}">
<c:set var="found" value="false" />
<c:forEach var="template" items="${templates}">
<c:if test="${template.name eq page.templateId}"><div class="line"><label>${i18n.edit['item.template']} : </label>${template.name}</div><c:set var="found" value="true" /></c:if>
</c:forEach>
<c:if test="${not found}">
<div class="line"><label>${i18n.edit['item.template']} : </label>${i18n.edit['global.inherited']} (${inheritedTemplate.name})</div>
</c:if>
</c:if>

<div class="form-group">
	<label for="new_name">${i18n.edit['item.name']}</label>
	<input type="text" class="form-control" id="new_name" name="new_name" value="${page.humanName}" />
</div>

<c:if test="${not globalContext.mailingPlatform}">
<fieldset>
<legend>${i18n.edit['item.time-range']} (${i18n.edit['item.time-published']}:${page.insideTimeRange})</legend>
<div class="row">
<div class="col-sm-6">
	<label for="start_publish">${i18n.edit['item.start-publish']}
	<input class="datepicker form-control" type="text" id="start_publish" name="start_publish" value="${page.startPublish}" />
	</label>
</div>
<div class="col-sm-6">
<div class="form-group">
	<label for="end_publish">${i18n.edit['item.end-publish']}
	<input class="datepicker form-control" type="text" id="end_publish" name="end_publish" value="${page.endPublish}" />
	</label>
</div>
</div>
</div>
</fieldset>
</c:if>

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
	<label>${i18n.edit['item.creator']}</label>
	<span>${page.creator}</span>
</div>

<div class="row">
<div class="col-sm-4">
<div class="line">
	<label>${i18n.edit['item.creation-date']}</label>
	<span>${page.creationDate}</span>
</div>
</div><div class="col-sm-4">
<div class="line">
	<label>${i18n.edit['item.modification-date']}</label>
	<span>${page.modificationDate}</span>
</div>
</div><div class="col-sm-4">
<div class="line">
	<label>${i18n.edit['item.content-date']}</label>
	<span>${page.contentDateValue}</span>
</div>

</div>
</div>
<div class="row">
<div class="col-sm-4">
<div class="line">
	<label>${i18n.edit['item.path']}</label>
	<span>${page.path}</span>
</div>
</div><div class="col-sm-4">
<c:if test="${not globalContext.mailingPlatform}">
<div class="line">
	<label>${i18n.edit['item.last-access']}</label>
	<span>${page.lastAccess}</span>
</div>
</div><div class="col-sm-4">
<div class="line">
	<label>${i18n.edit['item.page-rank']}</label>
	<span>${page.pageRank}</span>
</div>
</c:if>
</div></div>
<div class="row">
<div class="col-sm-4">
<div class="line">
	<label>Cachable?</label>
	<span>${page.cacheable} <c:if test="${!page.cacheable}">(${page.notCacheableComponent})</c:if></span>
</div>
</div><div class="col-sm-4">
<div class="line">
	<label>Real Content?</label>
	<span>${page.realContent} <c:if test="${page.realContent}">(${page.realContentComponent})</c:if><c:if test="${!page.realContent && not empty page.realContentLanguage}"> [content found : ${page.realContentLanguage}]</c:if></span>
</div>
</div><div class="col-sm-4">
<div class="line">
	<label>Image</label>	
	<span><c:if test="${empty page.info.imageBean}">-</c:if><c:if test="${not empty page.info.imageBean}"><a href="${page.info.imageBean.url}" target="_blank">${page.info.imageBean.name}</a></c:if></span></div>
</div></div>

</div>
<div class="one_half last">

<div class="form-group">
	<input type="checkbox" id="page_visible" name="view" ${page.info.visible?'checked="checked"':''} value="true" />
	<label class="suffix" for="page_visible">${i18n.edit['item.visible']}</label>
</div>

<c:if test="${!userInterface.light}">
<div class="form-group">
	<input type="checkbox" id="page_active" name="active" ${page.info.pageActive?'checked="checked"':''} value="true" />
	<label class="suffix" for="page_active">${i18n.edit['item.active']}</label>
</div>
</c:if>
<c:if test="${userInterface.model && !info.page.root && !page.childrenOfAssociation}">
<div class="form-group">
	<input type="checkbox" id="page_model" name="model" ${page.model?'checked="checked"':''} value="true" />
	<label class="suffix" for="page_model">Model</label>
</div></c:if>

<c:if test="${not globalContext.mailingPlatform}">
<div class="form-group">
	<label for="seoWeight">${i18n.edit['item.seo-weight']}</label>
	<select class="form-control" id="seoWeight" name="seo_weight">
		<option value="-1" ${page.info.seoWeight == -1?'selected="selected"':''}>${i18n.edit['global.inherited']}</option>
		<option value="0" ${page.info.seoWeight == 0?'selected="selected"':''}>${i18n.edit['item.seo-weight.0']}</option>
		<option value="1" ${page.info.seoWeight == 1?'selected="selected"':''}>${i18n.edit['item.seo-weight.1']}</option>
		<option value="2" ${page.info.seoWeight == 2?'selected="selected"':''}>${i18n.edit['item.seo-weight.2']}</option>
		<option value="3" ${page.info.seoWeight == 3?'selected="selected"':''}>${i18n.edit['item.seo-weight.3']}</option>
	</select>	
</div>
</c:if>

<c:if test="${fn:length(info.template.pageTypes) > 1}">
<fieldset>
	<legend>${i18n.edit['item.types']}</legend>
	<c:forEach var="type" items="${info.template.pageTypes}">
	<div class="form-group">				
		<input type="radio" class="form-control" id="page_type_${type}" name="page_type" value="${type}"${page.info.type == type?' checked="checked"':''} />
		<label class="suffix" for="page_type_${type}">${type}</label>		
	</div>
	</c:forEach>
</fieldset>
</c:if>

<c:if test="${!userInterface.light}">

<div class="checkbox">
	<input type="hidden" name="special_input" value="true" />	
	<label>
	<input type="checkbox" id="break_repeat" name="break_repeat" ${page.info.breakRepeat?'checked="checked"':''} value="true" />
	${i18n.edit['item.break-repeat']}</label>
</div>

<div class="checkbox">
	<label>
	<input type="checkbox" id="association" name="association" ${page.info.childrenAssociation?'checked="checked"':''} value="true" />
	${i18n.edit['item.children-association']}</label>
</div>

<c:if test="${globalContext.collaborativeMode}">
<div class="checkbox">
	<label><input type="checkbox" id="changeNotification" name="changeNotification" ${page.info.changeNotification?'checked="checked"':''} value="true" />
	${i18n.edit['item.change-notification']}</label>
</div>
</c:if>

<div class="form-group">
	<label>${i18n.edit['item.short-url']}</label>
	<c:if test="${!page.allreadyShortURL}">
		<input class="action-button" type="submit" id="shorturl-url-creator" name="shorturl" value="${i18n.edit['edit.action.short-url']}" />
	</c:if>
	<c:if test="${page.allreadyShortURL}">
		<span class="noclipboard"><a href="${info.shortURL}">${page.shortURL}</a></span>
		<input class="clipboard" type="button" class="btn btn-default" onclick="clipboardCopy('${info.shortURL}');" value="copy : ${page.shortURL}" />
		<span class="noclipboard">(${info.requestContentLanguage}:<a href="${info.shortLanguageURL}">${page.shortLanguageURL}</a>)</span>
		<input class="clipboard" type="button" class="btn btn-default" onclick="clipboardCopy('${info.shortLanguageURL}');" value="copy : ${page.shortLanguageURL}" />		
	</c:if>
</div>
</c:if>

<c:if test="${sharedContent}">
<div class="form-group">
	<label for="shared_name">${i18n.edit['item.shared']}</label>
	<input type="text" class="form-control" id="shared_name" name="share" ${page.info.visible?'checked="checked"':''} value="${page.info.sharedName}" />
</div>
</c:if>
<c:if test="${ipsecurity}">
<div class="form-group">
	<label for="shared_name">${i18n.edit['item.ipsecurity']}</label>
	<input type="text" class="form-control" id="ipsecurity" name="ipsecurity" value="${page.page.ipSecurityErrorPageName}" />
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
		<label class="checkbox-inline">	
		<input type="checkbox" name="admin-${role}" id="admin-${role}" ${not empty page.adminRoles[role]?'checked="checked"':''} />
		${role}</label>
	</c:forEach>
	<div class="other-roles">
	<c:forEach var="role" items="${adminOtherRole}">
		<label class="checkbox-inline">	
		<input type="checkbox" name="admin-${role}" id="admin-${role}" ${not empty page.adminRoles[role]?'checked="checked"':''} />
		${role}</label>
	</c:forEach>
	</div>
	</fieldset>
</div>
</c:if> 
<c:if test="${fn:length(info.roles) > 0}">
<div class="one_half">
	<fieldset>
	<legend>${i18n.edit['item.title.view-roles']}</legend>	
	<c:forEach var="role" items="${info.roles}">
		<label class="checkbox-inline">
		<input type="checkbox" name="user-${role}" id="user-${role}" ${not empty page.roles[role]?'checked="checked"':''} />
		${role}</label>
	</c:forEach>	
	</fieldset>
</div>
</c:if>
</div>

<div class="action">
	<input type="submit" class="btn btn-primary btn-color" value="${i18n.edit['global.ok']}" />
</div>

</form>

</div>
</div>

