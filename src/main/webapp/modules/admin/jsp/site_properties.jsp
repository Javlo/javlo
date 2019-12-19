<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="widgetbox site-properties">
<h3><span>${i18n.edit['admin.site-properties']}</span></h3>
<div class="content">
<c:if test="${!info.admin}">
<form id="form-page-properties" action="${info.currentURL}" method="post" enctype="multipart/form-data">
<div class="pull-right">
	<button type="submit" class="btn btn-primary">${i18n.edit['global.ok']}</button>
</div>
	<div>
		<input type="hidden" name="webaction" value="admin.updateGlobalContextLight" />
	</div>	
	<fieldset>
		<legend>${i18n.edit['admin.form.info']}</legend>
		<div class="row">
			<div class="col-sm-6">
				<div class="form-group">
					<label>${i18n.edit['global.name']}</label>
					<span>${currentContext.key}</span>
				</div>		
				<div class="form-group">
					<label>${i18n.edit['admin.form.size']}</label>
					<span>${currentContext.size}</span>
				</div><div class="form-group">		
					<label for="template-mode">${i18n.edit['admin.title.template-mode']}</label>
					<select class="form-control" id="template-mode" name="template-mode">
						<c:forEach var="layout" items="${contentContext.globalContext.staticConfig.previewLayout}">
							<option${layout == currentContext.editTemplateMode?' selected="selected"':''}>${layout}</option>
						</c:forEach>
					</select>
				</div>
				<c:if test="${fn:length(contentContext.currentTemplate.quietableAreaMap)>0}">
					<div class="form-group">		
						<label for="global-title">${i18n.edit['admin.form.quiet-area']} : </label>
						<c:forEach var="area" items="${contentContext.currentTemplate.quietableAreaMap}" varStatus="status">
							<c:if test="${area != 'content'}">
								<div class="checkbox-inline">
									<label>
										<input name="quietAreas" type="checkbox" value="${area.value}" ${not empty currentContext.quietAreaMap[area.value]?'checked="checked"':''}/> ${area.value}
									</label>
								</div>
							</c:if>
						</c:forEach>
					</div>
				</c:if>
			</div><div class="col-sm-6">
					<div class="screenshot">
						<c:if test="${currentContext.screenshot}">
							<a href="${currentContext.screenshotUrl}" target="_blank">
								<img id="screenshot-img" src="${currentContext.screenshotUrl}" alt="screenshot" /><br />
							</a>
						</c:if>
						<button class="btn btn-primary btn-sm" onclick="closePopup(); window.open('${takeSreenshotUrl}'); return false;">take a screenshot</button>
					</div>
				</div>
		</div>
	</fieldset>
	
	<fieldset>
	<legend>${i18n.edit['admin.form.owner']}</legend>
	<div class="row">
	<div class="col-sm-6">
	<div class="form-group">		
	<label for="global-title">${i18n.edit['admin.form.global-title']}</label>
	<input class="form-control" type="text" id="global-title" name="global-title" value="${currentContext.globalTitle}" />	
	</div>
	<div class="form-group">		
		<label for="owner.name">${i18n.edit['admin.form.owner.name']}</label>
		<input class="form-control" type="text" id="owner.name" name="owner.name" value="${currentContext.ownerName}" />	
	</div>
	<div class="form-group">		
		<label for="owner.address">${i18n.edit['admin.form.owner.address']}</label>
		<textarea class="form-control" id="owner.address" name="owner.address">${currentContext.ownerAddress}</textarea>	
	</div>
	</div><div class="col-sm-6">
	<div class="form-group">		
		<label for="owner.number">${i18n.edit['admin.form.owner.number']}</label>
		<input class="form-control" type="text" id="owner.number" name="owner.number" value="${currentContext.ownerNumber}" />	
	</div>
	<div class="form-group">		
		<label for="owner.email">${i18n.edit['admin.form.owner.email']}</label>
		<input class="form-control" type="email" id="owner.email" name="owner.email" value="${currentContext.ownerEmail}" />	
	</div>
	<div class="form-group">		
		<label for="owner.phone">${i18n.edit['admin.form.owner.phone']}</label>
		<input class="form-control" type="text" id="owner.phone" name="owner.phone" value="${currentContext.ownerPhone}" />	
	</div>
	</div></div>
	</fieldset>
	
	<jsp:include page="graphic_charter.jsp" />
	<div class="pull-right">
		<button type="submit" class="btn btn-primary">${i18n.edit['global.ok']}</button>
	 </div>
	
