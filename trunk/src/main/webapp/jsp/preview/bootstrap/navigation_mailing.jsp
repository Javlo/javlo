<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<h2><span class="glyphicon glyphicon-menu-hamburger" aria-hidden="true"></span>Navigation</h2>
<c:set var="rootAssociation" value="${info.page.rootOfChildrenAssociation}" />
<c:if test="${not empty rootAssociation}">
	<div class="height-to-bottom">
	<form id="children_list" action="${info.currentURL}" method="post">
	<div class="pages mailing">	
	   <ul class="navigation">	   
	   <li class="title ${rootAssociation.associationPage.url eq info.currentURL?'current ':''}${rootAssociation.associationPage.info.realContent?'real-content':''}">
	   		<a id="page-${rootAssociation.associationPage.name}" class="construction draggable" href="${rootAssociation.associationPage.url}">${i18n.edit["composition.main-title"]}</a>
	   </li>
	   <li><ul class="children sortable">	   
	   <c:forEach var="page" items="${rootAssociation.pages}" varStatus="bcl">
	   			<li class="${page.url eq info.currentURL?'current ':''}${page.info.realContent?'real-content':''}">
	   				<a id="page-${page.name}" class="draggable" href="${page.url}" title="${page.pageTitle}">page-${bcl.index+1}<span class="move ui-sortable-handle"></span></a>
	   			</li>	   			
	   		</c:forEach></ul>
	   </li>
	   </ul>
	   <ul class="navigation actions">
	   <li>
	   	 <div class="actions">
	   		<form id="add-page" action="${info.currentURL}" method="post">
	   			<div>
	   				<input type="hidden" value="edit.addPage" name="webaction" />				
					<input type="hidden" name="parent" value="${rootAssociation.associationPage.name}" />
	   				<button type="submit" class="btn btn-plus btn-default btn-xsm" value="+" title="Add a page" lang="en"><span class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span>Add a page</button>
	   			</div>
	   		</form>	   		
	   </div>	   	
	   </li>
	   </ul>
	   <ul class="navigation articles">
	   <li class="title"><h4>articles</h4></li>   
	   <li><ul class="children sortable ${fn:length(rootAssociation.articles)==0?'empty':'not-empty'}"><c:forEach var="page" items="${rootAssociation.articles}" varStatus="bcl">
	   			<li class="${page.url eq info.currentURL?'current ':''}${page.info.realContent?'real-content':''}">
	   				<a id="page-${page.name}" class="draggable" href="${page.url}" title="${page.pageTitle}">${page.titleOrSubtitle}</a>
	   			</li>	   			
	   		</c:forEach>
	   		</ul>	   	
	   </li>
	   </ul><ul class="navigation actions">
	   <li>
	   	 <div class="actions">
	   		<form id="add-page" action="${info.currentURL}" method="post">
	   			<div>	   			
					<input type="hidden" value="edit.addPage" name="webaction" />				
					<input type="hidden" name="parent" value="${rootAssociation.articleRoot.name}" />
		   			<button type="submit" class="btn btn-plus btn-default btn-xsm" value="+" title="Add a article" lang="en"><span class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span>Add a article</button>
	   			</div>
	   		</form>	   		
	   </div>	   	
	   </li>
	   </ul>
    </div>
    </form>
	</div>
</c:if><c:if test="${empty rootAssociation}"><jsp:include page="navigation.jsp" /></c:if>