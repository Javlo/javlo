<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><h2><span class="glyphicon glyphicon-menu-hamburger" aria-hidden="true"></span>Navigation</h2><c:set var="page" value="${info.page}" />
<c:if test="${fn:length(page.children) == 0 and not empty info.parent}">
	<c:set var="page" value="${info.parent}" />
</c:if>
<c:url var="pasteURL" value="${info.currentURL}" context="/">
	<c:param name="webaction" value="edit.insertPage" />
</c:url>
<div class="height-to-bottom">
<div class="pages web">
<ul class="navigation">
	<c:set var="asTitle" value="false" />
	<c:if test="${not empty info.parent}">
		<c:if test="${page.url eq info.currentURL}"><c:if test="${!page.trash}"><c:set var="asTitle" value="true" />		
			<li class="parent title">
				<c:if test="${not empty info.contextForCopy}"><a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}"><span class="glyphicon glyphicon-collapse-down" aria-hidden="true"></span></a></c:if>
				<span><a class="draggable" id="page-${info.parent.name}" href="${info.parent.url}">${info.parent.info.title}</a></span>			
			</li></c:if>
		</c:if>
		<c:if test="${!(page.url eq info.currentURL) && not empty info.parent.parent}"><c:if test="${!page.trash}"><c:set var="asTitle" value="true" />
			<li class="parent title">				
				<span><a class="draggable" id="page-${info.parent.parent.name}" href="${info.parent.parent.url}">${info.parent.parent.info.title}</a></span>			
			</li></c:if>
		</c:if>		
	</c:if>
	
	<c:forEach var="brother" items="${page.info.previousBrothers}"><c:if test="${!brother.trash}">
		<li>
		<span><a id="page-${brother.name}" class="draggable editor" title="${brother.info.title}" href="${brother.url}">${brother.info.title} </a></span>
		</li>
	</c:if></c:forEach>
	
	<li class="${page.url eq info.currentURL?'current ':''}${!asTitle?' title':''}${page.selected?' selected':''}">
		<c:if test="${not empty info.contextForCopy && (page.url eq info.currentURL)}"><a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}"><span class="glyphicon glyphicon-collapse-down" aria-hidden="true"></span></a></c:if>
		<span><a class="editor draggable" id="page-${page.name}" title="parent page" href="${page.url}">${page.info.label}</span></a>		
	</li>
	<c:if test="${asTitle}">
	<li><ul class="children sortable">
	</c:if>
	<c:forEach var="child" items="${page.children}">	
	<c:if test="${!child.trash}">
	<li id="page-${child.name}" class="${child.url eq info.currentURL?'current ':''}${child.info.realContent?'real-content':''} ${fn:length(child.children) > 0?'have-children ':''}">
	<c:if test="${not empty info.contextForCopy && (child.url eq info.currentURL)}"><a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}"><span class="glyphicon glyphicon-collapse-down" aria-hidden="true"></span></a></c:if>
	<span><a href="${child.url}" id="page-${child.name}" class="draggable" title="${child.info.title}">${child.info.title} </a></span>
	</li>
	</c:if>
	</c:forEach>	
	<c:if test="${asTitle}">
	</ul></li>
	</c:if> 
	<c:forEach var="brother" items="${page.info.nextBrothers}">		
		<c:if test="${!brother.trash}">
		<li>		
			<span><a class="draggable editor" id="page-${brother.name}" title="${brother.info.label}" href="${brother.url}">${brother.info.label}</span></a>
		</li>
		</c:if>
	</c:forEach>
	</ul>
	<c:if test="${!userInterface.contributor}">		
		<form class="preview-form" id="_pe_form-add-page" action="${info.currentURL}" method="post">
			<div class="row"><div class="col-xs-9">
			<div class="form-group">
			<input type="hidden" name="webaction" value="edit.addPage" />
			<input type="text" class="form-control input-sm" name="name" placeholder="${i18n.edit['navigation.add-page']}..." />
			</div>
			</div><div class="col-xs-3">
			<input class="btn btn-default btn-sm" type="submit" value="${i18n.edit['global.ok']}" />
			</div></div>
		</form>
		
	</c:if>	
</div>	
</div>