</form>
</c:if>
<c:if test="${info.admin}">
<form id="form-page-properties" action="${info.currentURL}" method="post" enctype="multipart/form-data">

<div>
	<input type="hidden" name="webaction" value="updateGlobalContext" />
	<input type="hidden" name="name" value="${page.name}" />
	<input type="hidden" name="context" value="${currentContext.key}" />
</div>

<div class="pull-right">
	<a href="${info.currentURL}?webaction=removeSite&removed-context=${currentContext.key}" class="btn btn-default warning needconfirm" title="${i18n.edit['admin.button.remove']}"><span>${i18n.edit['admin.button.remove']}</span></a>
	<button type="submit" name="back" class="btn btn-default">${i18n.edit['global.back']}</button>
	<button type="submit" class="btn btn-primary">${i18n.edit['global.ok']}</button>
</div>

<div class="row">
<div class="col-xs-2"><label for="default-template">${i18n.edit['admin.default-template']}</label></div>	
<div class="col-xs-4"><select id="default-template" name="default-template" class="form-control">
		<option value="">&nbsp;</option>
		<c:forEach var="template" items="${templates}">
		<option value="${template.name}" ${template.name eq currentContext.defaultTemplate?'selected="selected"':''} >${template.name}</option>
		</c:forEach> 
	</select>	
</div>
</div>

<div class="clearfix"></div>

<fieldset>
<legend>${i18n.edit['admin.form.info']}</legend>
<div class="row">
<div class="col-sm-6">
<div class="form-group">
	<label>${i18n.edit['global.name']}</label>
	<span>${currentContext.key}</span>
</div>

<div class="form-group">
	<label>${i18n.edit['admin.title.folder']}</label>
	<span>${currentContext.folder}</span>
</div>
<div class="form-group">
	<label>${i18n.edit['admin.form.size']}</label>
	<span>${currentContext.size}</span>
</div>

<c:if test="${fn:length(contentContext.currentTemplate.quietableAreaMap)>0}">
<div class="form-group">		
	<label for="global-title">${i18n.edit['admin.form.quiet-area']} : </label>
	<c:forEach var="area" items="${contentContext.currentTemplate.quietableAreaMap}" varStatus="status">
		<c:if test="${area != 'content'}">
			<div class="checkbox-inline">
				<label>
					<input name="quietAreas" type="checkbox" value="${area.value}" ${not empty currentContext.quietAreaMap[area.value]?'checked="checked"':''}/> ${area.value}
				</label>
			</div>
		</c:if>
	</c:forEach>
</div>
</c:if>
</div>
<div class="col-sm-6">
	<div class="screenshot">
		<c:if test="${currentContext.screenshot}">
			<img id="screenshot-img" src="${currentContext.screenshotUrl}" alt="screenshot" /><br />
		</c:if>
		<button class="btn btn-primary btn-sm" onclick="closePopup(); window.open('${takeSreenshotUrl}'); return false;">take a screenshot</button>
	</div>
</div>
</div>

</fieldset>

<fieldset>
	<legend>${i18n.edit['admin.form.owner']}</legend>
	<div class="row">
	<div class="col-sm-6">
	<div class="form-group">		
	<label for="global-title">${i18n.edit['admin.form.global-title']}</label>
	<input class="form-control" type="text" id="global-title" name="global-title" value="${currentContext.globalTitle}" />	
</div>
<div class="form-group">		
	<label for="owner.name">${i18n.edit['admin.form.owner.name']}</label>
	<input class="form-control" type="text" id="owner.name" name="owner.name" value="${currentContext.ownerName}" />	
