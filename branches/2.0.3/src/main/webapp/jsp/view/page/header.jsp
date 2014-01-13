<%@page contentType="text/html" pageEncoding="UTF-8"
        import="java.util.Date,
                org.javlo.helper.URLHelper,
                org.javlo.context.ContentContext,
                org.javlo.service.ContentService,
                org.javlo.data.InfoBean,
                org.javlo.i18n.I18nAccess,
                org.javlo.helper.StringHelper,
                org.javlo.user.User,
                org.javlo.context.EditContext,
                org.javlo.helper.XHTMLHelper,
                org.javlo.message.MessageRepository,
                org.javlo.user.AdminUserSecurity,
                org.javlo.navigation.MenuElement,
                org.javlo.helper.XHTMLNavigationHelper,
                org.javlo.context.GlobalContext"
%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv" %>
<%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentService content = ContentService.getInstance(request);
GlobalContext globalContext = GlobalContext.getInstance(request);
MenuElement currentPage = ctx.getCurrentPage();
InfoBean infoBean = InfoBean.getCurrentInfoBean(request);
String currentTitle = currentPage.getPageTitle(ctx);
String pageName = currentPage.getName();

String globalTitle = currentPage.getGlobalTitle(ctx);if (globalTitle == null) { globalTitle = globalContext.getGlobalTitle();}
I18nAccess i18nAccess = I18nAccess.getInstance(request);
AdminUserSecurity security = AdminUserSecurity.getInstance();%>
