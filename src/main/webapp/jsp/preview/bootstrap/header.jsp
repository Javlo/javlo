 <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"
%><%@ taglib prefix="fn"
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
    	    org.javlo.user.AdminUserFactory,
    	    org.javlo.message.GenericMessage"
%><%
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
%><c:set var="logged" value="${not empty editUser}" /><c:set var="pdf" value="${info.device.code == 'pdf'}" />
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><div
	class="header">
	<div class="logo">
		<a target="_blank" href="<%=URLHelper.createViewURL(ctx.getPath(), ctx)%>">Javlo</a> <img class="ajax-loading"
			src="${info.ajaxLoaderURL}" alt="loading..." lang="en" />
	</div>
	<%--  <div class="pulse">
	<div id='c'>
  	<div class='s'></div>  
  	</div>
	</div>  --%>

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
				<c:if test="${fn:length(contentContext.deviceNames)>1 && !globalContext.openPlatform}">
					<div class="select">
						<form id="renderers_form" action="${info.currentURL}" method="post">
							<div class="input-wrapper">
								<c:url var="url" value="${info.currentURL}" context="/"><c:param name="${info.staticData.forceDeviceParameterName}" value=""></c:param></c:url>
								<select class="form-control input-sm" id="renderers_button"
									onchange="window.location='${url}'+pjq('#renderers_button option:selected').val();"
									data-toggle="tooltip" data-placement="left"
									title="${i18n.edit['command.renderers']}">
									<c:forEach var="renderer" items="${contentContext.deviceNames}">
										<c:url var="url" value="${info.currentURL}" context="/">
											<c:param name="${info.staticData.forceDeviceParameterName}"
												value="${renderer}"></c:param>
										</c:url><option ${info.device.code eq renderer?' selected="selected"':''}>${renderer}</option>
									</c:forEach>
								</select>
							</div>
						</form>
					</div>
				</c:if>

				<div class="btn-group">
					<ul><c:if test="${!globalContext.openPlatform}">
						<li>
							<c:set var="tooltip" value="" />
							<c:if test="${i18n.edit['command.copy-page.tooltip'] != 'command.copy-page.tooltip'}">
								<c:set var="tooltip" value="data-toggle=\"tooltip\" data-placement=\"left\" title=\"${i18n.edit['command.copy-page.tooltip']}\"" />
							</c:if>						
							<form class="${info.page.pageLocalEmpty || info.page.childrenAssociation?'no-access':''}" id="copy_page" action="${info.currentURL}?webaction=edit.copyPage" method="post">
								<c:if test="${not empty param['force-device-code']}"><input type="hidden" name="force-device-code" value="${param['force-device-code']}" /></c:if>
								<button ${tooltip} id="pc_copy_page" type="submit"
									class="btn btn-default btn-sm" ${info.page.pageLocalEmpty || info.page.childrenAssociation?'disabled="disabled"':''}>
									<span class="glyphicon glyphicon-copy" aria-hidden="true"></span><span
										class="text">${i18n.edit['action.copy-page']}</span>
								</button>
							</form>
						</li>
						<li>
						<c:set var="tooltip" value='' />
						<c:if test="${i18n.edit['command.paste-page.tooltip'] != 'command.paste-page.tooltip'}">
							<c:set var="tooltip" value="data-toggle=\"tooltip\" data-placement=\"left\" title=\"${i18n.edit['command.paste-page.tooltip']}\"" />
						</c:if>						
						<form
								class="${empty info.contextForCopy || !info.page.pageLocalEmpty || info.page.childrenAssociation?'no-access':''}"
								id="paste_page" action="${info.currentURL}" method="post">
								<input type="hidden" name="webaction" value="edit.pastePage" />
								<c:if test="${not empty param['force-device-code']}"><input type="hidden" name="force-device-code" value="${param['force-device-code']}" /></c:if>
								<c:set var="tooltip" value="" />
								<c:if test="${i18n.edit['action.paste-page-preview'] != 'action.paste-page-preview'}">
									<c:set var="tooltip" value="data-toggle=\"tooltip\" data-placement=\"left\" title=\"${i18n.edit['action.paste-page-preview']}\"" />
								</c:if>
								<button ${tooltip} class="btn btn-default btn-sm" id="pc_paste_page"
									type="submit"
									${empty info.contextForCopy || !info.page.pageLocalEmpty || info.page.childrenAssociation?'disabled="disabled"':''}>
									<span class="glyphicon glyphicon-paste" aria-hidden="true"></span><span
										class="text">${i18n.edit['action.paste-page-preview']}</span>
								</button>
							</form></li>
						<li><form
								class="${empty info.contextForCopy || !info.page.pageEmpty?'no-access':''}"
								action="${info.currentURL}" method="get">
								<div class="hidden"><input type="hidden" name="webaction" value="edit.refresh" /></div>
								<c:if test="${not empty param['force-device-code']}"><input type="hidden" name="force-device-code" value="${param['force-device-code']}" /></c:if>
								<c:set var="tooltip" value='' />
								<c:if test="${i18n.edit['global.refresh'] != 'global.refresh'}">
									<c:set var="tooltip" value="data-toggle=\"tooltip\" data-placement=\"left\" title=\"${i18n.edit['global.refresh']}\"" />
								</c:if>
								<button ${tooltip} class="btn btn-default btn-sm btn-refresh"
									id="pc_paste_page" type="submit">
									<span class="glyphicon glyphicon-refresh" aria-hidden="true"></span><span
										class="text">${i18n.edit['global.refresh']}</span>
								</button>
							</form></li></c:if>
						<li><form id="pc_del_page_form" class="<%=readOnlyClass%>"
								action="${info.currentURL}" method="post">
								<div>
									<c:if test="${not empty param['force-device-code']}"><input type="hidden" name="force-device-code" value="${param['force-device-code']}" /></c:if>
									<input type="hidden" value="${info.pageID}" name="page" /> <input
										type="hidden" value="${globalContext.openPlatform?'edit.DeletePage':'edit.movePageToTrash'}" name="webaction" />
									<c:if test="${not empty param['force-device-code']}"><input type="hidden" name="force-device-code" value="${param['force-device-code']}" /></c:if>
									<c:if test="${!info.page.root}">
										<c:set var="tooltip" value="" />
										<c:if test="${i18n.edit['preview.label.delete.tooltip'] != 'preview.label.delete.tooltip'}">
											<c:set var="tooltip" value=' data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'preview.label.delete.tooltip\']}"' />
										</c:if>
										<button ${tooltip} class="btn btn-default btn-sm btn-delete"
											type="submit"
											onclick="if (!confirm('${i18n.edit['menu.confirm-page']}')) return false;">
											<span class="glyphicon glyphicon-trash" aria-hidden="true"></span><span
												class="text">${i18n.edit['menu.delete']}</span>
										</button>
									</c:if>
									<c:if test="${info.page.root}">
										<button class="btn btn-default btn-sm btn-delete" type="button"
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
							<button class="btn btn-default btn-sm" type="submit">
								<span class="glyphicon glyphicon-user" aria-hidden="true"></span>${i18n.edit['global.login']}</button>
						</c:if>
						<input type="hidden" name="backPreview" value="true" />
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
								<button class="btn btn-default btn-sm btn-mode btn-wait-loading" type="submit">
									<span class="glyphicon glyphicon-edit" aria-hidden="true"></span><span class="text">${i18n.edit['preview.label.edit-page']}</span></button>
							</c:if>
							<c:if test='${editPreview}'>
								<button class="btn btn-default btn-sm btn-mode btn-wait-loading" type="submit">
									<span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span><span class="text">${i18n.edit['preview.label.not-edit-page']}</span></button>
							</c:if>
						</div>
					</form>
				</c:if> <c:if test="${!globalContext.previewMode}">
					<div class="link-wrapper">						
						<a class="btn btn-default btn-sm btn-mode btn-wait-loading" href="${info.currentViewURLWidthDevice}"
							target="_blank"><span class="glyphicon glyphicon-eye-open"
							aria-hidden="true"></span>${i18n.edit['preview.label.not-edit-page']}</a>
					</div>
				</c:if></li>
			<c:if test="${!pdf && userInterface.mailing}">				
				<li><c:url var="url" value="<%=URLHelper.createURL(editCtx)%>"
						context="/">
						<c:param name="module" value="mailing"></c:param>
						<c:param name="previewEdit" value="true"></c:param>
						<c:param name="wizardStep" value="2"></c:param>
						<c:param name="box" value="sendwizard"></c:param>
						<c:param name="webaction" value="mailing.wizard"></c:param>
					</c:url>
					<form>
						<div class="btn-group">
						<c:if test="${userInterface.sendMailing}">
						<c:set var="tooltip" value="" />
						<c:if test="${i18n.edit['command.mailing.tooltip'] != 'command.mailing.tooltip'}">
							<c:set var="tooltip" value='data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'command.mailing.tooltip\']}"' />
						</c:if>
						<button ${tooltip} class="btn btn-default btn-sm btn-send btn-color btn-wait-loading"
							type="<%=accessType%>"
							value="${i18n.edit['preview.label.mailing']}"
							onclick="editPreview.openModal('${i18n.edit['preview.label.mailing']}','${url}'); return false;">
							<span class="glyphicon glyphicon-send" aria-hidden="true"></span><span class="text">${i18n.edit['preview.label.mailing']}</span>
						</button>
						</c:if>
					<c:url var="url" value="<%=URLHelper.createURL(editCtx)%>"
						context="/">
						<c:param name="module" value="mailing"></c:param>
						<c:param name="wizardStep" value="4"></c:param>
						<c:param name="webaction" value="mailing.wizard"></c:param>
						<c:param name="box" value="sendwizard"></c:param>												
						<c:param name="previewEdit" value="true"></c:param>
					</c:url>
					<c:set var="tooltip" value='' />
					<c:if test="${i18n.edit['command.export.tooltip'] != 'command.export.tooltip'}">
							<c:set var="tooltip" value='data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'command.export.tooltip\']}"' />
					</c:if>
						<button ${tooltip} class="btn btn-default btn-sm btn-export btn-color btn-wait-loading"
							type="<%=accessType%>"
							value="${i18n.edit['preview.label.export-mailing']}"
							onclick="editPreview.openModal('${i18n.edit['preview.label.export-mailing']}','${url}'); return false;">
							<span class="glyphicon glyphicon-export" aria-hidden="true"></span><span class="text">${i18n.edit['preview.label.export-mailing']}</span>
						</button>
						</div>
					</form></li>				
			</c:if>			
			<c:if test="${pdf}">
				<li>
				<li><c:url var="lowPDFURL" value="${info.currentPDFURL}" context="/">
						<c:param name="lowdef" value="true" />
					</c:url><form id="export_pdf_page_form"
						action="${info.currentPDFURL}" method="post" target="_blanck">
						<div class="btn-group" role="group"><button class="btn btn-default btn-sm btn-pdf btn-color btn-wait-loading"
							id="export_pdf_button" type="submit"
							value="${i18n.edit['preview.label.pdf']}"><span class="glyphicon glyphicon-open-file" aria-hidden="true"></span><span class="text">${i18n.edit['preview.label.pdf']}</span></button>
							<a href="${lowPDFURL}" class="btn btn-default btn-sm btn-pdf btn-color btn-wait-loading"
							id="export_pdf_button" type="submit" target="_blank"><span class="glyphicon glyphicon-leaf" aria-hidden="true"></span><span class="text">${i18n.edit['preview.label.pdf.low']}</span></a></div>
					</form>					
					</li>
			</c:if>
			<li class="undo${contentContext.canUndo?'':' no-access'}"><form
								class="${!info.page.pageLocalEmpty?'no-access':''}"
								action="${info.currentURL}" method="get">
								<c:if test="${not empty param['force-device-code']}"><input type="hidden" name="force-device-code" value="${param['force-device-code']}" /></c:if>
								<div class="hidden"><input type="hidden" name="webaction" value="time.undoRedo" /><input type="hidden" name="previous" value="true" /></div>
								<c:set var="tooltip" value="" />
								<c:if test="${i18n.edit['command.undo.tooltip'] != 'command.undo.tooltip'}">
									<c:set var="tooltip" value='data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'command.undo.tooltip\']}"' />
								</c:if>								
								<button class="btn btn-default btn-sm btn-refresh"
									id="pc_paste_page" type="submit" ${contentContext.canUndo?'':' disabled="disabled"'} ${tooltip}>
									<span class="fa fa-undo" aria-hidden="true"></span>
										<span class="text">${i18n.edit['global.undo']}</span>
								</button>
							</form></li>
			<c:if test="${globalContext.previewMode}">
				<li class="publish"><form id="pc_publish_form"
						action="${info.currentURL}" method="post">
						<input type="hidden" name="webaction" value="edit.publish" />
						<c:set var="tooltip" value="" />
						<c:if test="${i18n.edit['command.publish.tooltip'] != 'command.publish.tooltip'}">
							<c:set var="tooltip" value='data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'command.publish.tooltip\']}"' />
						</c:if>
						<button type="submit" class="btn btn-default btn-sm" ${tooltip}>
							<span class="glyphicon glyphicon-arrow-up" aria-hidden="true"></span>
							<span class="text">${i18n.edit['command.publish']}</span>
						</button>
					</form></li>
			</c:if>
			</c:if>
			<c:if test="${userInterface.search}">
			<li><c:url var="url" value="<%=URLHelper.createURL(editCtx)%>"
						context="/">
						<c:param name="module" value="search"></c:param>
						<c:param name="previewEdit" value="true"></c:param>
					</c:url>
					<c:set var="tooltip" value="" />
					<c:if test="${i18n.edit['preview.label.search.tooltip'] != 'preview.label.search.tooltip'}">
						<c:set var="tooltip" value=' data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'preview.label.search.tooltip\']}"' />
					</c:if>
					<button ${tooltip} class="btn btn-default btn-sm btn-search btn-color btn-notext badged"
						type="<%=accessType%>"
						value="${i18n.edit['preview.label.search']}"
						onclick="editPreview.openModal('${i18n.edit['preview.label.search']}','${url}'); return false;"${tooltip}>
						<span class="glyphicon glyphicon-search" aria-hidden="true"></span><span class="text">${i18n.edit['preview.label.search']}</span>
					</button>					
					</li>
			</c:if>
			<c:if test="${!pdf && userInterface.ticket}">
				<li><c:url var="url" value="<%=URLHelper.createURL(editCtx)%>"
						context="/">
						<c:param name="module" value="ticket"></c:param>
						<c:param name="previewEdit" value="true"></c:param>
					</c:url>
					<form>
						<c:set var="tooltip" value="" />
						<c:if test="${i18n.edit['preview.label.ticket.tooltip'] != 'preview.label.ticket.tooltip'}">
							<c:set var="tooltip" value=' data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'preview.label.ticket.tooltip\']}"' />
						</c:if>
						<button ${tooltip} class="btn btn-default btn-sm btn-tickets btn-color btn-notext badged"
							type="<%=accessType%>"
							value="${i18n.edit['preview.label.ticket']}"
							onclick="editPreview.openModal('${i18n.edit['preview.label.ticket']}','${url}'); return false;"${tooltip}>
							<span class="glyphicon glyphicon-question-sign" aria-hidden="true"></span><span class="text">${i18n.edit['preview.label.ticket']}</span>
							<c:set var="unreadTicketsize" value="${fn:length(info.unreadTickets)}" />
							<c:if test="${unreadTicketsize>0}"><div class="badge unread-count">${unreadTicketsize}</div></c:if>					
						</button>
				</form></li>
			</c:if>
						
			<c:if test="${not empty integrities && !globalContext.collaborativeMode && !globalContext.mailingPlatform && logged}">
			<c:if test="${fn:length(integrities.checker)>0}"><li>
			<a class="btn btn-default btn-sm btn-integrity btn-color alert-${integrities.levelLabel} btn-notext badged" data-toggle="_eprv_collapse" data-target="#integrity-list" href="#integrity-list"  aria-expanded="false" aria-controls="integrity-list">
				<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
				<c:if test="${fn:length(integrities.checker)>0}"><div class="badge unread-count">${fn:length(integrities.checker)}</div></c:if>
			</a>			
			<div class="integrity-message collapse${integrities.error && contentContext.previewEdit?' in':''}" id="integrity-list">
				<ul class="list-group"><c:forEach var="checker" items="${integrities.checker}"><c:if test="${checker.errorCount>0}">								
					<li class="list-group-item list-group-item-${checker.levelLabel}">
							<span class="badge">${checker.errorCount}</span>${checker.errorMessage}    									
					</li></c:if></c:forEach>
				</ul>
			</div>
			</li><c:if test="${userInterface.IM}"><li>
			<a class="btn btn-default btn-sm btn-discution btn-color btn-notext badged" data-toggle="_eprv_collapse" data-target="#discution" href="#discution"  aria-expanded="false" aria-controls="discution">
				<span class="glyphicon glyphicon-comment" aria-hidden="true"></span>
				<c:if test="${im.messagesSize>0}"><div class="badge unread-count">${im.messagesSize}</div></c:if>
			</a>
			<div class="discution collapse" id="discution">
				<jsp:include page="${info.editTemplateFolder}/im.jsp" />
			</div>	
			</li></c:if>
			</c:if>
			<c:if test="${fn:length(integrities.checker)==0}"><li>						
			<a class="btn btn-default btn-sm btn-integrity btn-color alert-success btn-notext" data-toggle="collapse" data-target="#integrity-list" href="#integrity-list"  aria-expanded="false" aria-controls="integrity-list">
				<span class="glyphicon glyphicon-check" aria-hidden="true"></span>
			</a>
			<div class="integrity-message collapse${integrities.error && contentContext.previewEdit?' in':''}" id="integrity-list">
				<ul class="list-group">								
					<li class="list-group-item list-group-item-success">
							${i18n.edit['integrity.no_error']}							
					</li>
				</ul>
			</div></li>
			</c:if></c:if>					
					
			</ul></div><div class="users">
			<c:if test="${not empty editUser}">
				<c:url var="logoutURL" value="<%=URLHelper.createURL(ctx)%>" context="/">
					<c:param name="edit-logout" value="true" />
				</c:url> 
				<c:set var="tooltip" value="" />
				<c:if test="${i18n.edit['preview.label.edit.tooltip'] != 'preview.label.edit.tooltip'}">
					<c:set var="tooltip" value=' data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'preview.label.edit.tooltip\']}"' />
				</c:if>
				<li class="user"><c:if test="${!userInterface.contributor}">
				<a ${tooltip} id="pc_edit_mode_button" class="btn btn-default btn-sm" title="${i18n.edit['global.exit']}" href="<%=URLHelper.createURL(returnEditCtx)%>"><span class="glyphicon glyphicon-briefcase"></span><span class="text">edit</span></a>	
				</c:if>
				<c:set var="tooltip" value="" />
				<c:if test="${i18n.edit['preview.label.user.tooltip'] != 'preview.label.user.tooltip'}">
					<c:set var="tooltip" value=' data-toggle="tooltip" data-placement="left" title="${i18n.edit[\'preview.label.user.tooltip\']}"' />
				</c:if>
				<a ${tooltip} href="${info.currentEditURL}?module=users&webaction=user.changeMode&mode=myself&previewEdit=true" class="as-modal btn btn-default btn-sm"><span class="glyphicon glyphicon-user" aria-hidden="true"></span><span class="text">${info.userName}</span></a>					
				<a class="btn btn-default btn-sm" title="${i18n.edit['global.logout']}" href="${logoutURL}"><span class="glyphicon glyphicon-log-out"></span><span class="text">${i18n.edit["global.logout"]}</span></a>				
				</li>
			</c:if>
			</div>	
</div>