</div>
<div class="form-group">		
	<label for="owner.address">${i18n.edit['admin.form.owner.address']}</label>
	<textarea class="form-control" id="owner.address" name="owner.address">${currentContext.ownerAddress}</textarea>	
</div>
</div><div class="col-sm-6">
<div class="form-group">		
	<label for="owner.number">${i18n.edit['admin.form.owner.number']}</label>
	<input class="form-control" type="text" id="owner.number" name="owner.number" value="${currentContext.ownerNumber}" />	
</div>
<div class="form-group">		
	<label for="owner.email">${i18n.edit['admin.form.owner.email']}</label>
	<input class="form-control" type="email" id="owner.email" name="owner.email" value="${currentContext.ownerEmail}" />	
</div>
<div class="form-group">		
	<label for="owner.phone">${i18n.edit['admin.form.owner.phone']}</label>
	<input class="form-control" type="text" id="owner.phone" name="owner.phone" value="${currentContext.ownerPhone}" />	
</div>
</div></div>
</fieldset>

<fieldset>
<legend>${i18n.edit['admin.form.config']}</legend>

<div class="row">
<div class="col-sm-6">

<c:if test="${fn:length(contextList) > 0}">
<c:if test="${not empty currentContext.aliasActive}"><div class="row"><div class="col-md-6"></c:if>
<div class="form-group">		
	<label for="alias">${i18n.edit['admin.form.alias']}</label>	
	<select id="alias" name="alias" class="form-control">
		<option value="">&nbsp;</option>
		<c:forEach var="context" items="${contextList}">
		<option value="${context.key}" ${context.key eq currentContext.aliasOf?'selected="selected"':''} >${context.key}</option>
		</c:forEach>
	</select>	
</div>
<c:if test="${not empty currentContext.aliasActive}"></div><div class="col-md-6">
<div class="form-group">		
	<label for="alias-active">${i18n.edit['admin.form.alias-active']}</label>
	<select id="alias-active" name="alias-active" class="form-control">
		<option value="true" ${currentContext.aliasActive?'selected="selected"':''} >${i18n.edit['global.yes']}</option>
		<option value="false" ${!currentContext.aliasActive?'selected="selected"':''} >${i18n.edit['global.no']}</option>
	</select>	
</div>
</div></div></c:if>
</c:if>

<div class="form-group">		
	<label for="administrator">${i18n.edit['admin.adminitrator']}</label>
	<input class="form-control" type="text" id="administrator" name="administrator" value="${currentContext.administrator}" />	
</div>

<div class="form-group">		
	<label for="default-languages">${i18n.edit['admin.form.language']}</label>
	<input  class="form-control" type="text" id="default-languages" name="default-languages" value="${currentContext.defaultLanguages}" />	
</div>

<div class="form-group">		
	<label for="languages">${i18n.edit['admin.form.languages']}</label>
	<input  class="form-control" type="text" id="languages" name="languages" value="${currentContext.languages}" />	
</div>

<div class="form-group">		
	<label for="content-languages">${i18n.edit['admin.form.content-languages']}</label>
	<input  class="form-control" type="text" id="content-languages" name="content-languages" value="${currentContext.contentLanguages}" />
</div>

<div class="checkbox">	
	<label class="suffix"><input type="checkbox" name="switch-default-language" ${currentContext.autoSwitchToDefaultLanguage?'checked="checked"':""} />
	${i18n.edit['admin.form.switch-default-language']}</label>
</div>

<div class="form-group">		
	<label for="tags">${i18n.edit['admin.title.tags']}</label>
	<input class="form-control" type="text" id="tags" name="tags" value="${currentContext.tags}" />	
</div>

<div class="form-group">		
	<label for="google-ana">${i18n.edit['admin.title.google-ana']}</label>
	<input class="form-control" type="text" id="google-ana" name="google-ana" value="${currentContext.googleAnalyticsUACCT}" />	
</div>

<div class="form-group">		
	<label for="google-key">Google API Key</label>
	<input class="form-control" type="text" id="google-key" name="google-key" value="${currentContext.googleApiKey}" />	
