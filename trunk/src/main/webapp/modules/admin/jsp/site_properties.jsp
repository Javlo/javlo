<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="widgetbox site-properties">
<h3><span>${i18n.edit['admin.site-properties']}</span></h3>
<div class="content">

<c:if test="${not empty templateImageUrl}">
<div class="template-preview">
	<img src="${templateImageUrl}" alt="template preview" lang="en" />
</div>
</c:if>
	
<form id="form-page-properties" class="standard-form js-change-submit" action="${info.currentURL}" method="post" enctype="multipart/form-data">

<div>
	<input type="hidden" name="webaction" value="updateGlobalContext" />
	<input type="hidden" name="name" value="${page.name}" />
	<input type="hidden" name="context" value="${currentContext.key}" />
</div>

<div class="line template">		
	<label for="default-template">${i18n.edit['admin.default-template']}</label>	
	<select id="default-template" name="default-template">
		<option value="">&nbsp;</option>
		<c:forEach var="template" items="${templates}">
		<option value="${template.name}" ${template.name eq currentContext.defaultTemplate?'selected="selected"':''} >${template.name}</option>
		</c:forEach> 
	</select>	
</div>

<fieldset>
<legend>${i18n.edit['admin.form.info']}</legend>

<div class="one_half">
<div class="line">
	<label>${i18n.edit['global.name']}</label>
	<span>${currentContext.key}</span>
</div>

<div class="line">
	<label>${i18n.edit['admin.title.folder']}</label>
	<span>${currentContext.folder}</span>
</div>
</div>

<div class="one_half">
<div class="line">
	<label>${i18n.edit['admin.form.size']}</label>
	<span>${currentContext.size}</span>
</div>

<div class="line">		
	<label for="global-title">${i18n.edit['admin.form.global-title']}</label>
	<input type="text" id="global-title" name="global-title" value="${currentContext.globalTitle}" />	
</div>
</div>

</fieldset>

<fieldset>
<legend>${i18n.edit['admin.form.config']}</legend>

<div class="one_half">

<c:if test="${fn:length(contextList) > 0}">
<div class="line">		
	<label for="alias">${i18n.edit['admin.form.alias']}</label>	
	<select id="alias" name="alias">
		<option value="">&nbsp;</option>
		<c:forEach var="context" items="${contextList}">
		<option value="${context.key}" ${context.key eq currentContext.aliasOf?'selected="selected"':''} >${context.key}</option>
		</c:forEach>
	</select>	
</div>
</c:if>

<div class="line">		
	<label for="administrator">${i18n.edit['admin.adminitrator']}</label>
	<input type="text" id="administrator" name="administrator" value="${currentContext.administrator}" />	
</div>

<div class="line">		
	<label for="default-languages">${i18n.edit['admin.form.language']}</label>
	<input type="text" id="default-languages" name="default-languages" value="${currentContext.defaultLanguages}" />	
</div>

<div class="line">		
	<label for="languages">${i18n.edit['admin.form.languages']}</label>
	<input type="text" id="languages" name="languages" value="${currentContext.languages}" />	
</div>

<div class="line">		
	<label for="content-languages">${i18n.edit['admin.form.content-languages']}</label>
	<input type="text" id="content-languages" name="content-languages" value="${currentContext.contentLanguages}" />
</div>

<div class="line">	
	<input type="checkbox" id="switch-default-language" name="switch-default-language" ${currentContext.autoSwitchToDefaultLanguage?'checked="checked"':""}" />
	<label class="suffix" for="switch-default-language">${i18n.edit['admin.form.switch-default-language']}</label>
</div>

<div class="line">		
	<label for="tags">${i18n.edit['admin.title.tags']}</label>
	<input type="text" id="tags" name="tags" value="${currentContext.tags}" />	
</div>

<div class="line">		
	<label for="google-ana">${i18n.edit['admin.title.google-ana']}</label>
	<input type="text" id="google-ana" name="google-ana" value="${currentContext.googleAnalyticsUACCT}" />	
</div>

