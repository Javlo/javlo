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
	
<form id="form-page-properties" class="standard-form js-change-submit" action="${info.currentURL}" method="post">

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

<div class="line">
	<label>${i18n.edit['global.name']}</label>
	<span>${currentContext.key}</span>
</div>

<div class="line">
	<label>${i18n.edit['admin.title.folder']}</label>
	<span>${currentContext.folder}</span>
</div>

<div class="line">
	<label>${i18n.edit['admin.form.size']}</label>
	<span>${currentContext.size}</span>
</div>

<div class="line">		
	<label for="global-title">${i18n.edit['admin.form.global-title']}</label>
	<input type="text" id="global-title" name="global-title" value="${currentContext.globalTitle}" />	
</div>

</fieldset>

<fieldset>
<legend>${i18n.edit['admin.form.config']}</legend>

<div class="line">		
	<label for="administrator">${i18n.edit['admin.adminitrator']}</label>
	<input type="text" id="administrator" name="administrator" value="${currentContext.administrator}" />	
</div>

<div class="line">		
	<label for="alias">${i18n.edit['admin.form.alias']}</label>	
	<select id="alias" name="alias">
		<option value="">&nbsp;</option>
		<c:forEach var="context" items="${contextList}">
		<option value="${context.key}" ${context.key eq currentContext.aliasOf?'selected="selected"':''} >${context.key}</option>
		</c:forEach>
	</select>	
</div>

<div class="line">		
	<label for="default-languages">${i18n.edit['admin.form.language']}</label>
	<input type="text" id="default-languages" name="default-languages" value="${currentContext.defaultLanguage}" />	
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
	<label for="homepage">${i18n.edit['admin.form.homepage']}</label>
	<input type="text" id="homepage" name="homepage" value="${currentContext.homepage}" />	
</div>

<div class="line">		
	<label for="google-ana">${i18n.edit['admin.title.google-ana']}</label>
	<input type="text" id="google-ana" name="google-ana" value="${currentContext.googleAnalyticsUACCT}" />	
</div>

<div class="line">		
	<label for="tags">${i18n.edit['admin.title.tags']}</label>
	<input type="text" id="tags" name="tags" value="${currentContext.tags}" />	
</div>

</fieldset>

<fieldset>
<legend>${i18n.edit['global.security']}</legend>

<div class="line">		
	<label for="user-factory">${i18n.edit['admin.form.user-factory']}</label>
	<input type="text" id="user-factory" name="user-factory" value="${currentContext.userFactoryClassName}" />	
</div>

<div class="line">		
	<label for="admin-user-factory">${i18n.edit['admin.form.admin-user-factory']}</label>
	<input type="text" id="admin-user-factory" name="admin-user-factory" value="${currentContext.adminUserFactoryClassName}" />	
</div>

<div class="line">		
	<label for="block-password">${i18n.edit['admin.form.block-password']}</label>
	<input type="text" id="block-password" name="block-password" value="${currentContext.blockPassword}" />	
</div>

<div class="line">		
	<label for="users-access">${i18n.edit['admin.form.users-access']}</label>
	<textarea id="users-access" name="users-access">${currentContext.usersAccess}</textarea>	
</div>

</fieldset>

<fieldset class="macros">
<legend>${i18n.edit['admin.title.macros']}</legend>
<c:forEach var="macro" items="${macros}">
	<div class="inline">
		<input type="checkbox" id="${macro}" name="${macro}" ${not empty selectedMacros[macro]?'checked="checked"':''}/>
		<c:set var="i18nKey" value="macro.${macro}" />
		<label for="${macro}">${not empty i18n.edit[i18nKey]?i18n.edit[i18nKey]:macro}</label>		
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
		<label for="${plugin.id}">${plugin.label} - ${plugin.version}</label>		
	</div>
</c:forEach>

<fieldset>
<legend>${i18n.edit['admin.title.template-plugin-config']}</legend>
<textarea rows="10" cols="10" name="template-plugin-config">${templatePluginConfig}</textarea>
</fieldset>
</fieldset>

<div class="action">
	<a href="${info.currentURL}?webaction=removeSite&removed-context=${currentContext.key}" class="action-button warning needconfirm" title="${i18n.edit['admin.button.remove']}"><span>${i18n.edit['admin.button.remove']}</span></a>
	<input type="submit" name="back" value="${i18n.edit['global.back']}" />
	<input type="submit" value="${i18n.edit['global.ok']}" />
</div>

</form>


</div>
</div>

