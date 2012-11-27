<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="page" value="${info.page}" />
<c:if test="${fn:length(page.children) == 0 and not empty info.parent}">
	<c:set var="page" value="${info.parent}" />
</c:if>

<ul class="navigation">
	<c:if test="${not empty info.parent && page.url eq info.currentURL}">
		<li class="parent"><a href="${info.parent.url}">${info.parent.info.title}</a></li>
	</c:if>
	<li class="${page.url eq info.currentURL?'current ':''}">	
		<a class="editor" title="parent page" href="${page.url}">${page.info.title}</a>
	</li>
	<li><ul class="children sortable">
	<c:forEach var="child" items="${page.children}">	
	<li id="page-${child.name}" class="${child.url eq info.currentURL?'current ':''}${child.info.realContent?'real-content':''} ${fn:length(child.children) > 0?'have-children ':''}${child.info.realContent?'real-content':''}">
	<a href="${child.url}">${child.info.title}</a>
	</li>
	</c:forEach>	
	</ul>	
	</li>
	<li class="add-page">
		<form id="form-add-page" action="${info.currentURL}" method="post">
			<div>
			<input type="hidden" name="webaction" value="edit.addPage" />
			<input type="text" class="label-inside label" name="name" value="${i18n.edit['navigation.add-page']}..." />
			<input type="submit" value="${i18n.edit['global.ok']}" />
			</div>
		</form>
	</li>	
	<c:if test="${not empty info.copiedPath && currentModule.name eq 'content'}">
	<li class="insert-page">
		<form id="form-insert-page" action="${info.currentURL}" method="post">
			<div>
			<input type="hidden" name="webaction" value="edit.insertPage" />			
			<input type="submit" name="name" value="${i18n.edit['navigation.insert-page']}" />			
			</div>
		</form>
	</li>
	</c:if>
</ul>