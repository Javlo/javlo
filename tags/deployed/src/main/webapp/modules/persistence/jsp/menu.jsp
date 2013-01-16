<%@page import="org.javlo.helper.NavigationHelper"
%><%@page import="org.javlo.user.AdminUserFactory"
%><%@page import="org.javlo.helper.URLHelper"
%><%@page import="org.javlo.user.AdminUserSecurity"
%><%@page import="org.javlo.user.IUserFactory"
%><%@page import="org.javlo.navigation.MenuElement"
%><%@page import="org.javlo.context.Content"
%><%@page import="org.javlo.context.EditContext"
%><%@page import="org.javlo.i18n.I18nAccess"
%><%@page import="org.javlo.context.GlobalContext"
%><%@page import="org.javlo.context.ContentContext"
%><%
ContentContext ctx = ContentContext.getContentContext ( request, response );
GlobalContext globalContext = GlobalContext.getInstance(request);
I18nAccess i18nAccess = I18nAccess.getInstance ( globalContext, request.getSession() );
EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());

Content content = Content.createContent(request);

String lg = ctx.getLanguage();
String path = ctx.getPath();

final String DISPLAY_CHILDREN_KEY = "__display_children__";

MenuElement menu  = content.getNavigation(ctx).getNoErrorFreeCurrentPage(ctx);

MenuElement[] elems = menu.getChildMenuElementsWithVirtual(ctx, false, false);
if (menu.getParent() != null && request.getAttribute(DISPLAY_CHILDREN_KEY) == null) {
	menu = menu.getParent();
	elems = menu.getChildMenuElements();
}

// Display the menu

IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
AdminUserSecurity userSecurity = AdminUserSecurity.getInstance(application);
boolean validator = globalContext.isPortail()&&userSecurity.haveRight(adminUserFactory.getCurrentUser(request.getSession()), AdminUserSecurity.VALIDATION_ROLE);

if (request.getAttribute(DISPLAY_CHILDREN_KEY) == null) {%><div id="menu"><%}%>
<ul><%
if (menu != null && request.getAttribute(DISPLAY_CHILDREN_KEY) == null) {
%><li class="parent"><a href="<%=URLHelper.createURL( ctx, menu.getPath() )%>" title="<%=menu.getFullName()%>"><%=menu.getLabel(ctx)%></a>
<%}
%>
<ul class="menu"><%

String addClass="";
int unvalidSearch = 0;
if ((!menu.isValid())&&(editCtx.haveLicence("portail"))) {
	addClass = " unvalid";
} 

unvalidSearch = NavigationHelper.countUnvalidChildren(menu);
if ( menu == null ) {%>
<li><a class="selected<%=addClass%>" href="<%=URLHelper.createURL( ctx, menu.getPath() )%>" title="<%=menu.getFullName()%>"><%=menu.getLabel(ctx)%><%=(((unvalidSearch>0)&&validator)?" <span class=\"unvalid\">["+unvalidSearch+"]</span>":"")%></a>
<%
}

for ( int i=0; i<elems.length; i++ ) {
boolean virtualPage = elems[i].getParent() != menu;
try {
	String selected = "";
	String imageBefore = URLHelper.createStaticURL(ctx, "/images/edit/cross_empty.gif" );
	String altBefore = "&nbsp;";
	if ( elems[i].getChildMenuElements().length > 0 ) {
		imageBefore = URLHelper.createStaticURL(ctx, "/images/edit/cross_closed.gif" );
		altBefore = "+";
		if ( elems[i].isSelected(ctx) ) {
			imageBefore = URLHelper.createStaticURL(ctx, "/images/edit/cross_opened.gif" );
			altBefore = "-";
		}
	}
	if ( elems[i].getPath().equals(ctx.getPath()) ) {
		selected="class=\"current\"";
	}%>	
	<li <%=selected%>><%
	unvalidSearch = 0;
	addClass = "";
	if ((!elems[i].isValid())&&(globalContext.isPortail())) {
		addClass = " unvalid";
	} 
	unvalidSearch = NavigationHelper.countUnvalidChildren(elems[i]);	
	%>
	<div class="label<%=addClass%>">
	<img style="margin-bottom: 1px;" width="7" height="7" src="<%=imageBefore%>" alt="<%=altBefore%>" />
	<a <%=selected%> href="<%=URLHelper.createURL( ctx, elems[i].getPath() )%>" title="<%=elems[i].getFullName()%>"><%
	if (virtualPage) {%>(<%}
	%><%=elems[i].getLabel(ctx)%><%=(((unvalidSearch>0)&&validator)?" <span class=\"unvalid\">["+unvalidSearch+"]</span>":"")%><%
	if (virtualPage) {%>)<%}
	%>
	</a>
	</div>
	</li><%
	if ( ( elems[i].getChildMenuElementsWithVirtual(ctx, false, false).length > 0 ) && (elems[i].isSelected(ctx) && !virtualPage) ) {	
		request.setAttribute(DISPLAY_CHILDREN_KEY, DISPLAY_CHILDREN_KEY);
		%><li><jsp:include page="menu.jsp"/></li>
<%  }
} catch ( Exception e ) {
	e.printStackTrace();
}

}

%>
</ul>
</li></ul><%if (request.getAttribute(DISPLAY_CHILDREN_KEY) == null ) {%></div><%}%>
