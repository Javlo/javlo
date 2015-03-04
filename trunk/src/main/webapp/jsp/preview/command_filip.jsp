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
	 <div class="wrapper for-header">
        	<div class="container">
            	<div id="header">
                    
                    <div id="logo"><a href="#"></a></div><!-- End-Logo -->
                    <div id="menu">
                        <ul>
                            <li class="home"><a href="index.html" class="tooltip" title="Home"><span>Home</span></a></li>
                            <li class="new active"><a href="#"><span>Create new mailing</span></a></li>
                            <li class="file"><a href="files.html" class="tooltip" title="Files"><span>Files</span></a></li>	
                        </ul>
                    </div><!-- End-Menu -->
                    <div id="presentation">
                        <ul>
                            <li class="icon icon-info"><a href="presentation.html" class="tooltip" title="NewsGate?"></a></li>
                        </ul>
                    </div><!-- End-Presentation -->
                    <div id="admin">
                        <ul>
                            <li class="icon icon-user">Fedulov Filip</li>
                            <li><a href="settings.html">Settings</a></li> 
                            <li><a href="login.html">Logout</a></li>              
                        </ul>
                    </div><!-- End-Admin -->
                    
                </div><!-- End-Header -->
            </div><!-- End-Container -->
        </div><!-- End-Wrapper (For-Header) -->
</div>	 