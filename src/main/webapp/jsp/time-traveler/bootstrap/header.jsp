<%@ taglib uri="jakarta.tags.core" prefix="c"%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"%><%@ taglib uri="jakarta.tags.core" prefix="c"%><%@ taglib prefix="fn" uri="jakarta.tags.functions"%><%@ taglib prefix="fn" uri="jakarta.tags.functions"%><%@page contentType="text/html" import="
    	    org.javlo.helper.XHTMLHelper,
    	    org.javlo.helper.URLHelper,
    	    org.javlo.helper.MacroHelper,
    	    org.javlo.component.core.IContentVisualComponent,
    	    org.javlo.helper.XHTMLNavigationHelper,
    	    org.javlo.context.ContentContext,
    	    org.javlo.service.ContentService,
    	    org.javlo.module.core.ModulesContext,
    	    org.javlo.context.GlobalContext,
    	    org.javlo.module.content.Edit,
    	    org.javlo.message.MessageRepository,
    	    org.javlo.user.AdminUserFactory,
    	    org.javlo.message.GenericMessage,
    	    org.javlo.navigation.MenuElement"%>
<%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
ContentContext returnEditCtx = new ContentContext(editCtx);
returnEditCtx.setEditPreview(false);
AdminUserFactory fact = AdminUserFactory.createAdminUserFactory(ctx.getGlobalContext(), request.getSession());
ContentService content = ContentService.getInstance(ctx.getGlobalContext());
String readOnlyPageHTML = "";
String readOnlyClass = "access";
String accessType = "submit";
boolean rightOnPage = Edit.checkPageSecurity(ctx);
if (!rightOnPage) {
	readOnlyPageHTML = " disabled";
	readOnlyClass = "no-access";
	accessType = "button";
}
%><c:set var="logged" value="${not empty info.editUser}" />
<c:set var="pdf" value="${info.device.code == 'pdf'}" />
<c:url var="urlPageProperties" value="<%=URLHelper.createURL(editCtx)%>" context="/">
	<c:param name="module" value="content" />
	<c:param name="webaction" value="changeMode" />
	<c:param name="mode" value="3" />
	<c:param name="previewEdit" value="true" />
</c:url>
<c:if test="${logged}">
	<div class="header">
		<div class="bloc-start">
			<div class="top-sidebar">
				<div class="_jv_ajax-loading">
					<div class="spinner_container">
						<div class="_jv_spinner" role="status">
							<span class="sr-only">Loading...</span>
						</div>
					</div>
				</div>
				<div class="name _jv_collapse-container _jv_ajax-hide-on-loading" data-jv-toggle="collapse" data-jv-target="#admin-list">${info.javloLogoHtml}</div>
				<ul>
					<li class="page-title"><span class="page">${info.page.name}</span></li>
				</ul>

			</div>
		</div>
		<div class="menu">
			<div class="pc_line">
				<%
				MenuElement timePage = ctx.getCurrentPage();
				String path = timePage.getPath();
				MenuElement viewPage = content.getNavigation(editCtx).searchChild(editCtx, path);
				String statusKey = "time.status";
				String icon = "<i class='bi bi-file-earmark-check'></i>";
				String messageStatus = "success";
				if (viewPage == null) {
					statusKey += ".deleted";
				} else {
					boolean metadataEquals = timePage.isMetadataEquals(viewPage);
					boolean contentEquals = timePage.isContentEquals(viewPage);
					boolean childrenEquals = timePage.isChildrenEquals(viewPage);
					if (metadataEquals && contentEquals && childrenEquals) {
						statusKey += ".same";
					} else {
						icon = "<i class='bi bi-file-earmark-x'></i>";
						messageStatus = "warning";
						statusKey += ".different.on";
						if (!metadataEquals) {
					statusKey += "-metadata";
						}
						if (!contentEquals) {
					statusKey += "-content";
						}
						if (!childrenEquals) {
					statusKey += "-children";
						}
					}
				}
				pageContext.setAttribute("statusKey", statusKey);
				%><div class="alert-header alert-<%=messageStatus%>"><%=icon%>
					${i18n.edit[statusKey]}
				</div>
			</div>
		</div>
		<div class="time-title">
			&nbsp;
		</div>
	</div>
</c:if>