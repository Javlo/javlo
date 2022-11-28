
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><%@page contentType="text/html" import="
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
    	    org.javlo.user.AdminUserFactory,
    	    org.javlo.message.GenericMessage"%>
<%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
ContentContext returnEditCtx = new ContentContext(editCtx);
returnEditCtx.setEditPreview(false);
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
%><c:set var="logged" value="${not empty info.editUser}" />
<c:set var="pdf" value="${info.device.code == 'pdf'}" />
<c:if test="${logged || !globalContext.staticConfig.addButton}">
	<c:if test="${!userInterface.minimalInterface}">
		<div class="header">
			<div class="logo">
				<c:if test="${not empty editUser && !globalContext.mailingPlatform && !userInterface.minimalInterface}">
					<c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">
						<c:param name="module" value="admin"></c:param>
						<c:param name="previewEdit" value="true"></c:param>
					</c:url>
					<a href="#" style="float: right;" onclick="editPreview.openModal('${i18n.edit['preview.label.properties']}','${url}'); return false;"> <i class="bi bi-gear"></i>
					</a>
				</c:if>
				<a target="_blank" href="<%=URLHelper.createViewURL(ctx.getPath(), ctx)%>">Javlo</a>
				<img class="ajax-loading" src="${info.ajaxLoaderURL}" alt="loading..." lang="en" />
			</div>
			<div class="menu">
				<c:if test="${fn:length(info.contentLanguages)>1}">
					<div class="language-list collapse" id="_language-list" aria-expanded="true" style="">
						<c:set var="noemptypage" value="true" />
						<div class="list-group">
							<c:forEach var="page" items="${info.pagesForAnyLanguages}">
								<c:set var="noemptypage" value="${noemptypage && page.realContent}" />
								<a href="${page.url}" class="list-group-item ${page.realContent?'list-group-item-success':'list-group-item-danger'}"> <span class="badge">${page.contentLanguage}</span>${page.contentLanguageName}
								</a>
							</c:forEach>
						</div>
					</div>
					<!-- 		<li class="_language"> -->
					<%-- 			<a class="btn btn-default btn-sm btn-languiages btn-notext ${noemptypage?'':'btn-color alert-warning'}" data-toggle="collapse" data-target="#_language-list" href="#_language-list" aria-expanded="true" aria-controls="_language-list"> --%>
					<%-- 			<i class="fa fa-language"></i><span class="text"> ${info.requestContentLanguageName}</span> --%>
					<!-- 			</a> -->
					<!-- 		</li> -->
					<form action="${info.currentURL}" method="post">
						<input type="hidden" name="webaction" value="edit.changeLanguage" />
						<select class="btn btn-default btn-sm btn-languiages btn-notext _language" name="language" onchange="this.form.submit();">
							<c:forEach var="page" items="${info.pagesForAnyLanguages}">
								<c:set var="noemptypage" value="${noemptypage && page.realContent}" />
								<option title="${page.contentLanguageName}" value="${page.contentLanguage}" class="list-group-item ${page.realContent?'list-group-item-success':'list-group-item-danger'}" ${info.requestContentLanguage==page.contentLanguage?'selected="selected"':''}>${page.contentLanguage}</option>
							</c:forEach>
						</select>
					</form>

					<c:if test="${globalContext.previewMode && !contentContext.asTimeMode}">
						<form id="pc_form" action="${info.currentURL}" method="post">
							<div class="pc_line">
								<input type="hidden" name="webaction" value="edit.previewedit" />
								<div class="btn-group" role="group" aria-label="...">
									<button class="btn btn-default btn-sm btn-mode btn-wait-loading ${editPreview?'active':''}" type="submit">
										<i class="bi bi-pencil-square"></i> <span class="text hidden">${i18n.edit['preview.label.edit-page']}</span>
									</button>
									<button class="btn btn-default btn-sm btn-mode btn-wait-loading ${!editPreview?'active':''}" type="submit">
										<i class="bi bi-eye-fill"></i> <span class="text hidden">${i18n.edit['preview.label.not-edit-page']}</span>
									</button>
								</div>
							</div>
						</form>
					</c:if>

				</c:if>

			</div>

			<div class="page-actions">
				<ul>
					<c:if test="${!logged}">
						<li>
							<form id="pc_form" method="post" action="<%=URLHelper.createURL(editCtx)%>">
								<div class="pc_line">
									<c:if test='${!editPreview}'>
										<button class="btn btn-default btn-sm btn-login" type="submit">
											<span class="glyphicon glyphicon-user" aria-hidden="true"></span>${i18n.edit['global.login']}</button>
									</c:if>
									<input type="hidden" name="backPreview" value="true" />
								</div>
							</form>
						</li>
					</c:if>
					<c:if test="${logged}">
						<li><c:if test="${!globalContext.previewMode && !contentContext.asTimeMode}">
								<div class="link-wrapper">
									<a class="btn btn-default btn-sm btn-mode btn-wait-loading" href="${info.currentViewURLWidthDevice}" target="_blank"><i class="bi bi-eye-fill"></i> ${i18n.edit['preview.label.not-edit-page']}</a>
								</div>
							</c:if></li>

						<c:if test="${pdf}">
							<li>
							<li><c:url var="lowPDFURL" value="${info.currentPDFURL}" context="/">
									<c:param name="lowdef" value="true" />
								</c:url>
								<form id="export_pdf_page_form" action="${info.currentPDFURL}" method="post" target="_blanck">
									<div class="btn-group" role="group">
										<button class="btn btn-default btn-sm btn-pdf btn-color btn-wait-loading" id="export_pdf_button" type="submit" value="${i18n.edit['preview.label.pdf']}">
											<span class="glyphicon glyphicon-open-file" aria-hidden="true"></span><span class="text">${i18n.edit['preview.label.pdf']}</span>
										</button>
										<a href="${lowPDFURL}" class="btn btn-default btn-sm btn-pdf btn-color btn-wait-loading" id="export_pdf_button" type="submit" target="_blank"><span class="glyphicon glyphicon-leaf" aria-hidden="true"></span><span class="text">${i18n.edit['preview.label.pdf.low']}</span></a>
									</div>
								</form></li>
						</c:if>

						<c:if test="${globalContext.previewMode}">

							<c:set var="webaction" value="edit.publish" />
							<c:set var="label" value="${i18n.edit['command.publish']}" />
							<c:if test="${info.page.flowIndex==1}">
								<c:set var="webaction" value="edit.needValidation" />
								<c:set var="label" value="${i18n.edit['command.need-validation']}" />
							</c:if>
							<c:if test="${info.page.flowIndex==2}">
								<c:if test="${info.page.validable}">
									<c:set var="webaction" value="edit.validate" />
									<c:set var="label" value="${i18n.edit['flow.validate']}" />
								</c:if>
								<c:if test="${!info.page.validable}">
									<c:set var="webaction" value="" />
									<c:set var="label" value="${i18n.edit['flow.wait-validation']}" />
								</c:if>
							</c:if>
							<c:if test="${not empty param.button_publish and empty param.previewEdit and info.page.flowIndex>2}">
								<a class="action-button publish ajax" href="${info.currentURL}?webaction=publish&render-mode=1"><span>${i18n.edit['command.publish']}</span></a>
							</c:if>
							
							<%
							if (rightOnPage) {
							%>

							<li class="btn-group"><form id="add_copy_page" action="${info.currentURL}" method="post">
								<div class="pc_line">
									<input type="hidden" name="webaction" value="edit.copypage" />
									<button class="btn btn-default btn-sm btn-paste" type="submit" title="${i18n.edit['preview.label.copy-page']}">
										<i class="bi bi-files"></i>
									</button>
								</div>
							</form>

							<form id="add_paste_page" action="${info.currentURL}" method="post" class="${empty info.contextForCopy || !info.page.pageLocalEmpty || info.page.childrenAssociation?'disabled':''}">
								<div class="pc_line">
									<c:if test="${!(empty info.contextForCopy || !info.page.pageLocalEmpty || info.page.childrenAssociation)}">
										<input type="hidden" name="webaction" value="edit.pastepage" />
										<button class="action btn-wait-loading" type="submit" title="${i18n.edit['preview.label.paste-page']}">
											<i class="bi bi-box-arrow-in-down"></i>
										</button>
									</c:if>
									<c:if test="${empty info.contextForCopy || !info.page.pageLocalEmpty || info.page.childrenAssociation}">
										<button class="btn btn-default btn-sm btn-copy" id="pc_paste_page" type="submit">
											<i class="bi bi-box-arrow-in-down"></i>
										</button>
									</c:if>
								</div>
							</form></li>
							
							<c:if test="${!userInterface.minimalInterface && !contentContext.asTimeMode}">
								<li class="undo${contentContext.canUndo?'':' no-access'}"><form class="${!info.page.pageLocalEmpty?'no-access':''}" action="${info.currentURL}" method="get">
										<c:if test="${not empty param['force-device-code']}">
											<input type="hidden" name="force-device-code" value="${param['force-device-code']}" />
										</c:if>
										<div class="hidden">
											<input type="hidden" name="webaction" value="time.undoRedo" />
											<input type="hidden" name="previous" value="true" />
										</div>
										<c:set var="tooltip" value="" />
										<c:if test="${i18n.edit['command.undo.tooltip'] != 'command.undo.tooltip'}">
											<c:set var="tooltip" value='data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'command.undo.tooltip\']}"' />
										</c:if>
										<button class="btn btn-default btn-sm btn-refresh" id="pc_paste_page" type="submit" ${contentContext.canUndo?'':' disabled="disabled"'} ${tooltip}>
											<i class="bi bi-arrow-counterclockwise"></i> <span class="text">${i18n.edit['global.undo']}</span>
										</button>
									</form></li>
							</c:if>

							<li><form id="pc_del_page_form" class="<%=readOnlyClass%>" action="${info.currentURL}" method="post">
								<div>
									<c:if test="${not empty param['force-device-code']}">
										<input type="hidden" name="force-device-code" value="${param['force-device-code']}" />
									</c:if>
									<input type="hidden" value="${info.pageID}" name="page" />
									<input type="hidden" value="${globalContext.pageTrash?'edit.DeletePage':'edit.movePageToTrash'}" name="webaction" />
									<c:if test="${not empty param['force-device-code']}">
										<input type="hidden" name="force-device-code" value="${param['force-device-code']}" />
									</c:if>
									<c:if test="${!info.page.root}">
										<button class="btn btn-default btn-sm btn-delete" type="submit" onclick="if (!confirm('${i18n.edit['menu.confirm-page']}')) return false;" title="${i18n.edit['menu.delete']}">
											<i class="bi bi-trash"></i>
										</button>
									</c:if>
									<c:if test="${info.page.root}">
										<button class="btn btn-default btn-sm btn-delete" type="button" onclick="if (!confirm('${i18n.edit['menu.confirm-page']}')) return false;" disabled="disabled">
											<i class="bi bi-trash"></i>
										</button>
									</c:if>
								</div>
							</form></li>
							<%
							}
							%>
							
							<c:if test="${fn:length(integrities.checker)>0}">
							<li><a class="btn btn-default btn-sm btn-integrity btn-color alert-${integrities.levelLabel} btn-notext badged" data-toggle="_eprv_collapse" data-target="#integrity-list" href="#integrity-list" aria-expanded="false" aria-controls="integrity-list"> <i class="bi bi-exclamation-triangle"></i> <c:if test="${fn:length(integrities.checker)>0}">
										<div class="badge unread-count">${fn:length(integrities.checker)}</div>
									</c:if>
							</a>
								<div class="integrity-message collapse${integrities.error && contentContext.previewEdit?' in':''}" id="integrity-list">
									<ul class="list-group">
										<c:forEach var="checker" items="${integrities.checker}">
											<c:if test="${checker.errorCount>0}">
												<li class="list-group-item list-group-item-${checker.levelLabel}"><span class="badge">${checker.errorCount}</span>${checker.errorMessage}</li>
											</c:if>
										</c:forEach>
									</ul>
								</div></li>
							<c:if test="${userInterface.IM}">
								<li><a class="btn btn-default btn-sm btn-discution btn-color btn-notext badged" data-toggle="_eprv_collapse" data-target="#discution" href="#discution" aria-expanded="false" aria-controls="discution"> <span class="glyphicon glyphicon-comment" aria-hidden="true"></span> <c:if test="${im.messagesSize>0}">
											<div class="badge unread-count">${im.messagesSize}</div>
										</c:if>
								</a>
									<div class="discution collapse" id="discution">
										<jsp:include page="${info.editTemplateFolder}/im.jsp" />
									</div></li>
							</c:if>
						</c:if>

						</c:if>
					</c:if>

				</ul>
			</div>
			<div class="users">

				<li class="publish"><form id="pc_publish_form" action="${info.currentURL}" method="post">
						<input type="hidden" name="webaction" value="${webaction}" />
						<c:set var="tooltip" value="" />
						<c:if test="${i18n.edit['command.publish.tooltip'] != 'command.publish.tooltip'}">
							<c:set var="tooltip" value='data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'command.publish.tooltip\']}"' />
						</c:if>
						<button type="submit" class="btn btn-default btn-sm" ${tooltip} ${empty webaction?'disabled="disabled"':''}>
							<i class="bi bi-box-arrow-up"></i>
							<c:if test="${globalContext.portail}">
								<span class="flow-status">${info.page.flowIndex}</span>
							</c:if>
							<span class="text">${label}</span>
						</button>
					</form></li>

				<c:if test="${userInterface.search && !userInterface.minimalInterface && !contentContext.asTimeMode}">
					<li><c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">
							<c:param name="module" value="search"></c:param>
							<c:param name="previewEdit" value="true"></c:param>
						</c:url> <c:set var="tooltip" value="" /> <c:if test="${i18n.edit['preview.label.search.tooltip'] != 'preview.label.search.tooltip'}">
							<c:set var="tooltip" value=' data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'preview.label.search.tooltip\']}"' />
						</c:if>
						<button ${tooltip} class="btn btn-default btn-sm btn-search btn-color btn-notext badged" type="<%=accessType%>" value="${i18n.edit['preview.label.search']}" onclick="editPreview.openModal('${i18n.edit['preview.label.search']}','${url}'); return false;" ${tooltip}>
							<i class="bi bi-search"></i> <span class="text">${i18n.edit['preview.label.search']}</span>
						</button></li>
				</c:if>
				<c:if test="${!pdf && userInterface.ticket  && !contentContext.asTimeMode}">
					<li><c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">
							<c:param name="module" value="ticket"></c:param>
							<c:param name="previewEdit" value="true"></c:param>
						</c:url>
						<form class="tooltip-form">
							<c:set var="tooltip" value="" />
							<c:if test="${i18n.edit['preview.label.ticket.tooltip'] != 'preview.label.ticket.tooltip'}">
								<c:set var="tooltip" value=' data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'preview.label.ticket.tooltip\']}"' />
							</c:if>
							<button ${tooltip} class="btn btn-default btn-sm btn-tickets btn-color btn-notext badged" type="<%=accessType%>" value="${i18n.edit['preview.label.ticket']}" onclick="html2canvas(document.querySelector('body')).then(canvas => {editPreview.openModal('${i18n.edit['preview.label.ticket']}','${url}'); editPreview.uploadScreenshot(canvas); });return false;" ${tooltip}>
								<i class="bi bi-question-lg"></i> <span class="text">${i18n.edit['preview.label.ticket']}</span>
								<c:set var="unreadTicketsize" value="${fn:length(info.unreadTickets)}" />
								<c:if test="${unreadTicketsize>0}">
									<div class="badge unread-count">${unreadTicketsize}</div>
								</c:if>
							</button>
						</form></li>
				</c:if>
				<c:if test="${not empty editUser}">
					<c:url var="logoutURL" value="<%=URLHelper.createURL(ctx)%>" context="/">
						<c:param name="edit-logout" value="true" />
					</c:url>
					<c:set var="tooltip" value="" />
					<c:if test="${i18n.edit['preview.label.edit.tooltip'] != 'preview.label.edit.tooltip'}">
						<c:set var="tooltip" value=' data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'preview.label.edit.tooltip\']}"' />
					</c:if>
					<li class="user"><c:if test="${!userInterface.contributor}">
							<c:url var="editURL" value="<%=URLHelper.createURL(returnEditCtx)%>" context="/">
								<c:param name="module" value="content" />
								<c:param name="webaction" value="previewEdit" />
								<c:param name="preview" value="false" />
							</c:url>
							<a ${tooltip} id="pc_edit_mode_button" class="btn btn-default btn-sm" title="${i18n.edit['global.exit']}" href="${editURL}" title="edit mode"><i class="bi bi-tools"></i> <span class="text hidden">admin</span></a>
						</c:if> <c:set var="tooltip" value="" /> <c:if test="${i18n.edit['preview.label.user.tooltip'] != 'preview.label.user.tooltip'}">
							<c:set var="tooltip" value=' data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'preview.label.user.tooltip\']}"' />
						</c:if> <a ${tooltip} href="${info.currentEditURL}?module=users&webaction=user.changeMode&mode=myself&previewEdit=true" class="as-modal btn btn-default btn-sm btn-user"><i class="bi bi-person-circle"></i> <span class="text">${info.adminUserName}</span></a> <a class="btn btn-default btn-sm" title="${i18n.edit['global.logout']}" href="${logoutURL}"><i class="bi bi-box-arrow-right"></i><span class="text hidden">${i18n.edit["global.logout"]}</span></a></li>
				</c:if>
			</div>
		</div>
	</c:if>
</c:if>