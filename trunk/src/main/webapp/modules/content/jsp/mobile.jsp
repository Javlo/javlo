<%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.helper.ServletHelper"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%
ServletHelper.execAction(ContentContext.getContentContext(request, response));
%><!DOCTYPE html>
<html lang="${info.language}">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard</title>
    <link href="../mobcss/mobile.css" rel="stylesheet" />    
</head>
<body>
<a class="action-button publish" href="${info.currentAbsoluteURL}?webaction=edit.publish&render-mode=1"><span>${i18n.edit['command.publish']}</span></a>
<script type="text/javascript">
function onMobileLoad() {
	var out = {
			message: '${messages.globalMessage.message}'
		};
	return JSON.stringify(out);
}
</script>
</body>