<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:set var="page" value="${info.page}" />
<c:if test="${fn:length(page.children) == 0 and not empty info.parent}">
	<c:set var="page" value="${info.parent}" />
</c:if>
<div class="height-to-bottom">
<div class="pages web">
<ul class="navigation">
	<c:set var="asTitle" value="false" />
	<c:if test="${not empty info.parent}">
		<c:if test="${page.url eq info.currentURL}"><c:set var="asTitle" value="true" />		
			<li class="parent title">
				<span><a href="${info.parent.url}">${info.parent.info.title}</a></span>			
			</li>
		</c:if>
		<c:if test="${!(page.url eq info.currentURL) && not empty info.parent.parent}"><c:set var="asTitle" value="true" />
			<li class="parent title">
				<span><a href="${info.parent.parent.url}">${info.parent.parent.info.title}</a></span>			
			</li>
		</c:if>		
	</c:if>
	
	<c:forEach var="brother" items="${page.info.previousBrothers}">
		<li>		
			<span><a class="editor" title="brother page" href="${brother.url}">${brother.info.title}</a></span>
		</li>
	</c:forEach>
	
	<li class="${page.url eq info.currentURL?'current ':''}${!asTitle?' title':''}${page.selected?' selected':''}">
		<span><a class="editor" title="parent page" href="${page.url}">${page.info.label}</span></a>
	</li>
	<c:if test="${asTitle}">
	<li><ul class="children sortable">
	</c:if>
	<c:forEach var="child" items="${page.children}">	
	<li id="page-${child.name}" data-name="${child.name}" class="${child.url eq info.currentURL?'current ':''}${child.info.realContent?'real-content':''} ${fn:length(child.children) > 0?'have-children ':''}">
	<span><a href="${child.url}">${child.info.title}</a></span>
	</li>
	</c:forEach>	
	<c:if test="${asTitle}">
	</ul></li>
	</c:if> 
	<c:forEach var="brother" items="${page.info.nextBrothers}">
		<li>		
			<span><a class="editor" title="brother page" href="${brother.url}">${brother.info.label}</span></a>
		</li>
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