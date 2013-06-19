<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content css">

<form id="form-css-template" action="${info.currentURL}" class="standard-form js-change-submit" method="post">
	
	<div>
		<input type="hidden" name="webaction" value="editHTML" />
		<input type="hidden" name="templateid" value="${currentTemplate.name}" />
	</div>
	
	<div class="action top">
			<input type="submit" name="back" value="${i18n.edit['global.back']}" />
			<input type="submit" value="${i18n.edit['global.ok']}" />		
	</div>
	
	<div class="static-tabs">
		<ul>
		<c:forEach var="html" items="${currentTemplate.htmls}">
			<li ${html == param.html?' class="active"':''} ><a href="${info.currentURL}?html=${html}&templateid=${currentTemplate.name}&webaction=editHTML">${html}</a></li>
		</c:forEach>
		</ul>
	</div>
	<div class="body">
			
		<c:if test="${not empty param.html}">	
			<input type="hidden" name="file" value="${param.html}" />
			<input type="hidden" name="html" value="${param.html}" />
			<textarea name="text" id="text-editor" rows="10" cols="10" data-ext="html">${fn:escapeXml(text)}</textarea>
			<pre id="ace-text-editor" data-ext="html">${fn:escapeXml(text)}</pre>
		</c:if>
		<div class="action">
			<input type="submit" name="back" value="${i18n.edit['global.back']}" />
			<input type="submit" value="${i18n.edit['global.ok']}" />		
		</div>		
	</div>
	
	</form>
	
	</div>

	