</div>

<div class="form-group">		
	<label for="short-date">${i18n.edit['admin.title.short-date']}</label>
	<input class="form-control" type="text" id="short-date" name="short-date" value="${currentContext.shortDateFormat}" />	
</div>

<div class="form-group">		
	<label for="medium-date">${i18n.edit['admin.title.medium-date']}</label>
	<input class="form-control" type="text" id="medium-date" name="medium-date" value="${currentContext.mediumDateFormat}" />	
</div>

<div class="form-group">		
	<label for="full-date">${i18n.edit['admin.title.full-date']}</label>
	<input class="form-control" type="text" id="full-date" name="full-date" value="${currentContext.fullDateFormat}" />	
</div>

<div class="form-group">		
	<label for="template-mode">${i18n.edit['admin.title.template-mode']}</label>
	<select class="form-control" id="template-mode" name="template-mode">
		<c:forEach var="layout" items="${contentContext.globalContext.staticConfig.previewLayout}">
			<option${layout == currentContext.editTemplateMode?' selected="selected"':''}>${layout}</option>
		</c:forEach>
	</select>
</div>

<div class="form-group">		
	<label for="platform">${i18n.edit['admin.title.platform']}</label>
	<input class="form-control" type="text" id="platform" name="platform" value="${currentContext.platformType}" />	
</div>

<div class="checkbox">		
	<label>
		 <input type="checkbox" id="components-filtered" name="components-filtered" ${currentContext.componentsFiltered?'checked="checked"':""}>
	     ${i18n.edit['admin.title.components-filtered']}		      
	</label>
</div>

<div class="checkbox">	
	<label><input type="checkbox" id="cookies" name="cookies" ${currentContext.cookies?'checked="checked"':""} />
	${i18n.edit['admin.form.cookies']}</label>	
</div>

<div class="form-group">		
	<label for="cookies-url">${i18n.edit['admin.form.cookies.url']}</label>
	<input class="form-control" type="text" id="cookies-url" name="cookies-url" value="${currentContext.cookiesPolicyUrl}" />	
</div>

</div>

<div class="col-sm-6">
<div class="form-group">		
	<label for="homepage">${i18n.edit['admin.form.homepage']}</label>
	<input class="form-control" type="text" id="homepage" name="homepage" value="${currentContext.homepage}" />	
</div>

<div class="form-group">		
	<label for="urlfactory">${i18n.edit['admin.form.urlfactory']}</label>
	<input class="form-control" type="text" id="urlfactory" name="urlfactory" value="${currentContext.urlFactory}" />	
</div>

<div class="form-group">		
	<label for="forced-host">${i18n.edit['admin.form.forced-host']}</label>
	<input class="form-control" type="text" id="forced-host" name="forced-host" value="${currentContext.forcedHost}" />	
</div>

<div class="form-group">		
	<label for="nopup-domain">${i18n.edit['admin.form-popup-domain']}</label>
	<input class="form-control" type="text" id="nopup-domain" name="nopup-domain" value="${currentContext.noPopupDomain}" />	
</div>

<div class="checkbox">
	<label><input type="checkbox" id="link-as-popup" name="link-as-popup" ${currentContext.openExternalLinkAsPopup?'checked="checked"':""} />
	${i18n.edit['admin.form.external-link-popup']}</label>	
</div>

<div class="checkbox">	
	<label><input type="checkbox" id="file-as-popup" name="file-as-popup" ${currentContext.openFileAsPopup?'checked="checked"':""} />
	${i18n.edit['admin.form.file-popup']}</label>	
</div>

<div class="checkbox">	
	<label><input type="checkbox" id="extend-menu" name="extend-menu" ${currentContext.extendMenu?'checked="checked"':""} />
	${i18n.edit['admin.form.extend-menu']}</label>	
</div>

<div class="checkbox">	
	<label><input type="checkbox" id="preview-mode" name="preview-mode" ${currentContext.previewMode?'checked="checked"':""} />
	${i18n.edit['admin.form.preview-mode']}</label>	
</div>

