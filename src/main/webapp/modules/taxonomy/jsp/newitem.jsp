<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><%
/* a taxonomy id is an identifier, it is rendered into html attributes below */
String taxonomyIdParam = request.getParameter("id");
if (taxonomyIdParam == null || !taxonomyIdParam.matches("[A-Za-z0-9_.\\-]{0,64}")) {
	taxonomyIdParam = "";
}
request.setAttribute("safeTaxonomyId", taxonomyIdParam);
%><div class="item-wrapper" id="newname-${safeTaxonomyId}" data-id="${safeTaxonomyId}" data-aschild="true">
<div class="item">
<c:set var="focusKey" value="focus${safeTaxonomyId}" />
<span class="new-name"><input type="text" name="newname-${safeTaxonomyId}" class="hidden-input form-control${not empty param[focusKey]?' needfocus':' nofocus'}" placeholder="create new node" /></span>
</div>
</div>