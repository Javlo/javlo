<%@page import="org.javlo.message.GenericMessage"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@page contentType="text/html" import="
    	    org.javlo.helper.XHTMLHelper,
    	    org.javlo.helper.URLHelper,
    	    org.javlo.helper.MacroHelper,
    	    org.javlo.component.core.IContentVisualComponent,
    	    org.javlo.helper.XHTMLNavigationHelper,
    	    org.javlo.context.ContentContext,
    	    org.javlo.module.core.ModulesContext,
    	    org.javlo.context.GlobalContext,
    	    org.javlo.module.content.Edit,
    	    org.javlo.message.MessageRepository"
%><%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
ContentContext returnEditCtx = new ContentContext(editCtx);
returnEditCtx.setEditPreview(false);
GlobalContext globalContext = GlobalContext.getInstance(request);
ModulesContext moduleContext = ModulesContext.getInstance(request.getSession(), globalContext);
MessageRepository msgRepo = MessageRepository.getInstance(request); // load request message

GenericMessage msg = msgRepo.getGlobalMessage();
boolean rightOnPage = Edit.checkPageSecurity(ctx);
msgRepo.setGlobalMessageForced(msg);
String readOnlyPageHTML = "";
String readOnlyClass = "access";
String accessType = "submit";
if (!rightOnPage) {
	readOnlyPageHTML = " disabled";
	readOnlyClass = "no-access";
	accessType = "button";
}
%>
<div id="preview_command" lang="${info.editLanguage}" class="edit-${not empty currentUser} ${editPreview == 'true'?'edit':'preview'}">
	<div class="pc_header"><span class="title">${i18n.edit["preview.command"]}</span>
	<c:if test="${!userInterface.contributor}"><a id="pc_edit_mode_button" title="${i18n.edit['global.exit']}" href="<%=URLHelper.createURL(returnEditCtx)%>?module=content&webaction=previewEdit&preview=false">X</a></c:if>
	<c:if test="${userInterface.contributor}"><a id="pc_edit_mode_button" class="logout" title="${i18n.edit['global.logout']}" href="<%=URLHelper.createURL(returnEditCtx)%>?edit-logout=true">X</a></c:if>	
	</div>
	<div class="pc_body">		    
			<c:if test="${not empty messages.globalMessage && messages.globalMessage.type > 0 && not empty messages.globalMessage.message}">
				<div class="message msg${messages.globalMessage.typeLabel}">${messages.globalMessage.message}</div>
			</c:if>
			<c:if test="${not empty currentUser}">
				<form id="formlogout" name="formlogout" method="post" action="${info.currentURL}">			
				<div class="pc_line">					
					<input type="hidden" name="edit-logout" value="logout" />
					<a class="action-button central-button" href="${info.currentURL}?edit-logout=true" onclick="document.formlogout.submit(); return false;">${i18n.edit['login.logout']}</a>					
				</div>
				</form>						
			<fieldset class="pc_command" id="pc_command">
				<legend>${i18n.edit['global.command']}</legend>
				<ul class="pc_form_line"><li class="refresh"><form id="refresh_form" action="${info.currentURL}" method="post">
						<div class="pc_line">							
							<input id="refresh_button" type="submit" value="${i18n.edit['command.refresh']}" title="${i18n.edit['command.refresh']}" />
							<label for="refresh_button">${i18n.edit['command.refresh']}</label>
						</div>
					</form></li>														
					<li class="preview"><c:if test="${globalContext.previewMode}"><form id="pc_form" action="${info.currentURL}" method="post">						
						<div class="pc_line">
							<input type="hidden" name="webaction" value="edit.previewedit" />
							<c:if test='${!editPreview}'> 
								<input id="pc_edit_button" type="submit" value="${i18n.edit['preview.label.edit-page']}" title="${i18n.edit['preview.label.edit-page']}" class="pc_edit_false" />
								<label for="pc_edit_button">${i18n.edit['preview.label.edit-page']}</label>
							</c:if> 
							<c:if test='${editPreview}'>
								<input id="pc_edit_button" type="submit" value="${i18n.edit['preview.label.not-edit-page']}" title="${i18n.edit['preview.label.not-edit-page']}" class="pc_edit_true" />
								<label for="pc_edit_button">${i18n.edit['preview.label.not-edit-page']}</label>
							</c:if>
						</div>				
					</form></c:if>
					<c:if test="${!globalContext.previewMode}">
						<a id="pc_edit_button" class="pc_edit_true" href="${info.currentViewURL}" target="_blank">${i18n.edit['preview.label.not-edit-page']}</a>
					</c:if>					
					</li><c:if test="${fn:length(contentContext.deviceNames)>1}">
					<li class="renderers"><form id="renderers_form" action="${info.currentURL}" method="get">
						<div class="pc_line">
							<c:url var="url" value="${info.currentURL}" context="/">
								<c:param name="${info.staticData.forceDeviceParameterName}" value=""></c:param>
							</c:url>
							<!-- <select id="renderers_button" onchange="jQuery('#renderers_form').attr('action','${url}'+jQuery('#renderers_button option:selected').val()); console.log('lg='+jQuery('#renderers_form').attr('action')); jQuery('#renderers_form').submit();"> -->
							<select id="renderers_button" onchange="window.location='${url}'+jQuery('#renderers_button option:selected').val();">
								<c:forEach var="renderer" items="${contentContext.deviceNames}">
									<c:url var="url" value="${info.currentURL}" context="/">
										<c:param name="${info.staticData.forceDeviceParameterName}" value="${renderer}"></c:param>
									</c:url>									 
									<option${info.device.code eq renderer?' selected="selected"':''}>${renderer}</option>
								</c:forEach>
							</select>
							<label for="renderers_button">${i18n.edit['command.renderers']}</label>
						</div>
					</form></li></c:if>
					<c:if test='${editPreview == "true"}'>
					<li><form id="home_form" action="${info.rootURL}" method="get">
						<div class="pc_line">							
							<input id="home_button" type="submit" value="${i18n.edit['command.home']}" title="${i18n.edit['command.home']}" />
							<label for="home_button">${i18n.edit['command.home']}</label>
						</div>
					</form></li>					
					<c:if test="${globalContext.previewMode}"><li class="publish"><form id="pc_publish_form" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" name="webaction" value="edit.publish" />
							<input id="pc_publish_button" type="submit" value="${i18n.edit['command.publish']}" title="${i18n.edit['command.publish']}" />
							<label for="pc_publish_button">${i18n.edit['command.publish']}</label>
						</div>
					</form></li></c:if>
					<c:if test="${!userInterface.light || globalContext.staticConfig.mailingPlatform}">
					<li><form class="preview-edit <%=readOnlyClass%>" id="change_template_form" action="<%=URLHelper.createURL(editCtx)%>?module=template&webaction=template.changeFromPreview&previewEdit=true" method="post">
						<div class="pc_line">							
							<input id="pc_change_template" type="<%=accessType%>" value="${i18n.edit['preview.label.choose-template']}" title="${i18n.edit['preview.label.choose-template']}" class="pc_edit_true"<%=readOnlyPageHTML%> />
							<label for="pc_change_template">${i18n.edit['preview.label.choose-template']}</label>
						</div>
					</form></li>
					</c:if>
					<%if ( moduleContext.searchModule("mailing") != null ) {%>
					<li><form class="preview-edit <%=readOnlyClass%>" id="mailing_form" action="<%=URLHelper.createURL(editCtx)%>?module=mailing&previewEdit=true" method="post">
						<div class="pc_line">							
							<input id="pc_mailing" type="<%=accessType%>" value="${i18n.edit['preview.label.mailing']}" title="${i18n.edit['preview.label.mailing']}" class="pc_edit_true"<%=readOnlyPageHTML%> />
							<label for="pc_mailing">${i18n.edit['preview.label.mailing']}</label>
						</div>
					</form></li><%
					}%>
					<%if (ctx.getCurrentTemplate().isPDFRenderer()) {%>
					<li><form id="export_pdf_page_form" action="${info.currentPDFURL}" method="post" target="_blanck">
						<div class="pc_line">
							<input id="export_pdf_button" type="submit" value="${i18n.edit['preview.label.pdf']}" title="${i18n.edit['preview.label.pdf']}" />
							<label for="export_pdf_button">${i18n.edit['preview.label.pdf']}</label>
						</div>
					</form></li>
					<%}%>
					<li><form id="pc_del_page_form" class="<%=readOnlyClass%>" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" value="${info.pageID}" name="page"/>
							<input type="hidden" value="edit.deletePage" name="webaction"/>
							<input id="pc_del_page_button" type="<%=accessType%>" value="${i18n.edit['menu.delete']}" title="${i18n.edit['menu.delete']}" onclick="if (!confirm('${i18n.edit['menu.confirm-page']}')) return false;"<%=readOnlyPageHTML%> />
							<label for="pc_del_page_button">${i18n.edit['menu.delete']}</label>
						</div>
					</form></li>
					<li><form class="preview-edit" id="user_info" action="<%=URLHelper.createURL(editCtx)%>?module=users&webaction=user.ChangeMode&mode=myself&previewEdit=true" method="post">
						<div class="pc_line">							
							<input id="pc_user_info" type="submit" value="${i18n.edit['global.account-setting']}" title="${i18n.edit['global.account-setting']}" class="pc_edit_true" />
							<label for="pc_user_info">${i18n.edit['global.account-setting']}</label>
						</div>
					</form></li>
					<li><form class="preview-edit <%=readOnlyClass%>" id="page_properties" action="<%=URLHelper.createURL(editCtx)%>?module=content&webaction=changeMode&mode=3&previewEdit=true" method="post">
						<div class="pc_line">							
							<input id="pc_page_properties" type="<%=accessType%>" value="${i18n.edit['global.page-properties']}" title="${i18n.edit['global.page-properties']}" class="pc_edit_true"<%=readOnlyPageHTML%> />
							<label for="pc_page_properties">${i18n.edit['global.page-properties']}</label>
						</div>
					</form></li>										
					</c:if>
				</ul>			
			</fieldset>
			
			<c:if test='${editPreview}'>
				<!--<a class="action-button central-button" href="${info.currentEditURL}?module=content&previewEdit=true" onclick="jQuery.colorbox({href : '${info.currentEditURL}?module=content&previewEdit=true',opacity : 0.6,iframe : true,width : '95%',	height : '95%'}); return false;">${i18n.edit['preview.label.edit-components']}</a> -->
				<fieldset id="upload-area" class="closable">
				<legend>${i18n.edit['preview.upload-here']}</legend>				
				<div id="pc_upload">
					<div id="ajax-loader">&nbsp;</div>
					<div id="upload-zone" data-url="${info.uploadURL}" class="drop-files"><span>${i18n.edit['preview.upload-here']}</span></div>
				</div>
				</fieldset>
				<c:if test="${not empty sharedContentProviders}">
				<c:if test="${fn:length(sharedContentProviders) > 0}"><fieldset class="shared closable">
					<legend>${i18n.edit["preview.shared-content"]}</legend>					
					<div class="shared-wrapper"><jsp:include page="shared_content.jsp" /></div>
				</fieldset>
				</c:if>
				
				</c:if>
				<c:if test="${not empty components}"><fieldset class="closable">
					<legend>${i18n.edit["component.choose"]}</legend>
					<jsp:include page="component.jsp" />
				</fieldset>
				</c:if>
				<div class="subblock">
				<div id="pc_macro">
					<fieldset class="closable">
						<legend>${i18n.edit['command.macro']}</legend>
						<%=MacroHelper.getXHTMLMacroSelection(ctx, false, true)%>
					</fieldset>
				</div>				
								
				<c:if test="${contentContext.currentTemplate.mailing}">							
					<jsp:include page="navigation_mailing.jsp"></jsp:include>
				</c:if>
				<c:if test="${!contentContext.currentTemplate.mailing}">							
					<jsp:include page="navigation.jsp"></jsp:include>
				</c:if>
							
				</div>
			</c:if>			
		</c:if><c:if test="${empty currentUser}">		
			    <a class="login action-button central-button" href="${info.currentEditURL}" onclick="jQuery.colorbox({href : '${info.currentEditURL}?previewEdit=true',opacity : 0.6,iframe : true,width : '95%',	height : '95%'}); return false;"><span>${i18n.edit['global.login']}</span></a>
			</c:if>
	</div>
	
	<div class="pc_footer">
		<a id="preview-delete-zone" href="#" class="hidden">&nbsp;</a>
		<a href="http://www.javlo.org">javlo.org</a>
    	<c:if test="${!userInterface.light}">    		 
    		<span class="version">2013 - v ${info.version} - ${info.previewVersion}</span> 
    	</c:if>
	</div>
</div>
<script type="text/javascript">
	if (top.location != document.location) { // iframe ?		
		var olddiv = document.getElementById('preview_command');
		olddiv.parentNode.removeChild(olddiv);
	}	
</script>
<!--[if lt IE 10]>
<script type="text/javascript">	
jQuery("#upload-zone").remove();
</script>	
<![endif]--> 