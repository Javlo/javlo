<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
	<form class="ajax" action="${info.currentURL}" method="post">
		<div>
			<input type="hidden" name="webaction" value="wizard" />
			<input type="hidden" name="box" value="${box.name}" />
		</div>

		<div id="gallery" class="gallery main-template">
			<div id="gridview" class="thumbview">
				<ul>
					<c:forEach var="template" items="${templates}">
						<li class="${template.name == currentTemplate ? 'selected' : ''}">
							<div class="thumb">
								<a class="ajax" href="${info.currentURL}?webaction=selectMailingTemplate&name=${template.name}">
									<img src="${template.previewUrl}" alt="${template.name}" />
								</a>
							</div><!--thumb-->
						</li>
					</c:forEach>
				</ul>
			</div>
		</div>
		<div>
			<input type="submit" name="next" value="Next" />
		</div>
	</form>
</div>