<div class="line">		
	<label for="short-date">${i18n.edit['admin.title.short-date']}</label>
	<input type="text" id="short-date" name="short-date" value="${currentContext.shortDateFormat}" />	
</div>

<div class="line">		
	<label for="medium-date">${i18n.edit['admin.title.medium-date']}</label>
	<input type="text" id="medium-date" name="medium-date" value="${currentContext.mediumDateFormat}" />	
</div>

<div class="line">		
	<label for="full-date">${i18n.edit['admin.title.full-date']}</label>
	<input type="text" id="full-date" name="full-date" value="${currentContext.fullDateFormat}" />	
</div>

<div class="line">		
	<label for="template-mode">${i18n.edit['admin.title.template-mode']}</label>
	<input type="text" id="template-mode" name="template-mode" value="${currentContext.editTemplateMode}" />	
</div>

<div class="line">		
	<label for="platform">${i18n.edit['admin.title.platform']}</label>
	<input type="text" id="platform" name="platform" value="${currentContext.platformType}" />	
</div>

</div>

<div class="one_half">

<div class="line">		
	<label for="homepage">${i18n.edit['admin.form.homepage']}</label>
	<input type="text" id="homepage" name="homepage" value="${currentContext.homepage}" />	
</div>

<div class="line">		
	<label for="urlfactory">${i18n.edit['admin.form.urlfactory']}</label>
	<input type="text" id="urlfactory" name="urlfactory" value="${currentContext.urlFactory}" />	
</div>

<div class="line">		
	<label for="forced-host">${i18n.edit['admin.form.forced-host']}</label>
	<input type="text" id="forced-host" name="forced-host" value="${currentContext.forcedHost}" />	
</div>

<div class="line">		
	<label for="nopup-domain">${i18n.edit['admin.form-popup-domain']}</label>
	<input type="text" id="nopup-domain" name="nopup-domain" value="${currentContext.noPopupDomain}" />	
</div>

<div class="line">
	<input type="checkbox" id="link-as-popup" name="link-as-popup" ${currentContext.openExternalLinkAsPopup?'checked="checked"':""}" />
	<label class="suffix" for="link-as-popup">${i18n.edit['admin.form.external-link-popup']}</label>	
</div>

<div class="line">	
	<input type="checkbox" id="file-as-popup" name="file-as-popup" ${currentContext.openFileAsPopup?'checked="checked"':""}" />
	<label class="suffix" for="file-as-popup">${i18n.edit['admin.form.file-popup']}</label>	
</div>

<div class="line">	
	<input type="checkbox" id="extend-menu" name="extend-menu" ${currentContext.extendMenu?'checked="checked"':""}" />
	<label class="suffix" for="extend-menu">${i18n.edit['admin.form.extend-menu']}</label>	
</div>

<div class="line">	
	<input type="checkbox" id="preview-mode" name="preview-mode" ${currentContext.previewMode?'checked="checked"':""}" />
	<label class="suffix" for="preview-mode">${i18n.edit['admin.form.preview-mode']}</label>	
</div>

<div class="line">	
	<input type="checkbox" id="wizz" name="wizz" ${currentContext.wizz?'checked="checked"':""}" />
	<label class="suffix" for="wizz">${i18n.edit['admin.form.wizz']}</label>	
</div>

<div class="line">	
	<input type="checkbox" id="Reversedlink" name="reversedlink" ${currentContext.reversedlink?'checked="checked"':""}" />
	<label class="suffix" for="Reversedlink">${i18n.edit['admin.form.reversedlink']}</label>	
</div>

<div class="line">		
	<label for="help-url">${i18n.edit['admin.title.help-url']}</label>
	<input type="text" id="help-url" name="help-url" value="${currentContext.helpURL}" />	
</div>

<div class="line">		
	<label for="private-help-url">${i18n.edit['admin.title.private-help-url']}</label>
	<input type="text" id="private-help-url" name="private-help-url" value="${currentContext.privateHelpURL}" />	
</div>

<div class="line">		
	<label for="uri-alias">${i18n.edit['admin.form.uri-alias']}</label>
	<textarea id="uri-alias" name="uri-alias">${currentContext.URIAlias}</textarea>	
