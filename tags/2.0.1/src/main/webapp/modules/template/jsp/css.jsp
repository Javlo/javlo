<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content css">

<form id="form-css-template" action="${info.currentURL}" class="standard-form js-change-submit" method="post">
	
	<div>
		<input type="hidden" name="webaction" value="editCSS" />
		<input type="hidden" name="templateid" value="${currentTemplate.name}" />
	</div>
	
	<div class="static-tabs">
		<ul>
		<c:forEach var="css" items="${currentTemplate.CSS}">
			<li ${css == param.css?' class="active"':''} ><a href="${info.currentURL}?css=${css}&templateid=${currentTemplate.name}&webaction=editCSS">${css}</a></li>
		</c:forEach>
		</ul>
		<div class="body">	
			<c:if test="${not empty param.css}">	
				<input type="hidden" name="file" value="${param.css}" />
				<input type="hidden" name="css" value="${param.css}" />
				<textarea name="text" id="text-editor" rows="10" cols="10" data-ext="css">${text}</textarea>
			</c:if>
			<div class="action">
				<input type="submit" name="back" value="${i18n.edit['global.back']}" />
				<input type="submit" value="${i18n.edit['global.ok']}" />		
			</div>		
		</div>	
	</div>
	
	</form>

	