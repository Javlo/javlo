<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html" import="
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
	<div class="pc_header">${i18n.edit["preview.command"]}<a id="pc_edit_mode_button" href="<%=URLHelper.createURL(editCtx)%>?module=content">X</a></div>
	<div class="pc_body">
			<c:if test="${not empty currentUser}">
			<form id="pc_logout_form" action="${info.currentURL}" method="post">
				<div class="pc_line">
					<a href="${info.currentURL}?edit-logout=logout">${i18n.edit['login.logout']}</a>
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
					<form id="change_template_form" action="<%=URLHelper.createURL(editCtx)%>?module=template&webaction=changeFromPreview&previewEdit=true" method="post">
						<div class="pc_line">							
							<input id="pc_change_template" type="submit" value="${i18n.edit['preview.label.choose-template']}" title="${i18n.edit['preview.label.choose-template']}" class="pc_edit_true" />
						</div>
					</form>
					<form id="pc_publish_form" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" name="webaction" value="edit.publish" />
							<input id="pc_publish_button" type="submit" value="${i18n.edit['command.publish']}" title="${i18n.edit['command.publish']}" />
						</div>
					</form>
					<form id="pc_del_page_form" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" value="${info.pageID}" name="page"/>
							<input type="hidden" value="edit.deletePage" name="webaction"/>
							<input id="pc_del_page_button" type="submit" value="${i18n.edit['menu.delete']}" title="${i18n.edit['menu.delete']}" onclick="if (!confirm('${i18n.edit['menu.confirm-page']}')) return false;"/>
						</div>
					</form>
				</div>
			</fieldset>
			<c:if test='${editPreview == "true"}'>				
				<form id="insert_page" action="${info.currentURL}" method="post">
					<fieldset>
						<legend>${i18n.edit['menu.new-page-title']}</legend>
						<div class="pc_line">
							<input type="hidden" name="webaction" value="edit.addpage" />
							<input type="text" name="name" value="" />
							<input type="submit" name="add-first" value="${i18n.edit['menu.new-page-first']}" />
							<input type="submit" class="button-right" name="add-last" value="${i18n.edit['menu.new-page-last']}" />
						</div>
					</fieldset>
				</form>
				<form id="children_list" action="${info.currentURL}" method="post">
					<fieldset>
						<legend>${i18n.edit['content.navigation']}</legend>
						<c:if test="${not empty info.parentPageURL}">
							<div class="pc_parent_link">
								<a href="${info.parentPageURL}">[..]</a>
							</div>
						</c:if>							
						<div class="pc_menu">
							<input type="hidden" name="webaction" value="movepreview" />
						<%=XHTMLNavigationHelper.renderDefinedMenu(ctx, false, false)%>
						</div>
					</fieldset>
				</form>
				<form id="pc_macro" action="${info.currentURL}" method="post">
					<fieldset>
						<legend>${i18n.edit['command.macro']}</legend>
						<%=MacroHelper.getXHTMLMacroSelection(ctx, false)%>
					</fieldset>
				</form>
			</c:if>
		</c:if><c:if test="${empty currentUser}">
				<form id="pc_login" name="login" method="post" action="${info.currentURL}">
				<input type="hidden" name="login-type" value="adminlogin">
				<input type="hidden" name="edit-login" value="edit-login">
				<fieldset>
					<legend>${i18n.edit['login.authentification']}</legend>
					<div class="pc_line">
						<label for="login">${i18n.edit["login.user"]} :</label>
					    <div class="pc_input"><input id="login" type="text" name="j_username" value="" /></div>
					</div>
					<div class="pc_line">
						<label for="password">${i18n.edit['login.password']} :</label>
						<div class="pc_input"><input type="password" name="j_password" value="" /></div>
					</div>	
					<input type="submit" name="edit-login" value="${i18n.edit['global.submit']}" />
				</fieldset>
				</form>
			</c:if>
	</div>
	<div class="pc_footer">
	</div>
</div>
<script type="text/javascript">
	if (top.location != document.location) { // iframe ?		
		var olddiv = document.getElementById('preview_command');
		olddiv.parentNode.removeChild(olddiv);
	}
</script>