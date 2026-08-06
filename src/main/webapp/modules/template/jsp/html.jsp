<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><%
/* a template name is an identifier, it is rendered into attributes and links below */
String templateIdParam = request.getParameter("templateid");
if (templateIdParam == null || !templateIdParam.matches("[A-Za-z0-9_.\\-]{0,64}")) {
	templateIdParam = "";
}
request.setAttribute("safeTemplateId", templateIdParam);
%><c:set var="templateid" value="${safeTemplateId}" /><c:if test="${not empty currentTemplate}"><c:set var="templateid" value="${currentTemplate.name}" /></c:if><div class="content css">

	<div class="row">
		<div class="col-md-3">
			<div class="file-filter">
				<form id="form-css-template" action="${info.currentURL}" method="post">
					<div class="form-group _jv_flex-line">
						<input type="hidden" name="templateid" value="${templateid}" />
						<input type="hidden" name="html" value="<c:out value="${param.html}" />" />
						<input type="hidden" name="webaction" value="template.editHTML" />
						<input type="text" name="search" value="<c:out value="${param.search}" />" placeholder="search..." id="input-filter" class="form-control" />
						<input type="submit" value="ok" class="btn btn-default btn-xs ms-1" />
					</div>
				</form>
			</div>
			<br />
			<div class="accordion">
				<c:forEach var="folder" items="${htmlFolder}">
					<h4>
						<a role="button" data-toggle="collapse" href="#${folder.key}" aria-expanded="false" aria-controls="${folder.key}">${empty folder.key?'/':folder.key}</a>
					</h4>
					<div>
						<div class="files">
							<c:forEach var="html" items="${folder.value}">
								<c:set var="file" value="${folder.key}/${html}" />
								<c:url var="htmlUrl" value="${info.currentURL}" context="/">
									<c:param name="html" value="${file}" />
									<c:param name="templateid" value="${templateid}" />
									<c:param name="webaction" value="editHTml" />
									<c:param name="search" value="${param.search}" />
								</c:url>
								<a ${file == param.css?' class="btn btn-sm btn-primary"':'class="btn btn-sm btn-default"'} id="${folder.key}" href="${htmlUrl}">${html}</a>
							</c:forEach>
						</div>
					</div>
				</c:forEach>
			</div>

		</div>
		<div class="col-md-9">

			<form id="form-css-template" action="${info.currentURL}" class="standard-form js-change-submit ajax" method="post">

				<div>
					<input type="hidden" name="webaction" value="template.editHTML" />
					<input type="hidden" name="templateid" value="${templateid}" />
				</div>

				<h3><c:out value="${param.html}" /></h3>
					<div class="body">

						<c:if test="${not empty param.html}">
							<input type="hidden" name="file" value="<c:out value="${param.html}" />" />
							<input type="hidden" name="html" value="<c:out value="${param.html}" />" />
							<textarea name="text" id="text-editor" rows="10" cols="10" data-ext="html" data-mode="text/html" class="text-editor">${fn:escapeXml(text)}</textarea>
						</c:if>
						<div class="action">
							<c:if test="${not empty editCssUrl}">
								<a class="btn btn-secondary" href="${editCssUrl}">Edit SCSS</a>
							</c:if>
							<c:if test="${empty editCssUrl && not empty createCssUrl}">
								<a class="btn btn-secondary" href="${createCssUrl}">Create SCSS</a>
							</c:if>
							<input type="submit" name="back" value="${i18n.edit['global.back']}" />
							<input type="submit" value="${i18n.edit['global.save']}" />
						</div>
					</div>
			</form>

		</div>
	</div>
</div>