<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="page" value="${info.page}" />
<c:if test="${fn:length(page.children) == 0 and not empty info.parent}">
	<c:set var="page" value="${info.parent}" />
</c:if>

<ul class="navigation">
	<c:if test="${not empty info.parent && page.url eq info.currentURL}">
		<li class="parent">
		
			<c:if test="${not empty info.parent.parent}">
				<div id="breadcrumb"><div class="breadcrumb-wrapper"><div class="breadcrumb">
					<c:forEach var="parent" items="${info.parent.parents}">
					<ul><li><a href="${parent.url}">${parent.info.title}</a>
					</c:forEach>
					<c:forEach var="parent" items="${info.parent.parents}">
					</li></ul>
					</c:forEach>
					<span><a href="${info.parent.url}">${info.parent.info.title}</a></span>
				</div></div></div>		
			</c:if>
		
			<span><a href="${info.parent.url}">${info.parent.info.title}</a></span>			
		</li>
	</c:if>
	
	<c:forEach var="brother" items="${page.info.previousBrothers}">
		<li>		
			<span><a class="editor" title="brother page" href="${brother.url}">${brother.info.title}</span></a>
		</li>
	</c:forEach>
	
	<li class="${page.url eq info.currentURL?'current ':''}">	
		<c:if test="${page.url != info.currentURL}">
		<div id="breadcrumb"><div class="breadcrumb-wrapper"><div class="breadcrumb">
			<c:forEach var="parent" items="${info.parent.parents}">
			<ul><li><a href="${parent.url}">${parent.info.title}</a>
			</c:forEach>
			<c:forEach var="parent" items="${info.parent.parents}">
			</li></ul>
			</c:forEach>
			<span><a class="editor" title="parent page" href="${page.url}">${page.info.title}</span></a>
		</div></div></div>	
		</c:if>
	
		<span><a class="editor" title="parent page" href="${page.url}">${page.info.title}</span></a>
	</li>
	<li><ul class="children sortable">
	<c:forEach var="child" items="${page.children}">	
	<li id="page-${child.name}" data-name="${child.name}" class="${child.url eq info.currentURL?'current ':''}${child.info.realContent?'real-content':''} ${fn:length(child.children) > 0?'have-children ':''}">
	<span><a href="${child.url}">${child.info.title}</a></span>
	</li>
	</c:forEach>	
	</ul>	
	</li>
	<c:forEach var="brother" items="${page.info.nextBrothers}">
		<li>		
			<span><a class="editor" title="brother page" href="${brother.url}">${brother.info.title}</span></a>
		</li>
	</c:forEach>
<c:if test="${!userInterface.contributor}">
	<li class="add-page">
		<form id="form-add-page" action="${info.currentURL}" method="post">
			<div>
			<input type="hidden" name="webaction" value="edit.addPage" />
			<input type="text" class="label-inside label" name="name" value="${i18n.edit['navigation.add-page']}..." />
			<input type="submit" value="${i18n.edit['global.ok']}" />
			</div>
		</form>
	</li>	
</c:if>	
</ul>