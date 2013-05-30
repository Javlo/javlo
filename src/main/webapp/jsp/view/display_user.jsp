<%@page contentType="text/html"
%><%@page import="
org.javlo.context.GlobalContext,
org.javlo.user.AdminUserFactory,
org.javlo.user.User
"%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%
GlobalContext globalContext = GlobalContext.getInstance(request);
AdminUserFactory userFactory = AdminUserFactory.createAdminUserFactory(globalContext, request.getSession());
User user = userFactory.getUser(""+request.getAttribute("creator"));
if (user != null) {
	request.setAttribute("user", user);
%>
<div class="jcreator ${elem.authors} ${elem.type}">
	<c:if test="${not empty user.userInfo.avatarURL}"><span class="avatar"><img src="${user.userInfo.avatarURL}" alt="avatar ${elem.authors}" /></span></c:if>
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
