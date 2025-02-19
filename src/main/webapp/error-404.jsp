<%@page contentType="text/html"
%><%@page import="java.net.URL"
%><%@page import="org.javlo.helper.URLHelper"
%><%@page import="org.javlo.helper.NetHelper"
%><%@page import="jakarta.servlet.http.HttpServletResponse"
%><%@page import="org.javlo.context.GlobalContext"
%><%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.service.ContentService"%><%
GlobalContext globalContext = GlobalContext.getInstance(request);
ContentContext ctx = ContentContext.getContentContext(request, response);
ctx = new ContentContext(ctx);
ctx.setFormat("html");
ctx.setAbsoluteURL(true);
String url = URLHelper.createURL(ctx, "/");
%>

<html>
<head>
	<style>
		*{
			transition: all 0.6s;
		}

		html {
			height: 100%;
		}

		body{
			font-family: 'Lato', sans-serif;
			color: #888;
			margin: 0;
		}

		#main{
			display: table;
			width: 100%;
			height: 100vh;
			text-align: center;
		}

		.fof{
			display: table-cell;
			vertical-align: middle;
		}

		p {
			margin-top: 0;
			margin-bottom: 1rem;
		}

		a {
			display: block;
			margin-top: 1rem;
		}

		.fof h1{
			font-size: 50px;
			display: inline-block;
			padding-right: 12px;
			animation: type .5s alternate infinite;
		}

		@keyframes type{
			from{box-shadow: inset -3px 0px 0px #888;}
			to{box-shadow: inset -3px 0px 0px transparent;}
		}
	</style>
</head>
<body>
<div id="main">
	<div class="fof">
		<p><%=globalContext.getGlobalTitle()%></p>
		<h1>404 : page not found</h1>
		<a href="<%=url%>">back to home</a>
	</div>
</div>
</body>
</html>