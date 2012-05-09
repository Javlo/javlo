<%@page import="org.javlo.context.ContentContext,org.javlo.helper.URLHelper,org.javlo.context.GlobalContext"%><%
ContentContext ctx = ContentContext.getContentContext(request, response);
GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
String root = URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder());

new org.javlo.module.file.ELFinder(root, application.getRealPath("/modules/file/")).process(out, request, response); %>