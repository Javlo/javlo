<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="special">
<form class="form_default" id="form-add-template" action="${info.currentURL}" method="post">
<div>
<input type="hidden" name="webaction" value="createTemplate" />
<input class="label-inside label" type="text" name="template" placeholder="create template"/>
<input type="submit" class="action-button add-user" value="${i18n.edit['global.ok']}" />
</div>
</form>
<c:if test="${not empty template}">
<form method="post" action="${info.currentEditURL}" id="form-create-row" class="form_default">
<div>
<input type="hidden" value="createRow" name="webaction">
<input type="submit" value="add row" class="action-button">
</div>
</form>
<form method="post" action="${info.currentEditURL}" id="form-create-area" class="form_default">
<div>
<input type="hidden" value="createArea" name="webaction">
<input type="submit" value="add area" class="action-button">
</div>
</form>
<a target="_blanck" class="action-button preview" href="${info.currentViewURL}?force-template=${templateEditorContext.currentTemplate.name}">preview</a>
</c:if>
</div>
<c:if test="${not empty template}">
<form method="post" action="${info.currentEditURL}" id="form-change-template" class="js-submit">
<div class="line">
	<input type="hidden" name="webaction" value="changeTemplate" />
	<label for="template">choose template</label>
	<select id="template" name="template">
	<c:forEach var="template" items="${templates}">
		<option${templateEditorContext.currentTemplate.name == template?' selected="selected"':''} value="${template}">${template}</option>
	</c:forEach>
	</select>
	<input type="hidden" name="webaction" value="showContent" />
	<label for="show-content">show content</label>
	<input type="checkbox" id="show-content" name="show-content" ${templateEditorContext.showContent?'checked="checked"':''} onchange="this.form.submit();" />
</div>	
</form>
</c:if>
<div class="clear">&nbsp;</div>


