<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="rootAssociation" value="${info.page.rootOfChildrenAssociation}" />
<c:if test="${not empty rootAssociation}">
	<div class="tabs-head"><h4>Navigation</h4></div>
	<form id="children_list" action="${info.currentURL}" method="post">
	<div class="pages">	
	   <ul class="navigation pages">	   
	   <li class="title ${rootAssociation.associationPage.url eq info.currentURL?'current ':''}${rootAssociation.associationPage.info.realContent?'real-content':''}"><a class="construction" href="${rootAssociation.associationPage.url}">${i18n.edit["composition.main-title"]}</a></li>
	   <li><ul class="children sortable">	   
	   <c:forEach var="page" items="${rootAssociation.pages}" varStatus="bcl">
	   			<li id="page-${page.name}" data-name="${page.name}" class="${page.url eq info.currentURL?'current ':''}${page.info.realContent?'real-content':''}">
	   				<a href="${page.url}" title="${page.pageTitle}">page-${bcl.index+1}<span class="move ui-sortable-handle"></span></a>
	   			</li>	   			
	   		</c:forEach></ul>
	   </li>
	   </ul>
	   <ul class="navigation actions">
	   <li>
	   	 <div class="actions">
	   		<form id="add-page">
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
	   			<li id="page-${page.name}" data-name="${page.name}" class="${page.url eq info.currentURL?'current ':''}${page.info.realContent?'real-content':''}">
	   				<a href="${page.url}" title="${page.pageTitle}">${page.titleOrSubtitle}</a>
	   			</li>	   			
	   		</c:forEach>
	   		</ul>	   	
	   </li>
	   </ul><ul class="navigation actions">
	   <li>
	   	 <div class="actions">
	   		<form id="add-page" action="${info.currentURL}">
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

</c:if><c:if test="${empty rootAssociation}"><jsp:include page="navigation.jsp" /></c:if>