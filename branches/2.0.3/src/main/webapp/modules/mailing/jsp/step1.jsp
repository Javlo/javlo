<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="${contentContext.editPreview?'preview ':'edit '}content wizard">
	<form class="${contentContext.editPreview?'':'ajax '} standard-form" action="${info.currentURL}" method="post">
		<div>
			<input type="hidden" name="webaction" value="wizard" />
			<input type="hidden" name="box" value="${box.name}" />
			<c:if test="${contentContext.editPreview}"><input type="hidden" name="previewEdit" value="true" /></c:if>
		</div>

		<div id="gallery" class="gallery main-template">
			<div id="gridview" class="thumbview">
				<ul>
					<c:forEach var="template" items="${templates}">
						<li class="${template.name == currentTemplate ? 'selected' : ''}">
							<div class="thumb">
								<a class="ajax" href="${info.currentURL}?webaction=mailing.selectMailingTemplate&name=${template.name}">
									<img src="${template.previewUrl}" alt="${template.name}" />
								</a>
							</div><!--thumb-->
						</li>
					</c:forEach>
				</ul>
			</div>
		</div>
		
		<div class="action">
			<input type="submit" name="next" value="Next" />
		</div>
		
	</form>
</div>
