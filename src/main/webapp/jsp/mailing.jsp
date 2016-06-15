<%@page import="org.javlo.helper.StringHelper"%>
<%@page import="org.javlo.helper.XHTMLHelper"%>
<%@page import="java.net.URL"%>
<%@page import="org.javlo.helper.NetHelper"%>
<%@page import="org.javlo.context.ContentContext"%>
<%@page import="org.javlo.cache.ICache"%>
<%@page import="org.javlo.context.GlobalContext"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%>
<%
ContentContext ctx = ContentContext.getContentContext(request, response);
GlobalContext gc = GlobalContext.getInstance(request);

String url = request.getParameter("url");
if (url != null && url.trim().length() > 0) {
	String content = NetHelper.readPageGet(new URL(url));
	content = StringHelper.toHTMLAttribute(StringHelper.removeCR(content));
	request.setAttribute("mailContent", content);
}

%>
<html>
<head>

<style type="text/css">

body {
	width: 800px;
	margin: 30px auto;
	font-family: sans-serif;	
}

input, textarea {
	width: 100%;
}

textarea {
	height: 300px;
}


</style>

</head>
<body>


<form>
	<label for="url">url : </label>
	<input type="text" id="url" name="url" value="${param.url}" />
</form>

<c:if test="${not empty mailContent}">

	<p><a href="mailto:">empty mail</a></p>
	<p><a href="mailto:?subject=subject&body=${mailContent}">send mail</a></p>


	<textarea>${mailContent}</textarea>
</c:if>

</body>
</html>