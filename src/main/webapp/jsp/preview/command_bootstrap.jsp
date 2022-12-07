<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><%@page contentType="text/html" import="
    	    org.javlo.helper.XHTMLHelper,
    	    org.javlo.helper.URLHelper,
    	    org.javlo.helper.MacroHelper,
    	    org.javlo.component.core.IContentVisualComponent,
    	    org.javlo.helper.XHTMLNavigationHelper,
    	    org.javlo.context.ContentContext,
    	    org.javlo.module.core.ModulesContext,
    	    org.javlo.context.GlobalContext,
    	    org.javlo.module.content.Edit,
    	    org.javlo.context.EditContext,
    	    org.javlo.context.UserInterfaceContext,
    	    org.javlo.message.MessageRepository,
    	    org.javlo.message.GenericMessage,
    	    org.javlo.helper.StringHelper"%>
<%
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
	case GenericMessage.ERROR :
		bootstrapType = "danger";
		break;
	case GenericMessage.INFO :
		bootstrapType = "info";
		break;
	case GenericMessage.HELP :
		bootstrapType = "info";
		break;
	case GenericMessage.ALERT :
		bootstrapType = "warning";
		break;
	case GenericMessage.SUCCESS :
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
request.setAttribute("editPreview", EditContext.getInstance(globalContext, session).isPreviewEditionMode());
if (StringHelper.isTrue(request.getParameter("preview-command"), true) && !ctx.isPreviewOnly()) {
%><c:set var="pdf" value="${info.device.code == 'pdf'}" />
<div id="preview_command" lang="${info.editLanguage}" class="edit-${not empty editUser} ${editPreview == 'true'?'edit':'preview'} mode-${globalContext.editTemplateMode}">
	<script type="text/javascript">
		var i18n_preview_edit = "${i18n.edit['component.preview-edit']}";
		var i18n_first_component = "${i18n.edit['component.insert.first']}";
	</script>
	<c:if test="${not empty messages.globalMessage && messages.globalMessage.type > 0 && not empty messages.globalMessage.message}">
		<div class="alert-wrapper slow-hide">
			<div class="alert alert-<%=bootstrapType%>">${messages.globalMessage.message}</div>
		</div>
	</c:if>
	<div id="modal-container" style="display: none;">[modal]</div>

	<jsp:include page="bootstrap/header.jsp"></jsp:include>
	<!-- <jsp:include page="bootstrap/mobile.jsp"></jsp:include> -->
	<c:if test="${editPreview}">
		<c:if test="${!userInterface.minimalInterface}">
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
									</div>
									<div class="form-group">
										<input type="password" name="j_password" id="j_password" class="form-control" placeholder="${i18n.edit['login.password']}">
									</div>
									<button class="btn btn-default pull-right">Login</button>
								</form>
							</div>
						</div>
					</c:if>
					<c:if test="${not empty editUser}">
						<div role="tabpanel">
							<ul class="nav nav-tabs" role="tablist">
								<li role="presentation" class="active"><a href="#_ep_navigation" aria-controls="_ep_navigation" role="tab" data-toggle="tab"><i class="bi bi-diagram-3"></i> Navigator</a></li>
								<li role="presentation" ${!info.pageEditable?'class="disabled"':''}><a href="#_ep_content" aria-controls="_ep_content" role="tab" ${info.pageEditable?'data-toggle="tab"':''}><i class="bi bi-boxes"></i> Elements</a></li>
								<c:if test="${userInterface.previewResourcesTab}">
									<li role="presentation" ${!info.pageEditable?'class="disabled"':''}><a href="#_ep_files" aria-controls="_ep_files" role="tab" ${info.pageEditable?'data-toggle="tab"':''}><i class="bi bi-file-earmark"></i> Files</a></li>
								</c:if>
							</ul>
							<div class="tab-content">
								<div role="tabpanel" class="tab-pane fade in active navigation_panel " id="_ep_navigation">
									<c:if test="${contentContext.currentTemplate.mailing}">
										<jsp:include page="bootstrap/navigation_mailing.jsp"></jsp:include>
									</c:if>
									<c:if test="${!contentContext.currentTemplate.mailing}">
										<jsp:include page="bootstrap/navigation.jsp"></jsp:include>
									</c:if>
								</div>
								<div role="tabpanel" class="tab-pane fade" id="_ep_content"><jsp:include page="bootstrap/component.jsp" /></div>
								<c:if test="${userInterface.previewResourcesTab}">
									<div role="tabpanel" class="tab-pane fade" id="_ep_files"><jsp:include page="bootstrap/shared_content.jsp" /></div>
								</c:if>
							</div>
							<c:set var="tabActive" value="${param._preview_tab}" />
							<c:if test="${not empty tabActive}">
								<script type="text/javascript">
									pjq(".nav-tabs a[href='#${tabActive}']")
											.tab("show");
								</script>
							</c:if>
						</div>
					</c:if>
				</div>
			</div>
		</c:if>
	</c:if>
	<jsp:include page="bootstrap/add.jsp"></jsp:include>
	<div class="modal fade fancybox-wrapper" id="preview-modal" tabindex="-1" role="dialog" aria-labelledby="previewModalTitle" aria-hidden="true">
		<div id="preview-modal-dialog" class="modal-dialog modal-full fancybox-skin">
			<div class="fancybox-outer">
				<div class="modal-content fancybox-inner">
					<div class="for-fancy">
						<div class="modal-header page-title">
							<h4 class="modal-title" id="previewModalTitle">[title]</h4>
							<div class="modal-btn">
								<button type="button" class="maximise-modal" data-modal="modal" aria-label="Close" onclick="document.getElementById('preview-modal-dialog').classList.add('maximized')">
									<span aria-hidden="true"><i class="bi bi-arrows-angle-expand"></i></span>
								</button>
								<button type="button" class="minimise-modal" data-modal="modal" aria-label="Close" onclick="document.getElementById('preview-modal-dialog').classList.remove('maximized')">
									<span aria-hidden="true"><i class="bi bi-arrows-angle-contract"></i></span>
								</button>
								<button type="button" class="close" data-dismiss="modal" aria-label="Close">
									<span aria-hidden="true"><i class="bi bi-x-lg"></i></span>
								</button>
							</div>
						</div>
						<div class="box">
							<div class="modal-preview-body tabs-edit-fancy">
								<iframe id="preview-modal-frame" data-wait="${info.waitURL}" src="${info.waitURL}"></iframe>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="modal fade fancybox-wrapper" id="preview-modal-question" tabindex="-1" role="dialog" aria-labelledby="previewModalTitle" aria-hidden="true">
		<div class="modal-dialog modal-sm fancybox-skin">
			<div class="fancybox-outer">
				<div class="modal-content fancybox-inner">
					<div class="for-fancy">
						<div class="modal-header page-title">
							<button type="button" class="close" data-dismiss="modal" aria-label="Close">
								<span aria-hidden="true"><i class="bi bi-x-lg"></i></span>
							</button>
							<h4 class="modal-title">[title]</h4>
						</div>
						<div class="box">
							<div class="modal-body tabs-edit-fancy">
								<p class="preview-modal-question">[question]</p>
							</div>
							<div class="modal-footer">
								<button type="submit" class="btn btn-default btn-no">[no]</button>
								<button type="submit" class="btn btn-primary btn-yes">[yes]</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<%
}
%>