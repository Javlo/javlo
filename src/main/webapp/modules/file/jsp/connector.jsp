<%@page import="
java.util.Map,
java.util.HashMap,
org.javlo.context.ContentContext,
org.javlo.helper.URLHelper,
org.javlo.context.GlobalContext,
org.javlo.module.file.JavloELFinder
"%><%!
private static final String SESSION_ATTRIBUTE = "javlo.elfinders"; 

private synchronized JavloELFinder getELFinder(HttpSession session, String root) {
	Map<String, JavloELFinder> sessionELFinders = (Map<String, JavloELFinder>)session.getAttribute(SESSION_ATTRIBUTE);
	if (sessionELFinders == null) {
		sessionELFinders = new HashMap<String, JavloELFinder>();
		session.setAttribute(SESSION_ATTRIBUTE, sessionELFinders);
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
GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
String root = URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder());

getELFinder(session, root).process(out, request, response);

%>