<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><h2><span class="glyphicon glyphicon-menu-hamburger" aria-hidden="true"></span>Navigation</h2><c:set var="page" value="${info.page}" />
<c:if test="${fn:length(page.children) == 0 and not empty info.parent}">
	<c:set var="page" value="${info.parent}" />
</c:if>
<c:url var="pasteURL" value="${info.currentURL}" context="/">
	<c:param name="webaction" value="edit.insertPage" />
</c:url>
<div class="height-to-bottom minus-50">
<div class="pages web">
<ul class="navigation">
	<c:set var="asTitle" value="false" />
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
		<span><a id="page-${brother.name}" class="draggable ${!brother.trash?'editor':'trash'} ${brother.active?'active':'unactive'} flow-${brother.flowIndex}" title="${brother.path}" href="${brother.url}">${brother.info.label}</a></span>
		</li>
	</c:forEach>
	
	<li class="${page.trash?'trash ':''}${page.url eq info.currentURL?'current ':''}${!asTitle?' title':''}${page.selected?' selected':''}">
		<c:if test="${not empty info.contextForCopy && (page.url eq info.currentURL)}"><a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}"><span class="glyphicon glyphicon-collapse-down" aria-hidden="true"></span></a></c:if>
		<span><a class="editor draggable ${page.active?'active':'unactive'} flow-${page.flowIndex}" id="page-${page.name}" data-pageid="${child.id}" title="${page.path}" href="${page.url}">${page.info.label}${page.haveChildren?'...':''}</a></span>		
	</li>
	<c:if test="${asTitle}">
	<li><ul class="children sortable">
	</c:if>
	<c:forEach var="child" items="${page.children}">		
	<li id="page-${child.name}" class="${child.trash?'trash ':''}${child.url eq info.currentURL?'current ':''}${child.info.realContent?'real-content':''} ${fn:length(child.children) > 0?'have-children ':''}">
	<c:if test="${not empty info.contextForCopy && (child.url eq info.currentURL)}"><a title="${i18n.edit['navigation.insert-page']}" class="paste-page" href="${pasteURL}"><span class="glyphicon glyphicon-collapse-down" aria-hidden="true"></span></a></c:if>
	<span><a href="${child.url}" id="page-${child.name}" data-pageid="${child.id}" class="draggable ${child.active?'active':'unactive'} flow-${child.flowIndex}" title="${child.path}">${child.info.label}${child.haveChildren?'...':''} </a></span>
	</li></c:forEach>	
	<c:if test="${asTitle}">
	</ul></li>
	</c:if> 
	<c:forEach var="brother" items="${page.info.nextBrothers}">		
		<li ${brother.trash?'class="trash"':''}>		
			<span><a class="draggable editor" id="page-${brother.name}" title="${brother.path}" href="${brother.url}">${brother.info.label}</span></a>
		</li>
		
	</c:forEach>
	</ul>
	<c:if test="${userInterface.navigation}">		
		<form class="preview-form" id="_pe_form-add-page" action="${info.currentURL}" method="post">
			<div class="row"><div class="col-xs-7">
			<div class="form-group">
			<input type="hidden" name="webaction" value="edit.addPage" />
			<input type="text" class="form-control input-sm" name="name" placeholder="${i18n.edit['navigation.add-page']}..." 
				onkeyup="if (pjq(this).val().length == 0) {$('#plus-btn').prop('disabled', false); $('#ok-btn').prop('disabled', true);} else {pjq('#plus-btn').prop('disabled', true); pjq('#ok-btn').prop('disabled', false);}" />
			</div>
			</div><div class="col-xs-5">
			<input id="ok-btn" class="btn btn-default btn-sm" type="submit" value="${i18n.edit['global.ok']}" disabled="disabled" />
			<button id="plus-btn" class="btn btn-default btn-sm pull-right" name="auto-name" type="submit"><i class="fa fa-plus-circle"></i></button>
			</div></div>
		</form>
		
	</c:if>	
</div>	
</div>