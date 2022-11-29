<%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.helper.URLHelper"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%
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
<div class="height-to-bottom minus-50">
<div class="pages web">

<c:url var="urlPageProperties" value="<%=URLHelper.createURL(editCtx)%>" context="/">
	<c:param name="module" value="content" />
	<c:param name="webaction" value="changeMode" />
	<c:param name="mode" value="3" />
	<c:param name="previewEdit" value="true" />
</c:url>

<ul class="navigation">
	<c:set var="asTitle" value="false" />
	
	<c:if test="${!page.root && page.parent != null && !page.parent.root}">
		<li class="parent title page-root">
			<a href="${info.rootURL}">ROOT <i class="bi bi-arrow-90deg-up"></i></a>
		</li>
	</c:if>
	
	<c:if test="${not empty info.parent}">
		<c:if test="${page.url eq info.currentURL}"><c:set var="asTitle" value="true" />		
			<li class="parent ${!page.trash?'title':'trash'}">
				<c:if test="${not empty info.contextForCopy && (child.url eq info.currentURL)}"><a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}"><span class="glyphicon glyphicon-collapse-down" aria-hidden="true"></span></a></c:if>
				<span><a class="draggable flow-${info.parent.flowIndex}" id="page-${info.parent.name}" data-pageid="${info.parent.id}" href="${info.parent.url}" title="${info.parent.path}">${info.parent.info.label}${info.parent.haveChildren?'...':''}</a></span>			
			</li>
		</c:if>
		<c:if test="${!(page.url eq info.currentURL) && not empty info.parent.parent}"><c:set var="asTitle" value="true" />
			<li class="parent ${!info.parent.parent.trash?'title':'trash'}">				
				<span><a class="draggable flow-${info.parent.parent.flowIndex}" id="page-${info.parent.parent.name}" href="${info.parent.parent.url}" title="${info.parent.parent.path}">${info.parent.parent.info.label}${info.parent.parent.haveChildren?'...':''}</a></span>			
			</li>
		</c:if>		
	</c:if>
	
	<c:forEach var="brother" items="${page.info.previousBrothers}"><li ${brother.trash?'class="trash"':''}>
		<span><a id="page-${brother.name}" class="draggable ${!brother.trash?'editor':'trash'} ${brother.active?'active':'unactive'} flow-${brother.flowIndex}" title="${brother.path}" href="${brother.url}">${brother.info.label}${info.parent.parent.haveChildren?'...':''}</a></span>
		</li>
	</c:forEach>
	
	<li class="${page.trash?'trash ':''}${page.url eq info.currentURL?'current ':''}${!asTitle?' title':''}${page.selected?' selected':''}">
		<c:if test="${not empty info.contextForCopy && (page.url eq info.currentURL)}"><a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}"><span class="glyphicon glyphicon-collapse-down" aria-hidden="true"></span></a></c:if>
		<span>
			<a class="editor draggable ${page.active?'active':'unactive'} flow-${page.flowIndex}" id="page-${page.name}" data-pageid="${child.id}" title="${page.path}" href="${page.url}">
				<span>${page.info.label}${page.haveChildren?'...':''}</span>
				<c:if test="${page.url eq info.currentURL}"><span><i class="bi bi-gear" onclick="editPreview.openModal('Page properties', '${urlPageProperties}'); return false;"></i></span></c:if>
			</a>
		</span>
		
		<c:if test="${page.url eq info.currentURL}">
		<c:if test="${userInterface.navigation}">
		<li class="add-page page-depth-${page.depth}"><form id="form-add-page" action="${info.currentURL}" method="post">
			<input type="hidden" name="webaction" value="edit.addPage" />
			<button class="flex-line btn-full" name="auto-name" type="submit">
				<span>${i18n.edit['navigation.add-page']}...</span>
				<i class="fa fa-plus-circle"></i>
			</button>
		</form></li>
		</c:if>
		</c:if>
		
	</li>
	<c:if test="${asTitle}">
	<li><ul class="children sortable">
	</c:if>
	<c:forEach var="child" items="${page.children}">		
	<li id="page-${child.name}" class="${child.trash?'trash ':''}${child.url eq info.currentURL?'current ':''}${child.info.realContent?'real-content':''} ${fn:length(child.children) > 0?'have-children ':''}">
	<c:if test="${not empty info.contextForCopy && (child.url eq info.currentURL)}"><a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}"><span class="glyphicon glyphicon-collapse-down" aria-hidden="true"></span></a></c:if>
	<span>
		<a href="${child.url}" id="page-${child.name}" data-pageid="${child.id}" class="draggable ${child.active?'active':'unactive'} flow-${child.flowIndex}" title="${child.path}">
			<span>${child.info.label}${child.haveChildren?'...':''}</span>
			<c:if test="${child.url eq info.currentURL}"><span><i class="bi bi-gear" onclick="editPreview.openModal('Page properties', '${urlPageProperties}'); return false;"></i></span></c:if>
		</a>
	</span>
	</li></c:forEach>	
	<c:if test="${asTitle}">
	</ul></li>
	</c:if> 
	<c:forEach var="brother" items="${page.info.nextBrothers}">		
		<li ${brother.trash?'class="trash"':''}>		
			<span><a class="draggable editor" id="page-${brother.name}" title="${brother.path}" href="${brother.url}">${brother.info.label}${info.parent.parent.haveChildren?'...':''}</a></span>
		</li>
		
	</c:forEach>
	</ul>
</div>	
</div>