<div class="checkbox">	
	<label><input type="checkbox" id="wizz" name="wizz" ${currentContext.wizz?'checked="checked"':""} />
	${i18n.edit['admin.form.wizz']}</label>	
</div>

<div class="checkbox">	
	<label><input type="checkbox" id="Reversedlink" name="reversedlink" ${currentContext.reversedlink?'checked="checked"':""} />
	${i18n.edit['admin.form.reversedlink']}</label>	
</div>

<div class="form-group">		
	<label for="help-url">${i18n.edit['admin.title.help-url']}</label>
	<input class="form-control" type="text" id="help-url" name="help-url" value="${currentContext.helpURL}" />	
</div>

<div class="form-group">		
	<label for="main-help-url">${i18n.edit['admin.title.main-help-url']}</label>
	<input class="form-control" type="text" id="main-help-url" name="main-help-url" value="${currentContext.mainHelpURL}" />	
</div>

<div class="form-group">		
	<label for="private-help-url">${i18n.edit['admin.title.private-help-url']}</label>
	<input class="form-control" type="text" id="private-help-url" name="private-help-url" value="${currentContext.privateHelpURL}" />	
</div>

<div class="form-group">		
	<label for="uri-alias">${i18n.edit['admin.form.uri-alias']}</label>
	<textarea class="form-control" id="uri-alias" name="uri-alias">${currentContext.URIAlias}</textarea>	
</div>

<div class="row">
<div class="col-sm-8">
<div class="form-group">		
	<label for="dmz-inter">${i18n.edit['admin.title.dmz-inter']}</label>
	<input class="form-control" type="text" id="dmz-inter" name="dmz-inter" value="${currentContext.DMZServerInter}" />	
</div>
<div class="form-group">
	<label for="dmz-intra">${i18n.edit['admin.title.dmz-intra']}</label>
	<input class="form-control" type="text" id="dmz-intra" name="dmz-intra" value="${currentContext.DMZServerIntra}" />	
</div></div>
<div class="col-sm-4">
	<c:if test="${not empty currentContext.DMZServerInter}">
	<c:url var="refreshAdmin" value="${info.currentURL}" context="/">
		<c:param name="webaction" value="file.synchro" />
		<c:param name="module" value="admin" />
	</c:url>
	<div class="form-group"><label>&nbsp;</label><a href="${refreshAdmin}" class="btn btn-default form-control">Synchronize</a></div>
	</c:if>
</div></div>

<div class="form-group">		
	<label for="proxy-prefix">${i18n.edit['admin.url.proxy-prefix']}</label>
	<input class="form-control" type="text" id="proxy-prefix" name="proxy-prefix" value="${currentContext.proxyPathPrefix}" />	
</div>

</div>
</div>
</fieldset>

<fieldset>
<legend>${i18n.edit['admin.mailing.title']}</legend>
<div class="row">
<div class="col-sm-6">
<div class="form-group">		
	<label for="mailing-senders">${i18n.edit['admin.mailing.from']}</label>
	<input class="form-control" type="text" id="mailing-senders" name="mailing-senders" value="${currentContext.mailingSenders}" />	
</div>
<div class="form-group">		
	<label for="mailing-report">${i18n.edit['admin.mailing.report']}</label>
	<input class="form-control" type="text" id="mailing-report" name="mailing-report" value="${currentContext.mailingReport}" />	
</div>

</div><div class="col-sm-6">
<div class="form-group">		
	<label for="mailing-subject">${i18n.edit['admin.mailing.subject']}</label>
	<input class="form-control" type="text" id="mailing-subject" name="mailing-subject" value="${currentContext.mailingSubject}" />	
</div>
<div class="form-group">		
	<label for="mailing-unsubscribe">${i18n.edit['admin.mailing.unsubscribe']}</label>
	<input class="form-control" type="text" id="mailing-unsubscribe" name="mailing-unsubscribe" value="${currentContext.unsubscribeLink}" />	
</div>

