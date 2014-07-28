<%@page contentType="text/html"
%><%@page import="
org.javlo.context.GlobalContext,
org.javlo.user.AdminUserFactory,
org.javlo.user.User,
org.javlo.helper.URLHelper,
org.javlo.context.ContentContext
"%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%
GlobalContext globalContext = GlobalContext.getInstance(request);
AdminUserFactory userFactory = AdminUserFactory.createAdminUserFactory(globalContext, request.getSession());
User user = userFactory.getUser(""+request.getAttribute("creator"));
ContentContext ctx = ContentContext.getContentContext(request, response);
if (user != null) {
	String avatarURL = URLHelper.createAvatarUrl(ctx, user.getUserInfo());
	pageContext.setAttribute("avatarURL", avatarURL);
}
if (user != null) {
	request.setAttribute("user", user);
%>
<div class="jcreator ${elem.authors} ${elem.type} ${samePrevious?'previous-same':'previous-different'}">
	<c:if test="${not empty avatarURL}"><span class="avatar"><img src="${avatarURL}" alt="avatar ${elem.authors}" /></span></c:if>
	<span class="username">${elem.authors}</span>
	<c:if test="${not empty user.userInfo.email}"><span class="email"><a href="mailto:${user.userInfo.email}">${user.userInfo.email}</a></span></c:if>
	<span class="date">${date}</span>	
</div>
<%
} else {%>
<div class="jcreator notfound">
	<span class="username">${creator}</span>
</div>
<%}%>
