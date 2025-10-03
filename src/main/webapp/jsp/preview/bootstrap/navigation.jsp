<%@page import="org.javlo.context.ContentContext"%>
<%@page import="org.javlo.helper.URLHelper"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<%
	/* =====================================================================
	 * Refactor notes (EN):
	 * - Keep EL operators (eq, ne, and, or) for safety.
	 * - Ensure ALL <c:if>/<c:forEach> blocks are perfectly balanced.
	 * - Avoid nested <ul> inside <li> without wrappers.
	 * - Single place to show add-child / add-page: for the CURRENT page line
	 *   AND for the CURRENT child line.
	 * ===================================================================== */
%>
<%
	ContentContext ctx = ContentContext.getContentContext(request, response);
	ContentContext editCtx = new ContentContext(ctx);
	editCtx.setRenderMode(ContentContext.EDIT_MODE);
%>

<!-- ========================= Helper values ============================= -->
<c:set var="page" value="${info.page}" />
<c:if test="${fn:length(page.children) == 0 and not empty info.parent}">
	<c:set var="page" value="${info.parent}" />
</c:if>

<c:url var="pasteURL" value="${info.currentURL}" context="/">
	<c:param name="webaction" value="edit.insertPage" />
</c:url>

<c:set var="hasMultipleLanguages" value="${fn:length(info.contentLanguages) > 1}" />
<c:set var="showParentSeparator" value="${not page.root and page.parent ne null and not page.parent.root}" />

