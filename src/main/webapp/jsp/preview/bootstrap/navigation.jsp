<%@page import="org.javlo.context.ContentContext"%><%@page import="org.javlo.helper.URLHelper"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
%><c:set var="page" value="${info.page}" />
<c:if test="${fn:length(page.children) == 0 and not empty info.parent}">
	<c:set var="page" value="${info.parent}" />
</c:if>
<c:url var="pasteURL" value="${info.currentURL}" context="/">
	<c:param name="webaction" value="edit.insertPage" />
</c:url>
<div class="height-to-bottom">
	<div class="pages web">

		<c:url var="urlPageProperties" value="<%=URLHelper.createURL(editCtx)%>" context="/">
			<c:param name="module" value="content" />
			<c:param name="webaction" value="changeMode" />
			<c:param name="mode" value="3" />
			<c:param name="previewEdit" value="true" />
		</c:url>

		<ul class="navigation">
			<c:set var="asTitle" value="false" />

			<li class="parent title page-root page-item ${info.page.root?'selected':''}">
				<div class="nav-item">
					<a href="${info.rootURL}">${info.globalTitle}
						<i class="bi bi-arrow-90deg-up"></i>
					</a>
					<c:if test="${info.page.root}">
						<div class="page-shortcut">
							<span><i class="bi bi-gear" onclick="editPreview.openModal('Page properties', '${urlPageProperties}'); return false;"></i></span> <span class="btn-integrity alert-${integrities.levelLabel} btn-notext badged" data-toggle="_eprv_collapse" data-target="#integrity-list" href="#integrity-list" aria-expanded="false" aria-controls="integrity-list"> <c:if test="${integrities.levelLabel != 'success'}">
									<i class="bi bi-exclamation-triangle-fill"></i>
								</c:if> <c:if test="${integrities.levelLabel == 'success'}">
									<i class="bi bi-check-circle-fill"></i>
								</c:if>
								<div class="badge-integrity">${fn:length(integrities.checker)}</div>
							</span>
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
					</c:if>
				</div>
			</li>
			<c:if test="${!page.root && page.parent != null && !page.parent.root}">
				<div class="hidden-page-sep">
					<i class="bi bi-three-dots"></i>
				</div>
			</c:if>

			<c:if test="${not empty info.parent}">
				<c:if test="${page.url eq info.currentURL}">
					<c:set var="asTitle" value="true" />
					<li class="page-item parent ${!page.trash?'title':'trash'}"><c:if test="${not empty info.contextForCopy && (child.url eq info.currentURL)}">
						<div class="nav-item">
							<a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}">
								<span class="glyphicon glyphicon-collapse-down" aria-hidden="true"></span>
							</a>
						</div>
						</c:if> <a class="draggable flow-${info.parent.flowIndex}" id="page-${info.parent.name}" data-pageid="${info.parent.id}" href="${info.parent.url}" title="${info.parent.path}">${info.parent.info.label}${info.parent.haveChildren?'...':''}</a></li>
				</c:if>
				<c:if test="${!(page.url eq info.currentURL) && not empty info.parent.parent}">
					<c:set var="asTitle" value="true" />
					<li class="page-item parent ${!info.parent.parent.trash?'title':'trash'}">
						<div class="nav-item">
						<a class="draggable flow-${info.parent.parent.flowIndex}" id="page-${info.parent.parent.name}" href="${info.parent.parent.url}" title="${info.parent.parent.path}">${info.parent.parent.info.label}${info.parent.parent.haveChildren?'...':''}</a>
						</div>
					</li>
				</c:if>
			</c:if>

			<c:forEach var="brother" items="${page.info.previousBrothers}">
				<li class="page-item" ${brother.trash?'class="trash"':''}>
				<div class="nav-item ">
				<a id="page-${brother.name}" class="draggable ${!brother.trash?'editor':'trash'} ${brother.active?'active':'unactive'} flow-${brother.flowIndex}" title="${brother.path}" href="${brother.url}"> ${brother.info.label}${info.parent.parent.haveChildren?'...':''} </a>
				</div>	
				</li>
			</c:forEach>

			<li class="page-item ${page.trash?'trash ':''}${page.url eq info.currentURL?'selected ':''}${!asTitle?' title':''}${page.selected?' selected':''}" id="page-${page.name}"><c:if test="${not empty info.contextForCopy && (page.url eq info.currentURL)}">
					<a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}">
						<span class="glyphicon glyphicon-collapse-down" aria-hidden="true"></span>
					</a>
				</c:if> <c:if test="${!page.root}">
					<span>
						<div class="nav-item editor draggable ${page.active?'active':'unactive'} flow-${page.flowIndex}" data-pageid="${child.id}" title="${page.path}">
							<a href="${page.url}">${page.info.label}${page.haveChildren?'...':''}</a>
							<c:if test="${page.url eq info.currentURL}">
								<div class="page-shortcut">
									<span><i class="bi bi-gear" onclick="editPreview.openModal('Page properties', '${urlPageProperties}'); return false;"></i></span> <span class="btn-integrity alert-${integrities.levelLabel} btn-notext badged" data-toggle="_eprv_collapse" data-target="#integrity-list" href="#integrity-list" aria-expanded="false" aria-controls="integrity-list"> <c:if test="${integrities.levelLabel != 'success'}">
											<i class="bi bi-exclamation-triangle-fill"></i>
										</c:if> <c:if test="${integrities.levelLabel == 'success'}">
											<i class="bi bi-check-circle-fill"></i>
										</c:if>
										<div class="badge-integrity">${fn:length(integrities.checker)}</div>
									</span>
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
							</c:if>
						</div>
					</span>
				</c:if> <c:if test="${page.url eq info.currentURL}">
					<c:if test="${userInterface.navigation}">
						<li class="page-item add-page page-depth-${page.depth}"><form id="form-add-page" action="${info.currentURL}" method="post">
								<input type="hidden" name="webaction" value="edit.addPage" />
								<button class="flex-line btn-full" name="auto-name" type="submit">
									<span>${i18n.edit['navigation.add-page']}...</span> <i class="fa fa-plus-circle"></i>
								</button>
							</form></li>
					</c:if>
				</c:if></li>
			<c:if test="${asTitle}">
				<li class="page-item"><ul class="children sortable">
			</c:if>
			<c:forEach var="child" items="${page.children}">
				<li id="page-${child.name}" class="page-item ${child.trash?'trash ':''}${child.url eq info.currentURL?'selected ':''}${child.info.realContent?'real-content':''} ${fn:length(child.children) > 0?'have-children ':''}"><c:if test="${not empty info.contextForCopy && (child.url eq info.currentURL)}">
						<a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}">
							<span class="glyphicon glyphicon-collapse-down" aria-hidden="true"></span>
						</a>
					</c:if> <span>
						<div id="page-${child.name}" data-pageid="${child.id}" class="nav-item draggable ${child.active?'active':'unactive'} flow-${child.flowIndex}" title="${child.path}">
							<a href="${child.url}" data-pageid="${child.id}" title="${child.path}">
								<span>${child.info.label}${child.haveChildren?'...':''}</span>
							</a>

							<c:if test="${child.url eq info.currentURL}">
								<div class="page-shortcut">
									<span><i class="bi bi-gear" onclick="editPreview.openModal('Page properties', '${urlPageProperties}'); return false;"></i></span> <span class="btn-integrity alert-${integrities.levelLabel} btn-notext badged" data-toggle="_eprv_collapse" data-target="#integrity-list" href="#integrity-list" aria-expanded="false" aria-controls="integrity-list"> <c:if test="${integrities.levelLabel != 'success'}">
											<i class="bi bi-exclamation-triangle-fill"></i>
										</c:if> <c:if test="${integrities.levelLabel == 'success'}">
											<i class="bi bi-check-circle-fill"></i>
										</c:if>
										<div class="badge-integrity">${fn:length(integrities.checker)}</div>
									</span>
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
							</c:if>
						</div>
				</span></li>

				<c:if test="${child.url eq info.currentURL}">
					<c:if test="${userInterface.navigation}">
						<li class="page-item add-page page-depth-${child.depth}"><form id="form-add-page" action="${info.currentURL}" method="post">
								<input type="hidden" name="webaction" value="edit.addPage" />
								<button class="flex-line btn-full" name="auto-name" type="submit">
									<span>${i18n.edit['navigation.add-page']}...</span> <i class="fa fa-plus-circle"></i>
								</button>
							</form></li>
					</c:if>
				</c:if>

			</c:forEach>
			<c:if test="${asTitle}">
		</ul>
		</li>
		</c:if>
		<c:forEach var="brother" items="${page.info.nextBrothers}">
			<li class="page-item" ${brother.trash?'class="trash"':''}>
				<div class="nav-item ">
					<a class="draggable editor" id="page-${brother.name}" title="${brother.path}" href="${brother.url}">${brother.info.label}${info.parent.parent.haveChildren?'...':''}</a>
				</div>
			</li>
		</c:forEach>
		</ul>
	</div>
</div>