</div>
</div>
<!-- POP -->
<div class="row">
<div class="col-sm-6">
<div class="form-group">		
	<label for="mailing-pophost">POP host</label>
	<input class="form-control" type="text" id="mailing-pophost" name="mailing-pophost" value="${currentContext.pophost}" />	
</div>
</div>
<div class="col-sm-4"><div class="form-group">		
	<label for="mailing-popport">POP port</label>
	<input class="form-control" type="number" min="-1" max="65535" step="1" id="mailing-popport" name="mailing-popport" value="${currentContext.popport}" />	
</div></div>
<div class="col-sm-2"><div class="form-group">		
	<label for="mailing-popssl">POP SSL</label>
	<input class="checkbox" type="checkbox" id="mailing-popssl" name="mailing-popssl" ${currentContext.popssl?'checked="checked"':''} />	
</div>
</div>
</div>
<div class="row">
<div class="col-sm-6">
<div class="form-group">		
	<label for="mailing-popuser">POP user</label>
	<input class="form-control" type="text" id="mailing-popuser" name="mailing-popuser" value="${currentContext.popuser}" />	
</div>
</div>
<div class="col-sm-6"><div class="form-group">		
	<label for="mailing-smtppassword">POP password</label>
	<div class="row">
		<div class="col-sm-10">
			<input class="form-control" type="text" id="mailing-poppassword" name="mailing-poppassword" value="" />
		</div><div class="col-sm-2">
			<c:if test="${not empty currentContext.poppassword}">	
			<input type="checkbox" id="mailing-resetpoppassword" name="mailing-resetpoppassword" />
			<label for="mailing-resetpoppassword">reset</label>
			</c:if>
		</div>
	</div>	
	</div></div>
</div>

<!-- /POP -->
<div class="row">
<div class="col-sm-6">
<div class="form-group">		
	<label for="mailing-smtphost">${i18n.edit['admin.mailing.smtphost']}</label>
	<input class="form-control" type="text" id="mailing-smtphost" name="mailing-smtphost" value="${currentContext.smtphost}" />	
</div>
</div>
<div class="col-sm-6"><div class="form-group">		
	<label for="mailing-smtpport">${i18n.edit['admin.mailing.smtpport']}</label>
	<input class="form-control" type="number" min="-1" max="65535" step="1" id="mailing-smtphost" name="mailing-smtpport" value="${currentContext.smtpport}" />	
</div>
</div>
</div>
<div class="row">
<div class="col-sm-6">
<div class="form-group">		
	<label for="mailing-smtphost">${i18n.edit['admin.mailing.smtpuser']}</label>
	<input class="form-control" type="text" id="mailing-smtpuser" name="mailing-smtpuser" value="${currentContext.smtpuser}" />	
</div>
</div>
<div class="col-sm-6"><div class="form-group">		
	<label for="mailing-smtppassword">${i18n.edit['admin.mailing.smtppassword']}</label>
	<div class="row">
		<div class="col-sm-10">
			<input class="form-control" type="text" id="mailing-smtppassword" name="mailing-smtppassword" value="" />
		</div><div class="col-sm-2">
			<c:if test="${not empty currentContext.smtppassword}">	
			<input type="checkbox" id="mailing-resetpassword" name="mailing-resetpassword" />
			<label for="mailing-resetpassword">reset</label>
			</c:if>
		</div>
	</div>	
</div>
</div>
</div>
<div class="row">
<div class="col-sm-6">
<div class="form-group">		
	<label for="mailing-dkimdomain">DKIM domain</label>
	<input class="form-control" type="text" id="mailing-dkimdomain" name="mailing-dkimdomain" value="${currentContext.dkimDomain}" />	
</div>
</div>
<div class="col-sm-6"><div class="form-group">		
	<label for="mailing-dkimselector">DKIM selector</label>
	<input class="form-control" type="text" id="mailing-dkimselector" name="mailing-dkimselector" value="${currentContext.dkimSelector}" />	