<div class="height-to-bottom">
	<nav class="pages web" aria-label="Page navigation">
		<ul class="navigation">

			<!-- Root link -->
			<li class="nav-root parent title page-root page-item ${info.page.root ? 'selected' : ''}">
				<div class="nav-item">
					<a href="${info.rootURL}">
						${info.globalTitle}
						<i class="bi bi-arrow-90deg-up" aria-hidden="true"></i>
					</a>

					<!-- Language switcher -->
					<c:if test="${hasMultipleLanguages}">
						<form class="language-list" action="${info.currentURL}" method="post">
							<input type="hidden" name="webaction" value="edit.changeLanguage" />
							<label class="sr-only" for="language-select">Language</label>
							<select id="language-select" class="btn btn-default btn-sm btn-languages btn-notext _language" name="language" onchange="this.form.submit();">
								<c:forEach var="pageLg" items="${info.pagesForAnyLanguages}">
									<option
											title="${pageLg.contentLanguageName}"
											value="${pageLg.contentLanguage}"
											class="list-group-item ${pageLg.realContent ? 'list-group-item-success' : 'list-group-item-danger'}"
										${info.requestContentLanguage eq pageLg.contentLanguage ? 'selected="selected"' : ''}
									>${pageLg.contentLanguage}</option>
								</c:forEach>
							</select>
						</form>
					</c:if>
				</div>
			</li>

			<!-- Visual separator when current has a non-root parent -->
			<c:if test="${showParentSeparator}">
				<li class="hidden-page-sep" aria-hidden="true">
					<i class="bi bi-three-dots"></i>
				</li>
			</c:if>

			<!-- Parent / Grand-parent titles (context trail) -->
			<c:if test="${not empty info.parent}">
				<c:if test="${page.url eq info.currentURL}">
					<!-- Show parent line -->
					<li class="page-item parent ${not page.trash ? 'title' : 'trash'}">
						<div class="nav-item">
							<a
									class="draggable flow-${info.parent.flowIndex} children-flow-${info.parent.childrenFlowIndex}"
									id="page-${info.parent.name}"
									data-pageid="${info.parent.id}"
									href="${info.parent.url}"
									title="${info.parent.path}"
							>
								<span>${info.parent.info.label}${info.parent.haveChildren ? '...' : ''}</span>
							</a>
							<c:if test="${not empty info.contextForCopy}">
								<a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}">
									<i class="bi bi-arrow-down-square" aria-hidden="true"></i>
								</a>
							</c:if>
						</div>
					</li>
				</c:if>
				<c:if test="${not (page.url eq info.currentURL) and not empty info.parent.parent}">
					<!-- Show grand-parent line -->
					<li class="page-item parent ${not info.parent.parent.trash ? 'title' : 'trash'}">
						<div class="nav-item">
							<a
									class="draggable ${not info.parent.parent.trash ? 'editor' : 'trash'} flow-${info.parent.parent.flowIndex} children-flow-${info.parent.parent.childrenFlowIndex}"
									data-pageid="${info.parent.parent.id}"
									id="page-${info.parent.parent.name}"
									href="${info.parent.parent.url}"
									title="${info.parent.parent.path}"
							>
									${info.parent.parent.info.label}${info.parent.parent.haveChildren ? '...' : ''}
							</a>
						</div>
					</li>
				</c:if>
			</c:if>

			<!-- Previous brothers -->
			<c:forEach var="brother" items="${page.info.previousBrothers}">
				<li class="page-item ${brother.trash ? 'trash' : ''}">
					<div class="nav-item">
						<a
								id="page-${brother.name}"
								class="draggable ${not brother.trash ? 'editor' : 'trash'} children-flow-${brother.childrenFlowIndex} ${brother.active ? 'active' : 'unactive'} flow-${brother.flowIndex}"
								data-pageid="${brother.id}"
								title="${brother.path}"
								href="${brother.url}"
						>
								${brother.info.label}${brother.haveChildren ? '...' : ''}
						</a>
					</div>
				</li>
			</c:forEach>

			<!-- Current page line -->
			<li class="page-item ${page.trash ? 'trash ' : ''}${page.url eq info.currentURL ? 'selected ' : ''}${page.selected ? ' selected' : ''}" id="page-${page.name}">
				<c:if test="${not page.root}">
					<div class="nav-item editor draggable ${page.active ? 'active' : 'unactive'} flow-${page.flowIndex}" data-pageid="${page.id}" title="${page.path}">
						<a href="${page.url}" class="flow-${page.flowIndex} children-flow-${page.childrenFlowIndex}">
							<span>${page.info.label}${page.haveChildren ? '...' : ''}</span>
						</a>
						<c:if test="${not empty info.contextForCopy and (page.url eq info.currentURL)}">
							<a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}"><i class="bi bi-arrow-down-square" aria-hidden="true"></i></a>
						</c:if>
					</div>
				</c:if>

				<!-- Inline creators when CURRENT page is selected -->
				<c:if test="${page.url eq info.currentURL and userInterface.navigation}">
					<ul class="inline-creators">
						<li class="page-item add-child page-depth-${page.depth}">
							<form class="button-form" id="form-add-child-current" action="${info.currentURL}" method="post">
								<input type="hidden" name="webaction" value="edit.addChild" />
								<i class="bi bi-arrow-return-right" aria-hidden="true"></i>
								<input name="name" placeholder="${i18n.edit['navigation.add-child']}" type="text" />
								<button type="submit"><i class="fa fa-plus-circle" aria-hidden="true"></i></button>
							</form>
						</li>
						<c:if test="${not info.currentPage.root}">
							<li class="page-item add-page page-depth-${page.depth}">
								<form class="button-form" id="form-add-page-current" action="${info.currentURL}" method="post">
									<input type="hidden" name="webaction" value="edit.addPage" />
									<i class="bi bi-arrow-down" aria-hidden="true"></i>
									<input name="name" placeholder="${i18n.edit['navigation.add-page']}" type="text" />
									<button type="submit"><i class="fa fa-plus-circle" aria-hidden="true"></i></button>
								</form>
							</li>
						</c:if>
					</ul>
				</c:if>
			</li>

			<!-- Children of resolved page -->
			<li class="${info.currentPage.root?'root-children':'page-item'}">
				<ul class="children sortable">

					<c:if test="${info.currentPage.root}">
						<li class="page-item page-depth-${page.depth}">
							<form class="button-form" action="${info.currentURL}" method="post">
								<input type="hidden" name="webaction" value="edit.addChild" />
								<i class="bi bi-arrow-return-right" aria-hidden="true"></i>
								<input name="name" placeholder="${i18n.edit['navigation.add-child']}" type="text" />
								<button type="submit"><i class="fa fa-plus-circle" aria-hidden="true"></i></button>
							</form>
						</li>
					</c:if>

					<c:forEach var="child" items="${page.children}">
						<li class="page-item ${child.trash ? 'trash ' : ''}${child.url eq info.currentURL ? 'selected ' : ''}${child.info.realContent ? 'real-content' : ''} ${fn:length(child.children) > 0 ? 'have-children ' : ''}">
							<div class="nav-item editor draggable">
								<a
										id="page-${child.name}"
										class="draggable ${not child.trash ? 'editor' : 'trash'} ${child.active ? 'active' : 'unactive'} flow-${child.flowIndex} children-flow-${child.childrenFlowIndex}"
										data-pageid="${child.id}"
										title="${child.path}"
										href="${child.url}"
								>
									<span>${child.info.label}${child.haveChildren ? '...' : ''}</span>
								</a>
								<c:if test="${not empty info.contextForCopy and (child.url eq info.currentURL)}">
									<a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}"><i class="bi bi-arrow-down-square" aria-hidden="true"></i></a>
								</c:if>
							</div>

							<!-- Inline creators when the CHILD is current -->
							<c:if test="${child.url eq info.currentURL}">
								<c:if test="${userInterface.navigation}">
									<ul class="inline-creators">
										<li class="page-item add-child page-depth-${page.depth}">
											<form class="button-form" action="${info.currentURL}" method="post">
												<input type="hidden" name="webaction" value="edit.addChild" />
												<i class="bi bi-arrow-return-right" aria-hidden="true"></i>
												<input name="name" placeholder="${i18n.edit['navigation.add-child']}" type="text" />
												<button type="submit"><i class="fa fa-plus-circle" aria-hidden="true"></i></button>
											</form>
										</li>
										<c:if test="${not info.currentPage.root}">
											<li class="page-item add-page page-depth-${page.depth}">
												<form class="button-form" action="${info.currentURL}" method="post">
													<input type="hidden" name="webaction" value="edit.addPage" />
													<i class="bi bi-arrow-down" aria-hidden="true"></i>
													<input name="name" placeholder="${i18n.edit['navigation.add-page']}" type="text" />
													<button type="submit"><i class="fa fa-plus-circle" aria-hidden="true"></i></button>
												</form>
											</li>
										</c:if>
									</ul>
								</c:if>
							</c:if>
						</li>
					</c:forEach>


				</ul>
			</li>

			<!-- Next brothers -->
			<c:forEach var="brother" items="${page.info.nextBrothers}">
				<li class="page-item ${brother.trash ? 'trash' : ''}">
					<div class="nav-item ">
						<a class="draggable ${not brother.trash ? 'editor' : 'trash'} flow-${brother.flowIndex} children-flow-${brother.childrenFlowIndex}" id="page-${brother.name}" title="${brother.path}" href="${brother.url}">
								${brother.info.label}${brother.haveChildren ? '...' : ''}
						</a>
					</div>
				</li>
			</c:forEach>

		</ul>
	</nav>
</div>
