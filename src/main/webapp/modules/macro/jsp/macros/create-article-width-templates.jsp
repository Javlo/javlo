<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>

${js}

<form method="post" action="${info.currentURL}">
<input type="hidden" name="webaction" value="macro-create-article-width-templates.create" />
<input type="hidden" name="module" value="content" />
<input type="hidden" name="page" value="${info.pageName}" />
<div class="row">
<div class="col-sm-6">
<c:if test="${fn:length(pages)>1}">
<div class="form-group">
<label for="root">group</label>
<select class="form-control" id="root" name="root" onchange="updateLayouts();">
<c:forEach var="page" items="${pages}">
<option value="${page.key}">${page.value}</option>
</c:forEach>
</select>
</div>
</c:if>
<c:if test="${fn:length(pages)==1}">
<c:forEach var="page" items="${pages}">
<input type="hidden" name="root" value="${page.key}" />
</c:forEach>
</c:if>
<div class="form-group">
<label for="root">layout</label>
<select id="layout" name="layout" class="form-control">
</select>
</div>
</div><div class="col-sm-6">
<c:if test="${fn:length(info.languages)>1}">
<div class="form-group">
<label for="lang">Language</label>
<select id="lang" name="lang" class="form-control">
<c:forEach var="lg" items="${info.contentLanguages}">
<option value="${lg}" ${info.contentLanguage == lg?'selected="selected"':''}>${lg}</option>
</c:forEach>
</select>
</div>
</c:if>
<div class="form-group">
	<label for="date">date</label>
	<input type="text" class="datepicker form-control" id="date" name="date" value="${info.currentDate}"/>
</div>
</div></div>
<div class="checkbox">
	<label for="duplicate">
		<input type="checkbox" id="duplicate" name="duplicate" /> duplicate current page
	</label>
</div>

<c:if test="${globalContext.collaborativeMode}">
	<div class="checkbox">
		<label for="email">send email
			<input type="checkbox" id="email" name="email" />
		</label>
	</div>
	<div class="roles">	
	<fieldset>
		<legend>choose group (no selection = everybody)</legend>		
		<c:forEach var="role" items="${adminRoles}">
			<div class="inline">			
				<input type="checkbox" id="role-${role}" name="role-${role}" />
				<label class="suffix" for="role-${role}">${role}</label>
			</div>
		</c:forEach>
	</fieldset>
	</div>
</c:if>


<div class="action">
	<input class="btn btn-primary pull-right" type="submit" value="create" />
</div>
</form>

<script>
	function updateLayouts() {
		var root = document.getElementById("root");
		var layout = document.getElementById("layout");		
		layout.innerHTML = '';
		for (var i=0; i<layouts[root.selectedIndex].length; i++) {
			var option = document.createElement("option");
			option.appendChild(document.createTextNode(layouts[root.selectedIndex][i][1]));
			option.setAttribute("value", layouts[root.selectedIndex][i][0]);
			layout.appendChild(option);
		}
	}
	updateLayouts();
</script>
