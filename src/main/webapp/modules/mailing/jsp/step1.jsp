<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="${contentContext.editPreview?'preview ':'edit '}content wizard">
	<form class="standard-form" action="${info.currentURL}" method="get">
		<div>
			<input type="hidden" name="webaction" value="wizard" />
			<input type="hidden" name="box" value="${box.name}" />
			<c:if test="${contentContext.editPreview}"><input type="hidden" name="previewEdit" value="true" /></c:if>
		</div>

		<div id="gallery" class="gallery main-template main">
			<div id="gridview" class="thumbview">
				<ul>
					<c:forEach var="template" items="${templates}">
						<li class="${template.name == currentTemplate ? 'selected' : ''}">
							<div class="thumb">
								<a href="${info.currentURL}?webaction=mailing.selectMailingTemplate&name=${template.name}">
									<img src="${template.previewUrl}" alt="${template.name}" />
								</a>
							</div><!--thumb-->
						</li>
					</c:forEach>
				</ul>
			</div>
		</div>
		
		<div class="action">
			<div class="btn-group pull-right">
				<button type="submit" class="btn btn-default btn-color" name="wizardStep" value="4">export</button>
				<button type="submit" class="btn btn-primary btn-color" name="next">next</button>
			</div>
		</div>
		
	</form>
</div>
