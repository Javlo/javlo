<%@page import="org.javlo.module.content.Edit"%>
<%@page import="org.javlo.context.ContentContext"%><%@page import="org.javlo.helper.URLHelper"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
//if (ctx.getGlobalContext().getStaticConfig().isAddButton()) {
%><c:set var="logged" value="${not empty info.editUser}" />
<c:if test="${empty info.editUser}">
	<div id="preview-login-banner">
		<div class="preview-login-banner-wrapped">
			<span><strong>Javlo</strong> Preview Mode</span>
			<form id="pc_form" method="post" action="<%=URLHelper.createURL(editCtx)%>">
				<c:if test='${!editPreview}'>
					<button class="action btn-login" type="submit" title="${i18n.edit['global.login']}"><span>Login</span>
						<i class="bi bi-person-down"></i>
					</button>
				</c:if>
				<input type="hidden" name="backPreview" value="true" />
			</form>
			<a class="close-preview-banner" href='#' onclick="document.getElementById('preview-login-banner').remove();"><i class="bi bi-x"></i></a>
		</div>
	</div>
</c:if>
<%
//}
%>