</div>
</div>
</div>
<c:if test="${not empty dkimpublickey}">
<div class="row">
<div class="col-sm-12"><div class="form-group">		
	<label for="mailing-dkimkey">DKIM public key</label>
	<div class="row">
		<div class="col-sm-11">
			<input class="form-control" type="text" id="mailing-dkimkey" value="${dkimpublickey}" />
		</div><div class="col-sm-1">
			<input class="btn btn-xs btn-default pull-right" type="submit" value="reset" name="resetdkim" />
		</div>
	</div>
	<label for="mailing-dkimkey">DKIM DNS entry</label>
	<input class="form-control" type="text" id="mailing-dkimkey" value='${currentContext.dkimSelector}._domainkey           IN TXT    ( "k=rsa; t=s; p=${dkimpublickey}" )' />
	</div>
</div>
</div>
</c:if>
</fieldset>
<fieldset>
<legend>${i18n.edit['admin.title.special-config']}</legend>
<div class="form-group">
<textarea class="form-control" id="specialconfig" rows="10" cols="10" name="specialconfig">${currentContext.specialConfig}</textarea>
</div>
</fieldset>
<fieldset>
<legend>${i18n.edit['admin.title.html']}</legend>
<div class="row">
<div class="col-sm-4">
	<div class="form-group">
		<label for="meta-bloc">${i18n.edit['admin.title.html.meta']}</label>
		<textarea class="form-control"  rows="10" cols="30" id="meta-bloc" name="meta-bloc">${currentContext.metaBloc}</textarea>
	</div>
</div>
<div class="col-sm-4">
	<div class="form-group">
		<label for="header-bloc">${i18n.edit['admin.title.html.header']}</label>
		<textarea class="form-control"  rows="10" cols="30" id="header-bloc" name="header-bloc">${currentContext.headerBloc}</textarea>
	</div>
</div>
<div class="col-sm-4">
	<div class="form-group">
		<label for="footer-bloc">${i18n.edit['admin.title.html.footer']}</label>
		<textarea class="form-control"  rows="10" cols="30" id="footer-bloc" name="footer-bloc">${currentContext.footerBloc}</textarea>
	</div>
</div>
</div>

</fieldset>
<fieldset>
<legend>${i18n.edit['global.security']}</legend>
<div class="row">
<div class="col-sm-6">
<div class="form-group">		
	<label for="user-factory">${i18n.edit['admin.form.user-factory']}</label>
	<input class="form-control" type="text" id="user-factory" name="user-factory" value="${currentContext.userFactoryClassName}" />	
</div>

<div class="form-group">		
	<label for="admin-user-factory">${i18n.edit['admin.form.admin-user-factory']}</label>
	<input class="form-control" type="text" id="admin-user-factory" name="admin-user-factory" value="${currentContext.adminUserFactoryClassName}" />	
</div>

<div class="form-group">		
	<label for="user-roles">${i18n.edit['admin.form.user-roles']}</label>
	<input class="form-control" type="text" id="user-roles" name="user-roles" value="${currentContext.userRoles}" />	
</div>

<div class="form-group">		
	<label for="admin-user-roles">${i18n.edit['admin.form.admin-user-roles']}</label>
	<input class="form-control" type="text" id="admin-user-roles" name="admin-user-roles" value="${currentContext.adminUserRoles}" />	
</div>

<div class="form-group">		
	<label for="block-password">${i18n.edit['admin.form.block-password']}</label>
	<input class="form-control" type="text" id="block-password" name="block-password" value="${currentContext.blockPassword}" />	
</div>

</div><div class="col-sm-6">

<div class="checkbox">		
	<label><input type="checkbox" id="only-creator-modify" name="only-creator-modify" ${currentContext.onlyCreatorModify?'checked="checked"':""} />
	${i18n.edit['admin.form.only-creator-modify']}</label>
</div>

<div class="checkbox">	
	<label><input type="checkbox" id="collaborative-mode" name="collaborative-mode" ${currentContext.collaborativeMode?'checked="checked"':""} />
	${i18n.edit['admin.form.collaborative-mode']}</label>
</div>

<div class="checkbox">	
	<label><input type="checkbox" id="security-forced-https" name="security-forced-https" ${currentContext.forcedHttps?'checked="checked"':""} />
	${i18n.edit['admin.form.security-forced-https']}</label>
