<%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.helper.URLHelper"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${fn:length(info.interactiveMacro)>0 || fn:length(info.macro)>0}"><%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
%><div class="add">
	<a role="button" class="action" data-toggle="collapse" href="#addMacros" aria-expanded="false" aria-controls="addMacros"><span class="glyphicon glyphicon-plus-sign"></span></a>

<div class="macros collapse" id="addMacros">
<c:forEach var="macro" items="${info.interactiveMacro}">
	<c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">
		<c:param name="module" value="macro" />
		<c:param name="webaction" value="macro.executeInteractiveMacro" />
		<c:param name="mode" value="3" />
		<c:param name="macro" value="${macro.name}" />
		<c:param name="macro-${macro.name}" value="${macro.name}" />
		<c:param name="previewEdit" value="true" />
	</c:url>
	 <c:set var="descriptionKey" value="macro.description.${macro.name}" />
	<button type="button" class="btn btn-default" aria-label="Left Align" onclick="editPreview.openModal('Template', '${url}');" title="${i18n.edit[descriptionKey] != descriptionKey?i18n.edit[descriptionKey]:''}">
	  <c:set var="key" value="macro.${macro.name}" />	 
	  ${i18n.edit[key] != key?i18n.edit[key]:macro.name}
	</button>	
</c:forEach></div></div>
<<script type="text/javascript">
<!--
pjq('.collapse').collapse('hide');
//-->
</script>
</c:if>