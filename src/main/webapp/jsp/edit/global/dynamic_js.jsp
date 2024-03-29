<%@page import="org.javlo.css.CssColor"%>
<%@page contentType="text/html"
        import="
        org.javlo.context.ContentContext,
        org.javlo.config.StaticConfig,
        org.javlo.data.InfoBean"  
%><%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %><%
ContentContext ctx = ContentContext.getContentContext(request, response);
InfoBean.getCurrentInfoBean(ctx);
StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
%>

var currentURL = "${info.currentURL}";
var currentAjaxURL = "${info.currentAjaxURL}";
var staticRootURL = "${info.staticRootURL}";
var dateFormat = "<%=staticConfig.getDefaultJSDateFormat()%>";
var editLanguage = "${info.editLanguage}";
<c:set var="sep" value="" />
var contentLanguage = [<c:forEach var="lg" items="${info.contentLanguages}">${sep}'${lg}'<c:set var="sep" value="," /></c:forEach>];

var isPreview = currentURL.indexOf("/preview") >= 0;

var i18n = [];
i18n.validation='${i18n.edit['global.validation']}';
i18n.confirm='${i18n.edit['global.confirm-delete']}';

<%if (ctx.getGlobalContext().getTemplateData().isColorListFilled()) {
	String sep="";
	%>
	var colorList = [<%for (CssColor color : ctx.getGlobalContext().getTemplateData().getColorList()) {if (color!=null) {%><%=sep%>'<%=color%>','<%=color%>'<%sep=",";} }%>];
	<%
} else {%>
   var colorList = null;
<%}%>
