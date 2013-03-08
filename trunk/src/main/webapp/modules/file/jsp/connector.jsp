<%@page import="
java.util.Map,
java.util.HashMap,
org.javlo.context.ContentContext,
org.javlo.module.file.FileModuleContext,
org.javlo.helper.URLHelper,
org.javlo.context.GlobalContext,
org.javlo.module.file.JavloELFinder,
org.javlo.user.AdminUserSecurity,
org.javlo.module.core.ModulesContext
"%><%!
private static final String SESSION_ATTRIBUTE = "javlo.elfinders"; 

private synchronized JavloELFinder getELFinder(HttpSession session, String root) {	
	Map<String, JavloELFinder> sessionELFinders = (Map<String, JavloELFinder>)session.getAttribute(SESSION_ATTRIBUTE+root);
	if (sessionELFinders == null) {
		sessionELFinders = new HashMap<String, JavloELFinder>();
		session.setAttribute(SESSION_ATTRIBUTE+root, sessionELFinders);
	}
	JavloELFinder elfinder = sessionELFinders.get(root);
	if (elfinder == null) {
		elfinder = new org.javlo.module.file.JavloELFinder(root, session.getServletContext());
		sessionELFinders.put(root, elfinder);
	}
	return elfinder;
}

%><%
ContentContext ctx = ContentContext.getContentContext(request, response);
GlobalContext globalContext = GlobalContext.getSessionInstance(ctx.getRequest().getSession());
String root;
if (request.getParameter("changeRoot") == null) {
	if (AdminUserSecurity.getInstance().isGod(ctx.getCurrentEditUser())) {
		root = globalContext.getDataFolder();
	} else {
		root = URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder());
	}
} else {
	root = ((FileModuleContext)FileModuleContext.getInstance(session, globalContext, ModulesContext.getInstance(session, globalContext).getCurrentModule(), FileModuleContext.class)).getRoot();
}
getELFinder(session, root).process(out, request, response);
%>