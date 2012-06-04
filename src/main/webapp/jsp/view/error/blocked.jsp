<%
response.setStatus(503);
response.flushBuffer();
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
<p><a href="http://www.javlo.org">javlo</a></p>
</div>
</body>
</html>