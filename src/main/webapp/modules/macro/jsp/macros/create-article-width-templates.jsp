<%@ taglib uri="jakarta.tags.core" prefix="c"%><%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
${js}
<form method="post" action="${info.currentURL}" class="create-article-macro">
	<input type="hidden" name="webaction" value="macro-create-article-width-templates.create" />
	<input type="hidden" name="module" value="content" />
	<input type="hidden" name="month" value="${month}" />
	<input type="hidden" name="page" value="${info.pageName}" />

	<c:if test="${!foundModel}">
		<div class="alert alert-danger">no model found.</div>
	</c:if>

	<div class="params">

		<c:if test="${fn:length(pages)>0}">
			<div class="${fn:length(pages)==1?'hidden':'form-group'}">
				<label for="root">group</label>
				<select class="form-control" id="root" name="root" onchange="updateLayoutsImage();" required>
					<c:if test="${fn:length(pages)>1}"><option></option></c:if>
					<c:forEach var="page" items="${pages}">
						<option value="${page.key}">${page.value}</option>
					</c:forEach>
				</select>
			</div>
		</c:if>

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
			<input type="text" class="datepicker form-control" id="date" name="date" value="" required />
		</div>
	</div>

	<div id="select-image" class="select-image"></div>

	<div class="checkbox">
		<label for="duplicate"> <input type="checkbox" id="duplicate" name="duplicate" /> duplicate current page
		</label>
	</div>

	<c:if test="${globalContext.collaborativeMode}">
		<div class="checkbox">
			<label for="email">send email <input type="checkbox" id="email" name="email" />
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

	<input type="hidden" id="layout" name="layout" />

	<div class="action">
		<button class="btn btn-primary pull-right" type="submit" disabled>create</button>
	</div>
</form>

<script>
	function updateLayoutsImage() {
		var layout = document.getElementById("select-image");
		layout.innerHTML = '';
		console.log("layouts = ", layouts);
		for (var i = 0; i < layouts.length; i++) {
			var option = document.createElement("a");
			option.setAttribute("class", "image");
			option.setAttribute("href", "#");
			option
					.setAttribute(
							"onclick",
							"if (document.querySelectorAll('.image.select').length>0) {document.querySelectorAll('.image.select')[0].classList.remove('select');} "
									+ "else { document.querySelectorAll('.btn-primary ')[0].disabled = false; } this.classList.add('select'); document.getElementById('layout').value = '"
									+ layouts[i][0] + "';");
			if (layouts[i][2].length > 0) {
				option.innerHTML = '<img src="'+layouts[i][2]+'" /><div class=\"name\">'
						+ layouts[i][1] + '</div>';
			} else {
				option.innerHTML = '<div class="no-image">no screenshot</div><div class=\"name\">'
						+ layouts[i][1] + '</div>';
			}
			layout.appendChild(option);
		}
	}
	function updateLayoutsSelect() {
		var root = document.getElementById("root");
		var layout = document.getElementById("layout");
		layout.innerHTML = '';
		for (var i = 0; i < layouts[root.selectedIndex].length; i++) {
			var option = document.createElement("option");
			option.appendChild(document.createTextNode(layouts[root.selectedIndex][i][1]));
			option.setAttribute("value", layouts[root.selectedIndex][i][0]);
			layout.appendChild(option);
		}
	}
	updateLayoutsImage();
</script>