</div>

<div class="checkbox">	
	<label><input type="checkbox" id="security-backup-thread" name="security-backup-thread" ${currentContext.backupThread?'checked="checked"':""} />
	${i18n.edit['admin.form.security-backup-thread']}</label>
</div>

<div class="checkbox">	
	<label><input type="checkbox" id="security-portail" name="security-portail" ${currentContext.portail?'checked="checked"':""} />
	${i18n.edit['admin.form.security-portail']}</label>
</div>


<div class="form-group">		
	<label for="users-access">${i18n.edit['admin.form.users-access']}</label>
	<textarea class="form-control" id="users-access" name="users-access">${currentContext.usersAccess}</textarea>	
</div>

<c:if test="${not empty qrcode}">
<div class="form-group">
	<label for="qrcode">${i18n.edit['admin.form.mobil-access']}</label>
	<button class="btn btn-default" id="qrcode">QRcode</button>
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
</div>

</fieldset>

<fieldset class="macros">
<legend>${i18n.edit['admin.title.macros']}</legend>
<c:forEach var="macro" items="${macros}">
	<div class="col-lg-3 col-sm-6 small-col">
		<div class="checkbox">
		<label title="${macro.info}"><input type="checkbox" id="${macro}" name="${macro}" ${not empty selectedMacros[macro.name]?'checked="checked"':''} />
		<c:set var="i18nKey" value="macro.${macro}" />
		${not empty i18n.edit[i18nKey]?i18n.edit[i18nKey]:macro}</label>
		</div>		
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
        	<c:if test="${!template.template.fake}">
            <img src="${template.previewUrl}" alt="${template.name}" />
            </c:if><c:if test="${template.template.fake}">
            <span class="fake-img"><i class="fa fa-question-circle" aria-hidden="true"></i></span>
            </c:if>
            <div class="info">
                <p>
                    <label>${i18n.edit['global.name']}</label>
                    <span>${template.name}</span>
                </p>                             
                <p>
                    <label>${i18n.edit['global.date']}</label>
                    <span>${template.creationDate}</span>
                </p>
               <c:if test="${!template.template.fake}"> <p>
					<a href="${template.downloadURL}">${i18n.edit['admin.download-template']}</a>
                </p></c:if>
                <p class="menu">
                     <c:if test="${!template.template.fake}"><a href="${template.viewUrl}" class="view" title="${template.name}"></a></c:if>
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
<div class="row">
<c:forEach var="plugin" items="${templatePlugins}">
	<div class="col-lg-3 col-sm-6 small-col">
		<div class="checkbox">
			<label><input type="checkbox" id="${plugin.id}" name="${plugin.id}" ${not empty selectedTemplatePlugins[plugin.id]?'checked="checked"':''}/>${plugin.label} - ${plugin.version}</label>
		</div>		
	</div>
</c:forEach>
</div>
<div class="form-group">
<label for="plugin-config">${i18n.edit['admin.title.template-plugin-config']}</label>
<textarea class="form-control" id="plugin-config" rows="10" cols="10" name="template-plugin-config">${templatePluginConfig}</textarea>
</div>
</fieldset>
<c:if test="${globalContext.staticConfig.integrityCheck}">
<fieldset>
<legend>${i18n.edit['admin.title.integrity']}</legend>
<textarea class="form-control" id="integrity" rows="10" cols="10" name="integrity">${globalContext.contentIntegrity}</textarea>
</fieldset>
</c:if>
<jsp:include page="graphic_charter.jsp" />
<div class="pull-right">
	<a href="${info.currentURL}?webaction=removeSite&removed-context=${currentContext.key}" class="btn btn-default warning needconfirm" title="${i18n.edit['admin.button.remove']}"><span>${i18n.edit['admin.button.remove']}</span></a>
	<button type="submit" name="back" class="btn btn-default">${i18n.edit['global.back']}</button>
	<button type="submit" class="btn btn-primary">${i18n.edit['global.ok']}</button>
</div>

</form>
</c:if>

</div>
</div>

