<%@page import="org.javlo.context.GlobalContext"%>
<%
response.setStatus(503);
response.flushBuffer();
GlobalContext globalContext = GlobalContext.getInstance(request);
%>
<%@page contentType="text/html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<style>
body {
	margin: 0;
	padding: 0;
	background: rgb(27, 45, 73);
	background: linear-gradient(140deg, rgba(27, 45, 73, 1) 0%, rgba(255, 255, 255, 1) 100%);
	background-repeat: no-repeat;
	color: #1b2d49;
}

#main {
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	height: 100vh;
	justify-content: center;
}

.bloc {
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	padding: 1rem;
	border: 1px #ccc solid;
	background-color: rgba(255, 255, 255, .1);
	border-radius: 6px;
}

.message {
	margin: 1rem 0;
	vertical-align: middle;
}

a {
	text-decoration: none;
	color: #fff;
	font-size
	.9rem;
}

img {
	margin: 0 15%;
	width: 70%;
}

h1 {
	font-size: 1.2rem;
	text-align: center;
}

svg {
	margin-bottom: 1rem;
}
</style>

<title>locked : ${info.globalTitle}</title>
</head>
<body id="blocked" lang="en">
	<div id="main">

		<svg xmlns="http://www.w3.org/2000/svg" width="26" height="26" fill="currentColor" class="bi bi-shield-lock" viewBox="0 0 16 16"> <path d="M5.338 1.59a61.44 61.44 0 0 0-2.837.856.481.481 0 0 0-.328.39c-.554 4.157.726 7.19 2.253 9.188a10.725 10.725 0 0 0 2.287 2.233c.346.244.652.42.893.533.12.057.218.095.293.118a.55.55 0 0 0 .101.025.615.615 0 0 0 .1-.025c.076-.023.174-.061.294-.118.24-.113.547-.29.893-.533a10.726 10.726 0 0 0 2.287-2.233c1.527-1.997 2.807-5.031 2.253-9.188a.48.48 0 0 0-.328-.39c-.651-.213-1.75-.56-2.837-.855C9.552 1.29 8.531 1.067 8 1.067c-.53 0-1.552.223-2.662.524zM5.072.56C6.157.265 7.31 0 8 0s1.843.265 2.928.56c1.11.3 2.229.655 2.887.87a1.54 1.54 0 0 1 1.044 1.262c.596 4.477-.787 7.795-2.465 9.99a11.775 11.775 0 0 1-2.517 2.453 7.159 7.159 0 0 1-1.048.625c-.28.132-.581.24-.829.24s-.548-.108-.829-.24a7.158 7.158 0 0 1-1.048-.625 11.777 11.777 0 0 1-2.517-2.453C1.928 10.487.545 7.169 1.141 2.692A1.54 1.54 0 0 1 2.185 1.43 62.456 62.456 0 0 1 5.072.56z" /> <path
			d="M9.5 6.5a1.5 1.5 0 0 1-1 1.415l.385 1.99a.5.5 0 0 1-.491.595h-.788a.5.5 0 0 1-.49-.595l.384-1.99a1.5 1.5 0 1 1 2-1.415z" /> </svg>


		<div class="bloc">

			<div class="logo">
				<c:if test="${not empty info.logoUrl}">
					<img src="${info.logoUrl}" alt="${info.globalTitle}" />
				</c:if>
				<c:if test="${empty info.logoUrl}">
					<h1>${info.globalTitle}</h1>
				</c:if>
			</div>


			<div class="message">This page is closed for the moment.</div>

			<c:if test="${empty edit}">
				<%
				if (globalContext.getBlockPassword() != null && globalContext.getBlockPassword().trim().length() > 0) {
				%>
				<form class="loginbox" id="form-block" method="post">
					<input class="password" type="password" name="block-password" />
				</form>
				<%
				}
				%>
			</c:if>
			<c:if test="${not empty param.message}">
				<div class="alert alert-danger">${param.message}</div>
			</c:if>
			<p>
				<a href="http://www.javlo.org">javlo.org</a>
			</p>
		</div>
	</div>
</body>
</html>