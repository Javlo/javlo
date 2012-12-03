<%@page contentType="text/html"
        import="
        org.javlo.context.ContentContext,
        org.javlo.config.StaticConfig"  
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%
StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
%>

var currentURL = "${info.currentURL}";
var dateFormat = "<%=staticConfig.getDefaultJSDateFormat()%>";
var editLanguage = "${info.editLanguage}";

var isPreview = currentURL.indexOf("/preview") >= 0;

var i18n = [];
i18n.validation='${i18n.edit['global.validation']}';
i18n.confirm='${i18n.edit['global.confirm-delete']}';