</div>

<div class="line">		
	<label for="dmz-inter">${i18n.edit['admin.title.dmz-inter']}</label>
	<input type="text" id="dmz-inter" name="dmz-inter" value="${currentContext.DMZServerInter}" />	
</div>

<div class="line">		
	<label for="dmz-intra">${i18n.edit['admin.title.dmz-intra']}</label>
	<input type="text" id="dmz-intra" name="dmz-intra" value="${currentContext.DMZServerIntra}" />	
</div>

<div class="line">		
	<label for="proxy-prefix">${i18n.edit['admin.url.proxy-prefix']}</label>
	<input type="text" id="proxy-prefix" name="proxy-prefix" value="${currentContext.proxyPathPrefix}" />	
</div>

</div>
</fieldset>

<fieldset>
<legend>${i18n.edit['global.security']}</legend>

<div class="one_half">
<div class="line">		
	<label for="user-factory">${i18n.edit['admin.form.user-factory']}</label>
	<input type="text" id="user-factory" name="user-factory" value="${currentContext.userFactoryClassName}" />	
</div>

<div class="line">		
	<label for="admin-user-factory">${i18n.edit['admin.form.admin-user-factory']}</label>
	<input type="text" id="admin-user-factory" name="admin-user-factory" value="${currentContext.adminUserFactoryClassName}" />	
</div>

<div class="line">		
	<label for="user-roles">${i18n.edit['admin.form.user-roles']}</label>
	<input type="text" id="user-roles" name="user-roles" value="${currentContext.userRoles}" />	
</div>

<div class="line">		
	<label for="admin-user-roles">${i18n.edit['admin.form.admin-user-roles']}</label>
	<input type="text" id="admin-user-roles" name="admin-user-roles" value="${currentContext.adminUserRoles}" />	
</div>

<div class="line">		
	<label for="block-password">${i18n.edit['admin.form.block-password']}</label>
	<input type="text" id="block-password" name="block-password" value="${currentContext.blockPassword}" />	
</div>
</div>

<div class="one_half">

<div class="line">		
	<input type="checkbox" id="only-creator-modify" name="only-creator-modify" ${currentContext.onlyCreatorModify?'checked="checked"':""}" />
	<label class="suffix" for="only-creator-modify">${i18n.edit['admin.form.only-creator-modify']}</label>
</div>

<div class="line">	
	<input type="checkbox" id="collaborative-mode" name="collaborative-mode" ${currentContext.collaborativeMode?'checked="checked"':""}" />
	<label class="suffix" for="collaborative-mode">${i18n.edit['admin.form.collaborative-mode']}</label>
</div>

<div class="line">		
	<label for="users-access">${i18n.edit['admin.form.users-access']}</label>
	<textarea id="users-access" name="users-access">${currentContext.usersAccess}</textarea>	
</div>

<c:if test="${not empty qrcode}">
<div class="line">
	<label for="qrcode">${i18n.edit['admin.form.mobil-access']}</label>
	<input type="button" id="qrcode" value="qrcode" />
	<script type="text/javascript">
		jQuery('#qrcode').click(function() {
			var item = jQuery(this);
			item.wrap("<div id=\"qrcode-wrapper\" />");
			jQuery("#qrcode-wrapper").html('<a href="${editAutoURL}"><img src="${qrcode}" /></a>');
		});
	</script>	
</div>
</c:if>

</div>

</fieldset>

<fieldset class="macros">
<legend>${i18n.edit['admin.title.macros']}</legend>
<c:forEach var="macro" items="${macros}">
	<div class="inline">
		<input type="checkbox" id="${macro}" name="${macro}" ${not empty selectedMacros[macro]?'checked="checked"':''}/>
		<c:set var="i18nKey" value="macro.${macro}" />
		<label class="suffix" for="${macro}">${not empty i18n.edit[i18nKey]?i18n.edit[i18nKey]:macro}</label>		
	</div>
</c:forEach>
</fieldset>

<fieldset class="templates-list">
<legend>${i18n.edit['admin.title.template-linked']}</legend>

