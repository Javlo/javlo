<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="list-multimedia">

<form action="${info.currentURL}" method="get">
<fieldset>
<legend>filter</legend>
<select name="folder">
<c:forEach var="folder" items="${folders}">
	<option value="${folder.key}"${param.folder == folder.key?' selected="selected"':''}>${not empty folder.title?folder.title:folder.key}</option>
</c:forEach>
</select>
<input type="submit" />
</fieldset>
</form>

<div class="current-resource">
	<img alt="${currentResource.title}" src="${currentResource.URL}" />
</div>

	<c:forEach var="resource" items="${resources}" varStatus="status">
	
	<c:set var="display" value="false" />
	<c:if test="${status.index >= (pagination.page-1)*pagination.pageSize || pagination.pageSize == 0}">
		<c:if test="${status.index <= (pagination.page)*pagination.pageSize || pagination.pageSize == 0}">
			<c:set var="display" value="true" />
		</c:if>
	</c:if>
	
	<c:if test="${display}">
	<div class="item">
			<div class="preview ${resource.cssClass}">
			    <c:url value="${info.currentURL}" var="resourceURL">
			    	<c:param name="resource" value="${resource.id}" />
			    	<c:if test="${not empty param.folder}">
			    		<c:param name="folder" value="${param.folder}" />
			    	</c:if>
			    </c:url>
				<a href="${resourceURL}" title="${resource.title} - ${resource.location} - ${resource.shortDate}" onclick="sendAction('${resource.accessURL}');">
					<img alt="${resource.title}" src="${resource.previewURL}" />
				</a>
			</div>			
		</div>
		</c:if>		
	</c:forEach>
	</div>
	<jsp:include page="../../pagination.jsp"></jsp:include>
	
	