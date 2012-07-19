<%@page import="org.javlo.context.GlobalContext"%>
<%
response.setStatus(503);
response.flushBuffer();
GlobalContext globalContext = GlobalContext.getInstance(request);
%>
<%@page contentType="text/html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link rel="stylesheet" media="screen" href="/css/view/view.css" />
<title>locked</title>
</head>
<body id="blocked" lang="en">
<div id="main">

<c:if test="${empty edit}">
<%if (globalContext.getBlockPassword() != null && globalContext.getBlockPassword().trim().length() > 0) {%>
<form class="loginbox" id="form-block" method="post">
<input class="password" type="password" name="block-password" />
</form>
<%}%>
</c:if>

<p><a href="http://www.javlo.org">javlo.org</a></p>
</div>
</body>
</html>