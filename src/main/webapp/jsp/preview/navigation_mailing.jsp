<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="rootAssociation" value="${info.page.rootOfChildrenAssociation}" />
<c:if test="${not empty rootAssociation}">
<fieldset class="closable composition">
	<legend>${i18n.edit['content.navigation']}</legend>
	   <ul class="navigation pages">
	   <li class="${rootAssociation.associationPage.url eq info.currentURL?'current ':''}${rootAssociation.associationPage.info.realContent?'real-content':''}"><span><a class="construction" href="${rootAssociation.associationPage.url}">composition</a></span></li>
	   <li><ul class="children sortable"><c:forEach var="page" items="${rootAssociation.pages}" varStatus="bcl">
	   			<li id="page-${page.name}" data-name="${page.name}" class="${page.url eq info.currentURL?'current ':''}${page.info.realContent?'real-content':''}">
	   				<span><a href="${page.url}" title="${page.pageTitle}">page-${bcl.index+1}</a></span>
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
	   				<input type="submit" value="+" title="create page" />
	   			</div>
	   		</form>	   		
	   </div>	   	
	   </li>
	   </ul>
	   <h4><span>articles</span></h4>
	   <ul class="navigation articles">
	   <li><ul class="children sortable"><c:forEach var="page" items="${rootAssociation.articles}" varStatus="bcl">
	   			<li id="page-${page.name}" data-name="${page.name}" class="${page.url eq info.currentURL?'current ':''}${page.info.realContent?'real-content':''}">
	   				<span><a href="${page.url}" title="${page.pageTitle}">${page.titleOrSubtitle}</a></span>
	   			</li>
	   		</c:forEach></ul>
	   </li>
	   </ul><ul class="navigation actions">
	   <li>
	   	 <div class="actions">
	   		<form id="add-page">
	   			<div>	   			
					<input type="hidden" value="edit.addPage" name="webaction" />				
					<input type="hidden" name="parent" value="${rootAssociation.articleRoot.name}" />
		   			<input type="submit" value="+" title="create article" />
	   			</div>
	   		</form>	   		
	   </div>
	   	
	   </li>
	   </ul>
</fieldset>
</c:if><c:if test="${empty rootAssociation}"><jsp:include page="navigation.jsp" /></c:if>