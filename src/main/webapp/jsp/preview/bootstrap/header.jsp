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
<c:url var="urlPageProperties" value="<%=URLHelper.createURL(editCtx)%>" context="/">
	<c:param name="module" value="content" />
	<c:param name="webaction" value="changeMode" />
	<c:param name="mode" value="3" />
	<c:param name="previewEdit" value="true" />
</c:url>
<c:if test="${logged || !globalContext.staticConfig.addButton}">
	<c:if test="${!userInterface.minimalInterface}">
		<div class="header">
			<div class="bloc-start">
				<div class="top-sidebar">
					<c:if test="${not empty editUser && !globalContext.mailingPlatform && !userInterface.minimalInterface}">
						<c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">
							<c:param name="module" value="admin"></c:param>
							<c:param name="previewEdit" value="true"></c:param>
						</c:url>
					</c:if>
					<div class="_jv_ajax-loading">
						<div class="spinner_container">
							<div class="_jv_spinner" role="status"><span class="sr-only">Loading...</span></div>
						</div>
					</div>
					<a class="name btn-toggle _jv_collapse-container _jv_ajax-hide-on-loading" href="${info.currentViewURL}" data-jv-toggle="collapse" data-jv-target="#admin-list">Javlo</a>
					<div id="admin-list" class="_jv_collapse-target _jv_hidden"><jsp:include page="admin_menu.jsp" /></div>

					<ul>
						<li class="page-title"><a href="#" class="page" onclick="editPreview.openModal('Page properties', '${urlPageProperties}'); return false;">${not empty info.page.pageTitle?info.page.pageTitle:info.page.name}</a> <a href="#" class="template" onclick="editPreview.openModal('Template', '${info.currentEditURL}?module=template&webaction=template.changeFromPreview&previewEdit=true');"> ${info.currentPage.templateId == null?'<i class="bi bi-file-earmark-arrow-down"></i>':'<i class="bi bi-file-earmark-code"></i>'} ${info.page.template} </a></li>
						
					</ul>
					
				</div>
				
				<div class="page-shortcut">
					<%-- 									<span><i class="bi bi-gear" onclick="editPreview.openModal('Page properties', '${urlPageProperties}'); return false;"></i></span> --%>
					<div class="btn btn-integrity btn-notext" data-toggle="_eprv_collapse" data-target="#integrity-list" href="#integrity-list" aria-expanded="false" aria-controls="integrity-list">
						<c:if test="${integrities.levelLabel != 'success'}">
							<i class="bi bi-exclamation-triangle"></i>
						</c:if>
						<c:if test="${integrities.levelLabel == 'success'}">
							<i class="bi bi-check-circle"></i>
						</c:if>
						<div class="integrity-message collapse${integrities.error && contentContext.previewEdit?' in':''}" id="integrity-list">
							<ul class="list-group">
								<c:forEach var="checker" items="${integrities.checker}">
									<c:if test="${checker.errorCount>0}">
										<li class="list-group-item list-group-item-${checker.levelLabel}"><span class="badge">${checker.errorCount}</span>${checker.errorMessage}</li>
									</c:if>
								</c:forEach>
							</ul>
						</div>
					</div>
				</div>

				<c:if test="${contentContext.preview}">
				<div class="page-actions">
					<ul>
						<c:if test="${!logged}">
							<li>
								<form id="pc_form" method="post" action="<%=URLHelper.createURL(editCtx)%>">
									<div class="pc_line">
										<c:if test='${!editPreview}'>
											<button class="btn btn-default btn-login" type="submit">
												<span class="glyphicon glyphicon-user" aria-hidden="true"></span>${i18n.edit['global.login']}</button>
										</c:if>
										<input type="hidden" name="backPreview" value="true" />
									</div>
								</form>
							</li>
						</c:if>
						<c:if test="${logged}">
							<c:if test="${!globalContext.previewMode && !contentContext.asTimeMode}">
								<li>
									<div class="link-wrapper">
										<a class="btn btn-default btn-mode" href="${info.currentViewURLWidthDevice}" target="_blank"><i class="bi bi-eye-fill"></i> ${i18n.edit['preview.label.not-edit-page']}</a>
									</div>
								</li>
							</c:if>

							<c:if test="${pdf}">
								<li><c:url var="lowPDFURL" value="${info.currentPDFURL}" context="/">
										<c:param name="lowdef" value="true" />
									</c:url>
									<form id="export_pdf_page_form" action="${info.currentPDFURL}" method="post" target="_blanck">
										<div class="btn-group" role="group">
											<button class="btn btn-default btn-pdf btn-color btn-wait-loading" id="export_pdf_button" type="submit" value="${i18n.edit['preview.label.pdf']}">
												<span class="glyphicon glyphicon-open-file" aria-hidden="true"></span><span class="text">${i18n.edit['preview.label.pdf']}</span>
											</button>
											<a href="${lowPDFURL}" class="btn btn-default btn-pdf btn-color btn-wait-loading" id="export_pdf_button" type="submit" target="_blank"><span class="glyphicon glyphicon-leaf" aria-hidden="true"></span><span class="text">${i18n.edit['preview.label.pdf.low']}</span></a>
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

								<li class="btn-group">
									<form id="add_copy_page" action="${info.currentURL}" method="post">
										<div class="pc_line">
											<input type="hidden" name="webaction" value="edit.copypage" />
											<button class="btn btn-default btn-copy" type="submit" title="${i18n.edit['preview.label.copy-page']}">
												<i class="bi bi-clipboard-plus"></i>
											</button>
										</div>
									</form>

									<form id="add_paste_page" action="${info.currentURL}" method="post" class="${empty info.contextForCopy || !info.page.pageLocalEmpty || info.page.childrenAssociation?'disabled':''}">
										<div class="pc_line">
											<c:if test="${!(empty info.contextForCopy || !info.page.pageLocalEmpty || info.page.childrenAssociation)}">
												<input type="hidden" name="webaction" value="edit.pastepage" />
												<button class="action btn btn-paste" type="submit" title="${i18n.edit['preview.label.paste-page']}">
													<i class="bi bi-clipboard-check"></i>
												</button>
											</c:if>
											<c:if test="${empty info.contextForCopy || !info.page.pageLocalEmpty || info.page.childrenAssociation}">
												<button class="btn btn-default btn-paste" id="pc_paste_page" type="submit">
													<i class="bi bi-clipboard-check"></i>
												</button>
											</c:if>
										</div>
									</form>
								</li>

								<li id="_jv_clipboard" class="clipboard ${not empty clipboard.copied || not empty editInfo.copiedPage?'':'disabled'}" title="${i18n.edit['global.clipboard']}"><c:url var="url" value="${info.currentURL}" context="/">
										<c:param name="webaction" value="edit.clearClipboard" />
									</c:url> 
									<!-- <span class="title btn"> <i class="bi bi-clipboard"></i></span> -->
									<c:if test="${not empty clipboard.copied || not empty editInfo.copiedPage}">
										<div class="cb-component-list">
											<c:if test="${not empty clipboard.copied}">
												<div class="cb-component" data-type="clipboard" data-deletable="true">
													<div class="wrapper-in">
														<div class="figure">
															<i class="${clipboard.icon}"></i>
														</div>
														<span>${clipboard.label}</span>
														<div class="category">(${i18n.edit['global.clipboard']})</div>
														<a href="${url}" class="ajax close"><i class="bi bi-x"></i></a>
													</div>
												</div>
											</c:if>
											<c:if test="${not empty editInfo.copiedPage}">
												<div class="cb-component page" data-type="clipboard-page" data-deletable="true">
													<div class="wrapper-in" title="${editInfo.copiedPage}">
														<div class="figure">
															<i class="bi bi-file-richtext"></i>
														</div>
														<span>${editInfo.copiedPage}</span>
														<div class="category">(page)</div>
														<a href="${url}" class="ajax close"><i class="bi bi-x"></i></a>
													</div>
												</div>
											</c:if>
										</div>
									</c:if></li>

								<%
								}
								%>

							</c:if>
						</c:if>
					</ul>
				</div>
				</c:if>
			</div>

			<div class="menu">

				<c:if test="${globalContext.previewMode && !contentContext.asTimeMode}">
					<form id="pc_form" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<div class="btn-group toggle-preview-edit" role="group" aria-label="...">
								<button class="btn btn-default btn-mode ${editPreview?'active':''}" type="submit" ${!editPreview?'name="webaction" value="edit.previewedit"':''}>
									<i class="bi bi-pencil-fill"></i> <span class="text hidden">${i18n.edit['preview.label.edit-page']}</span>
								</button>
								<button class="btn btn-default btn-mode ${!editPreview?'active':''}" type="submit" ${editPreview?'name="webaction" value="edit.previewedit"':''}>
									<i class="bi bi-eye-fill"></i> <span class="text hidden">${i18n.edit['preview.label.not-edit-page']}</span>
								</button>
							</div>
						</div>
					</form>
				</c:if>
			</div>

			<div class="users">
			
				<c:if test="${contentContext.time}">
					<div class="preview-version">content version : ${info.previewVersion}</div>
				</c:if>
			
				<c:if test="${contentContext.preview}">
				<li class="publish"><form id="pc_publish_form" action="${info.currentURL}" method="post">
						<input type="hidden" name="webaction" value="${webaction}" />
						<c:set var="tooltip" value="" />
						<c:if test="${i18n.edit['command.publish.tooltip'] != 'command.publish.tooltip'}">
							<c:set var="tooltip" value='data-toggle="tooltip" data-placement="bottom" title="${i18n.edit[\'command.publish.tooltip\']}"' />
						</c:if>
						<button type="submit" class="btn btn-default" ${tooltip} ${empty webaction?'disabled="disabled"':''}>
							<i class="bi bi-cloud-upload"></i>
							<c:if test="${globalContext.portail}">
								<span class="flow-status">${info.page.flowIndex}</span>
							</c:if>
							<span class="text">${label}</span>
						</button>
					</form></li>
				</c:if>

				<c:if test="${userInterface.search && !userInterface.minimalInterface && !contentContext.asTimeMode}">
					<li><c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">
							<c:param name="module" value="search"></c:param>
							<c:param name="previewEdit" value="true"></c:param>
						</c:url> <c:set var="tooltip" value="" /> <c:if test="${i18n.edit['preview.label.search.tooltip'] != 'preview.label.search.tooltip'}">
							<c:set var="tooltip" value=' data-toggle="tooltip" data-placement="bottom" title="${i18n.edit[\'preview.label.search.tooltip\']}"' />
						</c:if>
						<button ${tooltip} class="btn btn-default btn-search btn-color btn-notext badged" type="<%=accessType%>" value="${i18n.edit['preview.label.search']}" onclick="editPreview.openModal('${i18n.edit['preview.label.search']}','${url}'); return false;" ${tooltip}>
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
								<c:set var="tooltip" value=' data-toggle="tooltip" data-placement="bottom" title="${i18n.edit[\'preview.label.ticket.tooltip\']}"' />
							</c:if>
							<script>
								function openTicket() {
									document.getElementById('btn-ticket').classList.add('loading');
									setTimeout(() => {
										html2canvas(document.querySelector('body')).then(canvas => {
											editPreview.openModal('${i18n.edit['preview.label.ticket']}','${url}');
											editPreview.uploadScreenshot(canvas);
											document.getElementById('btn-ticket').classList.remove('loading');
										});
									}, 50); // if no timeout delay before display spinner, I don't no why :-)
									return false;
								}
							</script>
							<button id="btn-ticket" ${tooltip} class="btn btn-default btn-tickets btn-color btn-notext badged" type="<%=accessType%>" value="${i18n.edit['preview.label.ticket']}" onclick="return openTicket()" ${tooltip}>
								<i class="bi bi-question-circle"></i>
								<div class="loader _jv_spinner" role="status">
									<span class="sr-only" lang="en">Loading...</span>
								</div>
								<span class="text">${i18n.edit['preview.label.ticket']}</span>
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
					<li class="macro">
						<button class="btn btn-default btn-toggle _jv_collapse-container" data-jv-toggle="collapse" data-jv-target="#macro-list">
							<i class="bi bi-tools"></i>
						</button>
						<div id="macro-list" class="_jv_collapse-target _jv_hidden">
							<jsp:include page="macro.jsp" />
						</div>
					</li>
					<li class="user _jv_collapse-container"><button class=" btn btn-user btn-toggle" data-jv-target="#_jv_user-collapse" data-jv-toggle="collapse">
							<i class="bi bi-person-circle"></i>
						</button>
						<div id="_jv_user-collapse" class="user-collapse _jv_collapse-target _jv_menu _jv_hidden">
							<a href="${info.currentEditURL}?module=users&webaction=user.changeMode&mode=myself&previewEdit=true" class="as-modal btn btn-default btn-user">
								<div class="button-group-addon">
									<i class="bi bi-person"></i>
								</div> <span class="label">${info.adminUserName}</span>
							</a>
							<c:if test="${!userInterface.contributor}">
								<c:url var="editURL" value="<%=URLHelper.createURL(returnEditCtx)%>" context="/">
									<c:param name="module" value="content" />
									<c:param name="webaction" value="previewEdit" />
									<c:param name="preview" value="false" />
								</c:url>
							</c:if>
							<a class="btn btn-default" title="${i18n.edit['global.logout']}" href="${logoutURL}">
								<div class="button-group-addon">
									<i class="bi bi-box-arrow-right"></i>
								</div> <span class="text label">${i18n.edit["global.logout"]}</span>
							</a>
						</div></li>
				</c:if>
			</div>
		</div>
	</c:if>
</c:if>