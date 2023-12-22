<%@ taglib uri="jakarta.tags.core" prefix="c"%><%@ taglib prefix="fn" uri="jakarta.tags.functions"%><i class="bi bi-house-fill"></i>


<c:if test="${empty currentModule.breadcrumbList}">
	<c:forEach var="page" items="${info.pagePath}" varStatus="status">
		<c:set var="link" value='<a href="${page.url}">' />
		<c:set var="spanHtml" value="<span class='title'>"/>
		<c:if test="${status.first}">
			<c:set var="spanHtml" value="<span class='title first'>"/>
		</c:if>
		${empty param.previewEdit?link:spanHtml}${page.info.title}${empty param.previewEdit?'</a>':'</span>'}
		<c:if test="${fn:length(page.children) > 1 && empty param.previewEdit}">
			<div class="children">
				<div class="container">
					<ul>
						<c:forEach var="child" items="${page.children}">
							<c:url var="url" value="${child.url}" context="/">
								<c:if test="${not empty param[BACK_PARAM_NAME]}">
									<c:param name="${BACK_PARAM_NAME}" value="${param[BACK_PARAM_NAME]}" />
								</c:if>
								<c:if test="${not empty param['select']}">
									<c:param name="select" value="${param['select']}" />
								</c:if>
								<c:if test="${not empty param.previewEdit}">
									<c:param name="previewEdit" value="${param.previewEdit}" />
								</c:if>
							</c:url>
							<li class="${child.selected?'selected':''} ${child.realContent?'real-content':''}"><a href="${url}">${child.info.title}</a></li>
						</c:forEach>
					</ul>
				</div>
			</div>
		</c:if>
		<c:if test="${fn:length(page.children) == 0 && !status.last}"><div class="no-children 1"></div></c:if>
	</c:forEach>
	<c:set var="link" value='<a class="selected" href="${info.currentURL}">' />
	${empty param.previewEdit?link:'<span class="title">'}
	${info.pageTitle}
	${empty param.previewEdit?'</a>':'</span>'}
		<c:if test="${fn:length(info.page.children) > 0 && empty param.previewEdit}">
		<div class="children">
			<div class="container">
				<ul>
					<c:forEach var="child" items="${info.page.children}">
						<c:url var="url" value="${child.url}" context="/">
							<c:if test="${not empty param[BACK_PARAM_NAME]}">
								<c:param name="${BACK_PARAM_NAME}" value="${param[BACK_PARAM_NAME]}" />
							</c:if>
							<c:if test="${not empty param.previewEdit}">
								<c:param name="previewEdit" value="${param.previewEdit}" />
							</c:if>
							<c:if test="${not empty param['select']}">
								<c:param name="select" value="${param['select']}" />
							</c:if>
						</c:url>
						<li class="${child.selected?'selected':''} ${child.realContent?'real-content':''}"><a href="${url}">${child.info.title}</a></li>
					</c:forEach>
				</ul>
			</div>
		</div>
	</c:if>
	
</c:if>
<c:if test="${not empty currentModule.breadcrumbList}">
	<c:forEach var="link" items="${currentModule.breadcrumbList}" varStatus="status">
		<c:if test="${empty link.url || link.readonly && !status.last}">
			<div class="link-wrapper">
		</c:if>
		<c:if test="${not empty link.url && !link.readonly}">
			<c:url var="url" value="${link.url}" context="/">
				<c:if test="${not empty param[BACK_PARAM_NAME]}">
					<c:param name="${BACK_PARAM_NAME}" value="${param[BACK_PARAM_NAME]}" />
				</c:if>
				<c:if test="${not empty param.previewEdit}">
					<c:param name="previewEdit" value="${param.previewEdit}" />
				</c:if>
				<c:if test="${not empty param['select']}">
				<c:param name="select" value="${param['select']}" />
				</c:if>
			</c:url>
			<a ${link.selected?'class="selected"':''} href="${url}" title="${link.title}">
		</c:if>
		<span class="link">${link.legend}</span>
		<c:if test="${not empty link.url  && !link.readonly}">
			</a>
		</c:if>
		<c:if test="${empty link.url || link.readonly && !status.last}">
			</div>
		</c:if>
		<c:if test="${(fn:length(link.children) > 1 || (status.last && fn:length(link.children) > 0))}">
			<div class="children">
				<div class="container">
					<ul>
						<c:forEach var="child" items="${link.children}">
							<c:url var="url" value="${child.url}" context="/">
								<c:if test="${not empty param[BACK_PARAM_NAME]}">
									<c:param name="${BACK_PARAM_NAME}" value="${param[BACK_PARAM_NAME]}" />
								</c:if>
								<c:if test="${not empty param[previewEdit]}">
									<c:param name="previewEdit" value="${param[previewEdit]}" />
								</c:if>
								<c:if test="${not empty param['select']}">
									<c:param name="select" value="${param['select']}" />
								</c:if>
							</c:url>
							<c:if test="${!link.readonly}">
								<li><c:if test="${not empty child.url}">
										<a href="${url}" title="${child.title}">
									</c:if>${child.legend}<c:if test="${not empty child.url}">
										</a>
									</c:if></li>
							</c:if>
							<c:if test="${link.readonly}">
								<li><span class="link">${child.legend}</span></li>
							</c:if>
						</c:forEach>
					</ul>
				</div>
			</div>
		</c:if>
		<c:if test="${fn:length(link.children) <= 1 && !status.last}"><div class="no-children 3"></div></c:if>
	</c:forEach>
</c:if>