<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
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
    	    org.javlo.message.MessageRepository,
    	    org.javlo.message.GenericMessage"
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
String bootstrapType = "success";
switch (msg.getType()) {
case GenericMessage.ERROR:
	bootstrapType = "danger";
	break;
case GenericMessage.INFO:
	bootstrapType = "info";
	break;
case GenericMessage.HELP:
	bootstrapType = "info";
	break;
case GenericMessage.ALERT:
	bootstrapType = "warning";
	break;
case GenericMessage.SUCCESS:
	bootstrapType = "success";
	break;
}
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
request.setAttribute("editUser", ctx.getCurrentEditUser());
%><c:set var="pdf" value="${info.device.code == 'pdf'}" /><div id="preview_command" lang="${info.editLanguage}" class="edit-${not empty editUser} ${editPreview == 'true'?'edit':'preview'}">
	<script type="text/javascript">	
		var i18n_preview_edit = "${i18n.edit['component.preview-edit']}";	
		var i18n_first_component = "${i18n.edit['component.insert.first']}";
	</script>
	<c:if test="${not empty messages.globalMessage && messages.globalMessage.type > 0 && not empty messages.globalMessage.message}">
		<div class="alert-wrapper slow-hide"><div class="alert alert-<%=bootstrapType%>">${messages.globalMessage.message}</div></div>
	</c:if>
	<div id="modal-container" style="display: none;">[modal]</div>
	<jsp:include page="bootstrap/header.jsp"></jsp:include>
	<div class="sidebar panel panel-default">
		<div class="panel-body">
			<c:if test="${empty editUser}">
			<div class="panel panel-default">
			<div class="panel-heading">${i18n.edit['login.authentification']}</div>
			<div class="panel-body">
				<form method="post" action="${info.currentURL}" id="_ep_login">
			    	<div class="form-group">
		    			<input type="hidden" name="login-type" value="adminlogin">
		    			<input type="hidden" name="edit-login" value="edit-login">    		    		
		            	<input type="text" name="j_username" id="j_username" class="form-control" placeholder="${i18n.edit['login.user']}">
		            </div><div class="form-group">
		            	<input type="password" name="j_password" id="j_password" class="form-control" placeholder="${i18n.edit['login.password']}">
		            </div>
		            <button class="btn btn-default pull-right">Login</button>	        	        
				</form>
			</div>
			</div>
			</c:if><c:if test="${not empty editUser}">
			<div role="tabpanel">				  
				<ul class="nav nav-tabs" role="tablist">
				  <li role="presentation" class="active"><a href="#_ep_navigation" aria-controls="_ep_navigation" role="tab" data-toggle="tab">Navigation</a></li>
				  <li role="presentation"><a href="#_ep_settings" aria-controls="_ep_settings" role="tab" data-toggle="tab">Settings</a></li>
				  <li role="presentation"><a href="#_ep_content" aria-controls="_ep_content" role="tab" data-toggle="tab">Content</a></li>
				  <li role="presentation"><a href="#_ep_files" aria-controls="_ep_files" role="tab" data-toggle="tab">Resouces</a></li>
				</ul>
				<div class="tab-content">
				  <div role="tabpanel" class="tab-pane fade in active navigation_panel " id="_ep_navigation">
				  <c:if test="${contentContext.currentTemplate.mailing}">							
					<jsp:include page="bootstrap/navigation_mailing.jsp"></jsp:include>
				  </c:if><c:if test="${!contentContext.currentTemplate.mailing}">							
					<jsp:include page="bootstrap/navigation.jsp"></jsp:include>
				   </c:if>
				  </div>
				  <div role="tabpanel" class="tab-pane fade" id="_ep_settings"><jsp:include page="bootstrap/settings.jsp" /></div>
				  <div role="tabpanel" class="tab-pane fade" id="_ep_content"><jsp:include page="bootstrap/component.jsp" /></div>
				  <div role="tabpanel" class="tab-pane fade" id="_ep_files"><jsp:include page="bootstrap/shared_content.jsp" /></div>
				</div>
				<c:set var="tabActive" value="${param._preview_tab}" />
				<c:if test="${not empty tabActive}">
					<script type="text/javascript">pjq(".nav-tabs a[href='#${tabActive}']").tab("show");</script>					
				</c:if>
			</div>
			</c:if>
		</div>
	</div>
	
	<div class="modal fade fancybox-wrapper" id="preview-modal" tabindex="-1" role="dialog" aria-labelledby="previewModalTitle" aria-hidden="true">
	  <div class="modal-dialog modal-full fancybox-skin">
      <div class="fancybox-outer">
  	    <div class="modal-content fancybox-inner">
          <div class="for-fancy">
    	      <div class="modal-header page-title">
    	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
    	        <h4 class="modal-title" id="previewModalTitle">[title]</h4>
    	      </div>
            <div class="box">
      	      <div class="modal-body tabs-edit-fancy">
      	        <iframe id="preview-modal-frame" data-wait="/wait.html" src="/wait.html" ></iframe>
      	      </div>
      	      <div class="modal-footer box-foot">
      	        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>        
      	      </div>
            </div>
          </div>
  	    </div>
      </div>
	  </div>
	</div>
	
</div>	 