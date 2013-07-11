<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="ROBOTS" content="NONE" />
<title>Javlo 2</title>
<link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/style.css" />
<!--[if IE 9]>
    <link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/ie9.css"/>
<![endif]-->

<!--[if IE 8]>
    <link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/ie8.css"/>
<![endif]-->

<!--[if IE 7]>
    <link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/ie7.css"/>
<![endif]-->
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/custom/general.js"></script>
</head>

<body>

<div class="loginlogo">
	<img src="${info.editTemplateURL}/images/logo.png" alt="Logo Javlo" />
</div><!--loginlogo-->

<c:if test="${not empty info.globalMessage && not empty info.globalMessage.message}">
    	<div class="notification notifyError loginNotify">${info.globalMessage}</div>
</c:if>
<form id="loginform" action="${info.currentEditURL}" method="post">
<div class="loginbox">
	<div class="loginbox_inner">
    	<div class="loginbox_content">
    		<input type="hidden" value="adminlogin" name="login-type" />
    		<input type="hidden" value="edit-login" name="edit-login" />
    		<c:if test="${not empty param.previewEdit}">
    		<input type="hidden" name="previewEdit" value="${param.previewEdit}" />
    		</c:if>    		
            <input class="username" id="j_username" type="text" name="j_username" />
            <input class="password" id="j_password" type="password" name="j_password"  />
            <button name="submit" class="submit" >Login</button>
        </div><!--loginbox_content-->
        
    </div><!--loginbox_inner-->
</div><!--loginbox-->

<div class="loginoption">
	<a href="http://www.javlo.org" class="cant">javlo.org</a>
    <input type="checkbox" id="autologin" name="autologin" /> Remember me on this computer.
</div><!--loginoption-->
</form>

</body>
</html>
