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
request.setAttribute("editUser", ctx.getCurrentEditUser());
%><div id="preview_command" lang="${info.editLanguage}" class="edit-${not empty editUser} ${editPreview == 'true'?'edit':'preview'}">
	<div class="header">	
		<nav class="navbar navbar-default">
  			<div class="container-fluid">    
    			<div class="navbar-header">
      				<button type="button" lang="en" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
        			<span class="sr-only" lang="en">Toggle navigation</span>
      				</button>
      				<a class="navbar-brand" href="#">Javlo</a>
    			</div>
			</div>			
			<div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
      			<ul class="nav navbar-nav">
      				<li${info.page.root?' class="active"':''}><a title="home" href="<%=URLHelper.createURL(ctx,"/")%>"><span aria-hidden="true" class="glyphicon glyphicon-home" aria-hidden="true"></span></a></li>
      				<c:if test="${not empty editUser}">        			
        			<li><a href="#">Link</a></li>
        			<li><form id="pc_del_page_form" class="<%=readOnlyClass%>" action="${info.currentURL}" method="post">
						<div>
							<input type="hidden" value="${info.pageID}" name="page"/>
							<input type="hidden" value="edit.deletePage" name="webaction"/>
							<c:if test="${!info.page.root}">
							<input id="pc_del_page_button" type="<%=accessType%>" value="${i18n.edit['menu.delete']}" title="${i18n.edit['menu.delete']}" onclick="if (!confirm('${i18n.edit['menu.confirm-page']}')) return false;"<%=readOnlyPageHTML%> />
							</c:if><c:if test="${info.page.root}">
							<input id="pc_del_page_button" type="button" value="${i18n.edit['menu.delete']}" title="${i18n.edit['menu.delete']}" disabled="disabled" />
							</c:if>
						</div>
					</form></li>
					</c:if>
        		</ul>
        	</div>
		</nav>
	</div>
	<div class="sidebar panel panel-default">
		<div class="panel-body">
			<c:if test="${empty editUser}">
			<div class="panel panel-default">
			<div class="panel-heading">${i18n.edit['login.authentification']}</div>
			<div class="panel-body">
				<form method="post" action="<%=URLHelper.createURL(ctx, "/") %>" id="_ep_login">
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
				  <li role="presentation"><a href="#_ep_settings" aria-controls="_ep_settings" role="tab" data-toggle="tab">Profile</a></li>
				  <li role="presentation"><a href="#_ep_content" aria-controls="_ep_content" role="tab" data-toggle="tab">Content</a></li>
				  <li role="presentation"><a href="#_ep_files" aria-controls="_ep_files" role="tab" data-toggle="tab">Settings</a></li>
				</ul>
				<div class="tab-content">
				  <div role="tabpanel" class="tab-pane fade in active" id="_ep_navigation"><jsp:include page="bootstrap/navigation.jsp" /></div>
				  <div role="tabpanel" class="tab-pane fade" id="_ep_settings">settings</div>
				  <div role="tabpanel" class="tab-pane fade" id="_ep_content"><jsp:include page="bootstrap/component.jsp" /></div>
				  <div role="tabpanel" class="tab-pane fade" id="_ep_files">files</div>
				</div>
			</div>
			</c:if>
		</div>
	</div>
</div>	 