<div id="gallery" class="gallery">
<div id="gridview" class="thumbview">
<ul>
<c:forEach var="template" items="${templates}">
    <li>
        <div class="thumb">
            <img src="${template.previewUrl}" alt="${template.name}" />
            <div class="info">
                <p>
                    <label>${i18n.edit['global.name']}:</label>
                    <span>${template.name}</span>
                </p>
                <p>
                    <label>${i18n.edit['admin.file-source']}:</label>
                    <span><a href="${template.htmlUrl}">${template.htmlFile}</a></span>
                </p>
                <p>
                    <label>${i18n.edit['global.author']}:</label>
                    <span>${template.authors}</span>
                </p>
                <p>
                    <label>${i18n.edit['global.date']}:</label>
                    <span>${template.creationDate}</span>
                </p>
                <p>
                	<a href="${template.downloadURL}">${i18n.edit['admin.download-template']}</a>
                </p>
                <p class="menu">
                    <a href="${template.viewUrl}" class="view" title="${template.name}"></a>                    
                    <a href="${info.currentURL}?webaction=unlinkTemplate&context=${currentContext.key}&template=${template.name}&mailing=${template.mailing}" class="delete" title="${i18n.edit['global.unlinked']}"></a>
                </p>                
            </div><!--info-->
        </div><!--thumb-->        
 	</li>
 	</c:forEach>
 </ul>
 <div class="template-action">
 <a class="action-button" href="${linkUrl}">${i18n.edit['admin.button.link-template']}</a>
 </div>
 </div>
 </div>

</fieldset>

<fieldset class="templates-plugin">
<legend>${i18n.edit['admin.title.template-plugin']}</legend>
<c:forEach var="plugin" items="${templatePlugins}">
	<div class="inline">
		<input type="checkbox" id="${plugin.id}" name="${plugin.id}" ${not empty selectedTemplatePlugins[plugin.id]?'checked="checked"':''}/>
		<label class="suffix" for="${plugin.id}">${plugin.label} - ${plugin.version}</label>		
	</div>
</c:forEach>

<fieldset>
<legend>${i18n.edit['admin.title.template-plugin-config']}</legend>
<textarea rows="10" cols="10" name="template-plugin-config">${templatePluginConfig}</textarea>
</fieldset>
</fieldset>

<fieldset>
<legend>${i18n.edit['admin.title.graphic-charter']}</legend>

<div class="cols">
<div class="one_half">
<jsp:include page="template_data.jsp?name=background&value=${currentContext.templateData.background}" />
<jsp:include page="template_data.jsp?name=backgroundMenu&value=${currentContext.templateData.backgroundMenu}" />
<jsp:include page="template_data.jsp?name=foreground&value=${currentContext.templateData.foreground}" />
<jsp:include page="template_data.jsp?name=border&value=${currentContext.templateData.border}" />
<jsp:include page="template_data.jsp?name=textMenu&value=${currentContext.templateData.textMenu}" />
<jsp:include page="template_data.jsp?name=text&value=${currentContext.templateData.text}" />
<jsp:include page="template_data.jsp?name=title&value=${currentContext.templateData.title}" />
<jsp:include page="template_data.jsp?name=link&value=${currentContext.templateData.link}" />
<jsp:include page="template_data.jsp?name=special&value=${currentContext.templateData.special}" />
</div><div class="one_half last">
<div class="line">
	<label for="logo">logo : </label>
	<input type="file" name="logo" id="logo" />
</div>
<c:if test="${not empty logoPreview}">
	<img alt="logo" src="${logoPreview}" />
</c:if>
</div>
</div>

</fieldset>

<div class="action">
	<a href="${info.currentURL}?webaction=removeSite&removed-context=${currentContext.key}" class="action-button warning needconfirm" title="${i18n.edit['admin.button.remove']}"><span>${i18n.edit['admin.button.remove']}</span></a>
	<input type="submit" name="back" value="${i18n.edit['global.back']}" />
	<input type="submit" value="${i18n.edit['global.ok']}" />
</div>

</form>


</div>
</div>

