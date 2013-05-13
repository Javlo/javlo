<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"
%><%@page contentType="text/html" import="
    	    org.javlo.helper.XHTMLHelper,
    	    org.javlo.helper.URLHelper,
    	    org.javlo.helper.MacroHelper,
    	    org.javlo.component.core.IContentVisualComponent,
    	    org.javlo.helper.XHTMLNavigationHelper,
    	    org.javlo.context.ContentContext"
%><%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
%>
<div id="preview_command" lang="${info.editLanguage}" class="edit-${not empty currentUser}">
	<div class="pc_header">${i18n.edit["preview.command"]}<c:if test="${!userInterface.contributor}"><a id="pc_edit_mode_button" href="<%=URLHelper.createURL(editCtx)%>?module=content&webaction=previewEdit&preview=false">X</a></c:if></div>
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
			<fieldset class="pc_command">
				<legend>${i18n.edit['global.command']}</legend>
				<div class="pc_form_line">			
					<form id="pc_form" action="${info.currentURL}" method="post">						
						<div class="pc_line">
							<input type="hidden" name="webaction" value="edit.previewedit" />
							<c:if test='${editPreview == "false"}'> 
								<input id="pc_edit_button" type="submit" value="${i18n.edit['preview.label.edit-page']}" title="${i18n.edit['preview.label.edit-page']}" class="pc_edit_false" />
							</c:if> 
							<c:if test='${editPreview == "true"}'>
								<input id="pc_edit_button" type="submit" value="${i18n.edit['preview.label.not-edit-page']}" title="${i18n.edit['preview.label.not-edit-page']}" class="pc_edit_true" />
							</c:if>
						</div>				
					</form>
					<form id="pc_publish_form" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" name="webaction" value="edit.publish" />
							<input id="pc_publish_button" type="submit" value="${i18n.edit['command.publish']}" title="${i18n.edit['command.publish']}" />
						</div>
					</form>
					<c:if test='${editPreview == "true"}'>
					<c:if test="${!userInterface.light && !userInterface.contributor}">
					<form class="preview-edit" id="change_template_form" action="<%=URLHelper.createURL(editCtx)%>?module=template&webaction=template.changeFromPreview&previewEdit=true" method="post">
						<div class="pc_line">							
							<input id="pc_change_template" type="submit" value="${i18n.edit['preview.label.choose-template']}" title="${i18n.edit['preview.label.choose-template']}" class="pc_edit_true" />
						</div>
					</form>
					</c:if>
					<form id="pc_del_page_form" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" value="${info.pageID}" name="page"/>
							<input type="hidden" value="edit.deletePage" name="webaction"/>
							<input id="pc_del_page_button" type="submit" value="${i18n.edit['menu.delete']}" title="${i18n.edit['menu.delete']}" onclick="if (!confirm('${i18n.edit['menu.confirm-page']}')) return false;"/>
						</div>
					</form>
					<form class="preview-edit" id="user_info" action="<%=URLHelper.createURL(editCtx)%>?module=users&webaction=user.ChangeMode&mode=myself&previewEdit=true" method="post">
						<div class="pc_line">							
							<input id="pc_user_info" type="submit" value="${i18n.edit['global.account-setting']}" title="${i18n.edit['global.account-setting']}" class="pc_edit_true" />
						</div>
					</form>					
					</c:if>					
				</div>
			</fieldset>
			<c:if test='${editPreview == "true"}'>
				<a class="action-button central-button" href="${info.currentEditURL}?module=content&previewEdit=true" onclick="jQuery.colorbox({href : '${info.currentEditURL}?module=content&previewEdit=true',opacity : 0.6,iframe : true,width : '95%',	height : '95%'}); return false;">${i18n.edit['preview.label.edit-components']}</a>				
				<div id="pc_upload">
					<div id="ajax-loader">&nbsp;</div>
					<div id="upload-zone" data-url="${info.uploadURL}" class="drop-files"><span>${i18n.edit['preview.upload-here']}</span></div>
				</div>
				<c:if test="${not empty components}"><fieldset class="closable">
					<legend>${i18n.edit["component.choose"]}</legend>
					<jsp:include page="component.jsp" />
				</fieldset>
				</c:if>
				<form id="children_list" action="${info.currentURL}" method="post">
					<fieldset class="closable">
						<legend>${i18n.edit['content.navigation']}</legend>
						<jsp:include page="navigation.jsp"></jsp:include>
					</fieldset>
				</form>				
				</c:if>	
				<form id="pc_macro" action="${info.currentURL}" method="post">
					<fieldset class="closable">
						<legend>${i18n.edit['command.macro']}</legend>
						<%=MacroHelper.getXHTMLMacroSelection(ctx, false, true)%>
					</fieldset>
				</form>			
		</c:if><c:if test="${empty currentUser}">		
			    <a class="action-button central-button" href="${info.currentEditURL}" onclick="jQuery.colorbox({href : '${info.currentEditURL}?previewEdit=true',opacity : 0.6,iframe : true,width : '95%',	height : '95%'}); return false;">${i18n.edit['global.login']}</a>
			</c:if>
	</div>
	<a id="preview-delete-zone" href="#" class="hidden">&nbsp;</a>
	<div class="pc_footer">
	</div>
</div>
<script type="text/javascript">
	if (top.location != document.location) { // iframe ?		
		var olddiv = document.getElementById('preview_command');
		olddiv.parentNode.removeChild(olddiv);
	}
</script>