<%@page import="org.javlo.user.AdminUserFactory"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib
	uri="/WEB-INF/javlo.tld" prefix="jv"%><%@ taglib prefix="fn"
	uri="http://java.sun.com/jsp/jstl/functions"%><%@page
	contentType="text/html"
	import="
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
    	    org.javlo.message.GenericMessage"%>
<%
	ContentContext ctx = ContentContext.getContentContext(request, response);
	ContentContext editCtx = new ContentContext(ctx);
	editCtx.setRenderMode(ContentContext.EDIT_MODE);
	ContentContext returnEditCtx = new ContentContext(editCtx);
	AdminUserFactory fact = AdminUserFactory.createAdminUserFactory(ctx.getGlobalContext(), request.getSession());
	String readOnlyPageHTML = "";
	String readOnlyClass = "access";
	String accessType = "submit";
	boolean rightOnPage = Edit.checkPageSecurity(ctx);	
	if (!rightOnPage) {
		readOnlyPageHTML = " disabled";
		readOnlyClass = "no-access";
		accessType = "button";
	}
%><c:set var="logged" value="${not empty editUser}" />
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><div
	class="header">
	<div class="logo">
		<a href="#">Javlo</a> <img class="ajax-loading"
			src="${info.ajaxLoaderURL}" alt="loading..." lang="en" />
	</div>

	<div class="menu">
		<li ${info.page.root?'class="active home"':'class="home"'}><a
			class="home" title="home" href="<%=URLHelper.createURL(ctx, "/")%>"><span
				aria-hidden="true" class="glyphicon glyphicon-home"
				aria-hidden="true"></span></a></li>
		<li
			${!info.page.root?'class="active action-title"':'class="action-title"'}><span
			class="inwrapper"><span class="glyphicon glyphicon-edit"
				aria-hidden="true"></span>${info.template.mailing?'edit mailing':'edit content'}</span></li>
	</div>
	<div class="actions-wrapper">
		<div class="page-title">
			<span class="inwrapper"><h1>${info.page.rootOfChildrenAssociation.title}</h1></span>
		</div>
		<c:if test="${not empty editUser}">
			<div class="actions">
				<c:if test="${fn:length(contentContext.deviceNames)>1}">
					<div class="select">
						<form id="renderers_form" action="${info.currentURL}"
							method="post">
							<div class="input-wrapper">
								<c:url var="url" value="${info.currentURL}" context="/">
									<c:param name="${info.staticData.forceDeviceParameterName}"
										value=""></c:param>
								</c:url>
								<select class="form-control" id="renderers_button"
									onchange="window.location='${url}'+pjq('#renderers_button option:selected').val();"
									data-toggle="tooltip" data-placement="left"
									title="${i18n.edit['command.renderers']}">
									<c:forEach var="renderer" items="${contentContext.deviceNames}">
										<c:url var="url" value="${info.currentURL}" context="/">
											<c:param name="${info.staticData.forceDeviceParameterName}"
												value="${renderer}"></c:param>
										</c:url>
										<option
											${info.device.code eq renderer?' selected="selected"':''}>${renderer}</option>
									</c:forEach>
								</select>
							</div>
						</form>
					</div>
				</c:if>

				<div class="btn-group">
					<ul>
						<li>
							<form class="${info.page.pageEmpty?'no-access':''}"
								id="copy_page"
								action="${info.currentURL}?webaction=edit.copyPage"
								method="post">
								<button id="pc_copy_page" type="submit"
									class="btn btn-default btn-xs">
									<span class="glyphicon glyphicon-copy" aria-hidden="true"></span><span
										class="text">${i18n.edit['action.copy-page']}</span>
								</button>
							</form>
						</li>
						<li><form
								class="${empty info.contextForCopy || !info.page.pageEmpty?'no-access':''}"
								id="paste_page" action="${info.currentURL}" method="post">
								<input type="hidden" name="webaction" value="edit.pastePage" />
								<button class="btn btn-default btn-xs" id="pc_paste_page"
									type="submit"
									${empty info.contextForCopy || !info.page.pageEmpty?'disabled="disabled"':''}>
									<span class="glyphicon glyphicon-paste" aria-hidden="true"></span><span
										class="text">${i18n.edit['action.paste-page-preview']}</span>
								</button>
							</form></li>
						<li><form
								class="${empty info.contextForCopy || !info.page.pageEmpty?'no-access':''}"
								action="${info.currentURL}" method="get">
								<button class="btn btn-default btn-xs btn-refresh"
									id="pc_paste_page" type="submit">
									<span class="glyphicon glyphicon-refresh" aria-hidden="true"></span><span
										class="text">${i18n.edit['global.refresh']}</span>
								</button>
							</form></li>
						<li><form id="pc_del_page_form" class="<%=readOnlyClass%>"
								action="${info.currentURL}" method="post">
								<div>
									<input type="hidden" value="${info.pageID}" name="page" /> <input
										type="hidden" value="edit.deletePage" name="webaction" />
									<c:if test="${!info.page.root}">
										<button class="btn btn-default btn-xs btn-delete"
											type="submit"
											onclick="if (!confirm('${i18n.edit['menu.confirm-page']}')) return false;">
											<span class="glyphicon glyphicon-trash" aria-hidden="true"></span><span
												class="text">${i18n.edit['menu.delete']}</span>
										</button>
									</c:if>
									<c:if test="${info.page.root}">
										<button class="btn btn-default btn-delete" type="button"
											onclick="if (!confirm('${i18n.edit['menu.confirm-page']}')) return false;"
											disabled="disabled"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span>											
											<span class="text">${i18n.edit['menu.delete']}</span>
										</button>
									</c:if>
								</div>
							</form></li>
					</ul>
				</div>
			</div>
		</c:if>
	</div>

	<div class="page-actions">
		<ul>
			<c:if test="${!logged}">
			<li>
				<form id="pc_form" method="post" action="<%=URLHelper.createURL(editCtx)%>">
					<div class="pc_line">							
						<c:if test='${!editPreview}'>
							<button class="btn btn-default btn-xs" type="submit">
								<span class="glyphicon glyphicon-user" aria-hidden="true"></span>${i18n.edit['global.login']}</button>
						</c:if>
					</div>
				</form>
			</li>
			</c:if>
			<c:if test="${logged}">
			<li><c:if test="${globalContext.previewMode}">
					<form id="pc_form" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" name="webaction" value="edit.previewedit" />
							<c:if test='${!editPreview}'>
								<button class="btn btn-default btn-xs" type="submit">
									<span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span>${i18n.edit['preview.label.edit-page']}</button>
							</c:if>
							<c:if test='${editPreview}'>
								<button class="btn btn-default btn-xs" type="submit">
									<span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span>${i18n.edit['preview.label.not-edit-page']}</button>
							</c:if>
						</div>
					</form>
				</c:if> <c:if test="${!globalContext.previewMode}">
					<div class="link-wrapper">
						<a class="btn btn-default btn-xs" href="${info.currentViewURL}"
							target="_blank"><span class="glyphicon glyphicon-eye-open"
							aria-hidden="true"></span>${i18n.edit['preview.label.not-edit-page']}</a>
					</div>
				</c:if></li>
			<c:if test="${!pdf}">
				<li><c:url var="url" value="<%=URLHelper.createURL(editCtx)%>"
						context="/">
						<c:param name="module" value="mailing"></c:param>
						<c:param name="previewEdit" value="true"></c:param>
					</c:url>
					<form>
						<button class="btn btn-default btn-xs btn-send btn-color"
							type="<%=accessType%>"
							value="${i18n.edit['preview.label.mailing']}"
							onclick="editPreview.openModal('${i18n.edit['preview.label.mailing']}','${url}'); return false;">
							<span class="glyphicon glyphicon-send" aria-hidden="true"></span>${i18n.edit['preview.label.mailing']}
						</button>
					</form></li>
			</c:if>
			<c:if test="${pdf}">
				<li>
				<li><form id="export_pdf_page_form"
						action="${info.currentPDFURL}" method="post" target="_blanck">
						<button class="btn btn-default btn-xs btn-pdf btn-color"
							id="export_pdf_button" type="submit"
							value="${i18n.edit['preview.label.pdf']}">${i18n.edit['preview.label.pdf']}</button>
					</form></li>
			</c:if>
			<c:if test="${globalContext.previewMode}">
				<li class="publish"><form id="pc_publish_form"
						action="${info.currentURL}" method="post">
						<input type="hidden" name="webaction" value="edit.publish" />
						<button type="submit" class="btn btn-default btn-xs">
							<span class="glyphicon glyphicon-arrow-up" aria-hidden="true"></span>
							${i18n.edit['command.publish']}
						</button>
					</form></li>
			</c:if>
			</c:if>
			</ul></div><div class="users">
			<c:if test="${not empty editUser}">
				<li class="user"><c:if test="${!userInterface.contributor}">
						<a id="pc_edit_mode_button" class="close" title="${i18n.edit['global.exit']}"
							href="<%=URLHelper.createURL(returnEditCtx)%>">X</a>
					</c:if> <c:url var="url" value="<%=URLHelper.createURL(returnEditCtx)%>"
						context="/">
						<c:param name="edit-logout" value="true" />
					</c:url> <c:if test="${userInterface.contributor}">
						<a
							href="${info.currentEditURL}?module=users&webaction=user.changeMode&mode=myself&previewEdit=true"
							class="as-modal"><span class="glyphicon glyphicon-user"
							aria-hidden="true"></span>${info.userName}</a>
						<a id="pc_edit_mode_button" class="logout"
							title="${i18n.edit['global.logout']}" href="${url}">${i18n.edit["global.logout"]}</a>
					</c:if></li>
			</c:if>
			</div>
		
	
</div>