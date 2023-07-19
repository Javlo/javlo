<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:set var="templateid" value="${param.templateid}" /><c:if test="${not empty currentTemplate && empty templateid}"><c:set var="templateid" value="${currentTemplate.name}" /></c:if>
<div class="content css">

	<div class="row">
	<div class="col-md-3">

	<div class="file-filter">
		<form id="form-css-template" action="${info.currentURL}" method="post">
		<div class="form-group _jv_flex-line">
		<input type="hidden" name="templateid" value="${templateid}" />
		<input type="hidden" name="css" value="${param.css}" />
		<input type="hidden" name="webaction" value="template.editCSS" />
		<input type="text" name="search" value="${param.search}" placeholder="search..." id="input-filter" class="form-control" />
		<input type="submit" value="ok" class="btn btn-default btn-xs ms-1" />
		</div>
		</form>

	</div>
	<br />
	<div class="file-list">

		<input id="filter-files" class="form-control mb-3" onkeyup="filterFilesList();" placeholder="filter" />

		<script>
			function openCloseFileList(e, i) {
				let wrapper = i.parentElement.parentElement;
				if (wrapper.classList.contains("active")) {
					wrapper.classList.remove("active");
				} else {
					wrapper.classList.add("active");
				}
				event.preventDefault();
			}

			function filterFilesList() {
				var query = document.getElementById("filter-files").value.toLowerCase();
				document.querySelectorAll(".file-block").forEach(i => {
					i.classList.remove("low-active");
				});
				document.querySelectorAll(".file-list .files a").forEach(i => {
					let text = i.innerHTML;
					if (query.length > 0) {
						if ((query.length > 0 && text.indexOf(query) >= 0)) {
							i.classList.remove("hidden");
							i.parentElement.parentElement.classList.add("low-active");
						} else {
							i.classList.add("hidden");
						}
					} else {
						i.classList.remove("hidden");
					}
				});
			}

			function openSection() {
				document.querySelectorAll(".file-list .files a.active").forEach(i => {
					let wrapper = i.parentElement.parentElement;
					wrapper.classList.add("active");
				});
			}

		</script>

		<c:forEach var="folder" items="${cssFolder}">
			<div class="file-block">
			<div class="title"><a onclick="openCloseFileList(event, this)" role="button" data-toggle="collapse" href="#${folder.key}" aria-expanded="false" aria-controls="${folder.key}">${folder.key}</a></div>
			<div class="files" id="${folder.key}">
				<c:forEach var="css" items="${folder.value}" varStatus="status">
					<c:set var="file" value="${folder.key}/${css}" />
					<c:url var="cssUrl" value="${info.currentURL}" context="/">
						<c:param name="css" value="${file}" />
						<c:param name="templateid" value="${templateid}" />
						<c:param name="webaction" value="editCSS" />
						<c:param name="search" value="${param.search}" />
					</c:url>
					<a id="${folder.key}-${status.index}" ${file == param.css?' class="file-link active"':'class="file-link"'} href="${cssUrl}">${css}</a>
				</c:forEach>
			</div>
			</div>
		</c:forEach>

		<c:if test="${not empty param.css}"><script>openSection();</script></c:if>
	</div>

	</div>

	<div class="col-md-7">

	<form id="form-css-template" action="${info.currentURL}" class="standard-form js-change-submit ajax" method="post">
		<div>
			<input type="hidden" name="webaction" value="template.editCSS" /> <input type="hidden" name="templateid" value="${templateid}" />
		</div>
		<div class="widgetbox2">
		<h3>${param.css}</h3>
		<div class="content nopadding">
		<c:if test="${not empty param.css}">
			<input type="hidden" name="file" value="${param.css}" />
			<input type="hidden" name="css" value="${param.css}" />
			<textarea name="text" class="text-editor" id="text-editor" rows="10" cols="10" data-ext="css" data-mode="text/x-scss">${text}</textarea>
		</c:if>
		</div></div>
		<div class="action">
			<input type="submit" name="back" value="${i18n.edit['global.back']}" />
			<input type="submit" value="${i18n.edit['global.save']}" />
			<input type="submit" name="indent" value="${i18n.edit['global.save']} & indent" />
		</div>
	</form>

	</div>

		<script>
			function filterCssVariable() {
				var textIn = document.getElementById("filter-input-in").value.toLowerCase();
				var textOut = document.getElementById("filter-input-out").value.toLowerCase();
				var css = document.getElementById("filter-css").checked;
				var scss = document.getElementById("filter-sass").checked;
				document.querySelectorAll(".css-variable-list .css-variable a").forEach(i => {
						let text = (i.innerHTML + ' ' + i.getAttribute("title")).toLowerCase();
						if (textIn.length > 0 || textOut.length > 0) {
							if ((textIn.length > 0 && text.indexOf(textIn) >= 0) || (textOut.length > 0 && text.indexOf(textOut) < 0)) {
								i.classList.remove("hidden");
							} else {
								i.classList.add("hidden");
							}
						} else {
							i.classList.remove("hidden");
						}
						if (css && !i.innerHTML.startsWith("--")) {
							i.classList.add("hidden");
						}
						if (scss && !i.innerHTML.startsWith("$")) {
							i.classList.add("hidden");
						}
				});
			}
		</script>

		<div class="col-md-2 css-variable-list">
			<h4>CSS/SCSS Variables</h4>
			<input id="filter-input-in" class="form-control mb-3" onkeyup="filterCssVariable();" placeholder="filter +" />
			<input id="filter-input-out" class="form-control mb-3" onkeyup="filterCssVariable();" placeholder="filter -" />

			<div class="btn-group mb-3">
				<div class="_jv_btn-check">
					<input id="filter-all" type="radio" name="filter" value="all" checked onchange="filterCssVariable();">
					<label for="filter-all">All</label>
				</div>
				<div class="_jv_btn-check">
					<input id="filter-css" type="radio" name="filter" value="css" onchange="filterCssVariable();">
					<label for="filter-css">CSS</label>
				</div>
				<div class="_jv_btn-check">
					<input id="filter-sass" type="radio" name="filter" value="sass" onchange="filterCssVariable();">
					<label for="filter-sass">SASS</label>
				</div>
			</div>

			<c:forEach var="cssVar" items="${cssVariables}">
				<div class="css-variable">
					<a href="javascript:navigator.clipboard.writeText('${cssVar.key}');" title="<c:out value="${cssVar.value}" escapeXml="true"/>">${cssVar.key}</a>
				</div>
			</c:forEach>
		</div>
	</div>

</div>
