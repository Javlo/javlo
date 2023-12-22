<%@page import="org.javlo.context.ContentContext"%><%@page import="org.javlo.helper.URLHelper"%><%@ taglib uri="jakarta.tags.core" prefix="c"%><%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
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

		<ul class="navigation"><c:set var="asTitle" value="false" />
			<li class="nav-root parent title page-root page-item ${info.page.root?'selected':''}">
				<div class="nav-item">
					<a href="${info.rootURL}">${info.globalTitle} <i class="bi bi-arrow-90deg-up"></i></a>
					<c:if test="${fn:length(info.contentLanguages)>1}">
						<form class="language-list" action="${info.currentURL}" method="post">
							<input type="hidden" name="webaction" value="edit.changeLanguage" />
							<select class="btn btn-default btn-sm btn-languages btn-notext _language" name="language" onchange="this.form.submit();">
								<c:forEach var="pageLg" items="${info.pagesForAnyLanguages}">
									<c:set var="noemptypage" value="${noemptypage && pageLg.realContent}" />
									<option title="${pageLg.contentLanguageName}" value="${pageLg.contentLanguage}" class="list-group-item ${pageLg.realContent?'list-group-item-success':'list-group-item-danger'}" ${info.requestContentLanguage==pageLg.contentLanguage?'selected="selected"':''}>${pageLg.contentLanguage}</option>
								</c:forEach>
							</select>
						</form>
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
									<i class="bi bi-arrow-down-square"></i>
								</a>
							</div>
						</c:if>
						<a class="draggable flow-${info.parent.flowIndex}" children-flow-${info.parent.childrenFlowIndex}" id="page-${info.parent.name}" data-pageid="${info.parent.id}" href="${info.parent.url}" title="${info.parent.path}">
							${info.parent.info.label}${info.parent.haveChildren?'...':''}
						</a>
					</li>
				</c:if>
				<c:if test="${!(page.url eq info.currentURL) && not empty info.parent.parent}">
					<c:set var="asTitle" value="true" />
					<li class="page-item parent ${!info.parent.parent.trash?'title':'trash'}">
						<div class="nav-item">
							<a class="draggable flow-${info.parent.parent.flowIndex} children-flow-${info.parent.parent.childrenFlowIndex} ${!brother.trash?'editor':'trash'}" data-pageid="${info.parent.id}" id="page-${info.parent.parent.name}" href="${info.parent.parent.url}" title="${info.parent.parent.path}">
							${info.parent.parent.info.label}${info.parent.parent.haveChildren?'...':''}
							</a>
						</div>
					</li>
				</c:if>
			</c:if>

			<c:forEach var="brother" items="${page.info.previousBrothers}">
				<li class="page-item" ${brother.trash?'class="trash"':''}>
					<div class="nav-item ">
						<a id="page-${brother.name}" class="draggable ${!brother.trash?'editor':'trash'} children-flow-${brother.childrenFlowIndex} ${brother.active?'active':'unactive'} flow-${brother.flowIndex}" data-pageid="${info.parent.id}" title="${brother.path}" href="${brother.url}">
						${brother.info.label}${info.parent.parent.haveChildren?'...':''}
						</a>
					</div>
				</li>
			</c:forEach>

			<li class="page-item ${page.trash?'trash ':''}${page.url eq info.currentURL?'selected ':''}${!asTitle?' title':''}${page.selected?' selected':''}" id="page-${page.name}">
				<c:if test="${!page.root}">
					<div class="nav-item editor draggable ${page.active?'active':'unactive'} flow-${page.flowIndex}" data-pageid="${child.id}" title="${page.path}">
						<a href="${page.url}" class="flow-${page.flowIndex} children-flow-${page.childrenFlowIndex}"><span>${page.info.label}${page.haveChildren?'...':''}</span></a>
						<c:if test="${not empty info.contextForCopy && (page.url eq info.currentURL)}">
							<a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}"> <i class="bi bi-arrow-down-square"></i></a>
						</c:if> 
					</div>
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
				
				
				<li class="page-item ${child.trash?'trash ':''}${child.url eq info.currentURL?'selected ':''}${child.info.realContent?'real-content':''} ${fn:length(child.children) > 0?'have-children ':''}">
					<div class="nav-item editor draggable">
						<a id="page-${child.name}" class="draggable ${!child.trash?'editor':'trash'} ${child.active?'active':'unactive'} flow-${child.flowIndex} children-flow-${child.childrenFlowIndex}" data-pageid="${info.parent.id}" title="${child.path}" href="${child.url}">
						<span>${child.info.label}${child.haveChildren?'...':''}</span>
						<c:if test="${not empty info.contextForCopy && (child.url eq info.currentURL)}"> 
							<a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}"> <i class="bi bi-arrow-down-square"></i></a>
						</c:if>
						</a>
					</div>
				</li>

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
					<a class="draggable editor flow-${brother.flowIndex} children-flow-${brother.childrenFlowIndex}" id="page-${brother.name}" title="${brother.path}" href="${brother.url}">${brother.info.label}${info.parent.parent.haveChildren?'...':''}</a>
				</div>
			</li>
		</c:forEach>
		</ul>
	</div>
</div>