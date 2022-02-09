<%@page import="org.javlo.component.container.Box"%>
<%@page import="org.javlo.data.InfoBean"%>
<%@page import="java.util.List"
%><%@page import="org.javlo.component.core.ComponentFactory"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@page contentType="text/html"
        import="
        java.util.Map,
        java.util.Stack,
        org.javlo.i18n.I18nAccess,
        org.javlo.helper.StringHelper,
        org.javlo.context.ContentContext,
        org.javlo.context.EditContext,
        org.javlo.context.ContentManager,
		org.javlo.template.Template,
        org.javlo.navigation.MenuElement,
		org.javlo.component.core.ContentElementList,
		org.javlo.component.core.IContentComponentsList,
		org.javlo.component.core.IContentVisualComponent,
		org.javlo.component.links.PageMirrorComponent,		
		org.javlo.component.container.IContainer,
		org.javlo.component.column.TableContext,
		org.javlo.component.column.TableBreak,
		org.javlo.component.column.ColContext,
		org.javlo.component.column.OpenCol,
		org.javlo.context.GlobalContext,		
		org.javlo.service.ContentService,
		org.javlo.user.User,
		org.javlo.user.UserFactory,
		org.javlo.user.IUserFactory,
		org.javlo.user.AdminUserSecurity,
		org.javlo.component.core.ComponentBean,
		org.javlo.helper.URLHelper,
		org.javlo.helper.LocalLogger"
%><%
ContentContext ctx = ContentContext.getContentContext ( request, response );
GlobalContext globalContext = GlobalContext.getInstance(request);

boolean areaWrapper = StringHelper.isTrue(request.getParameter("only-area-wrapper"), false);
String area = (String)request.getAttribute(ContentContext.CHANGE_AREA_ATTRIBUTE_NAME);
if (area==null) {
	area = request.getParameter("area");
} else {
	request.removeAttribute(ContentContext.CHANGE_AREA_ATTRIBUTE_NAME);
}
MenuElement currentPage = ctx.getCurrentPage();
if (areaWrapper && area != null) {
	List<String> layouts = currentPage.getLayouts(ctx);
	String layoutClass = "pos-"+currentPage.getPosition();
	if (layouts.size() > 0) {
		for (String layout : layouts) {
			layoutClass+=" layout-"+layout;
		}		
	}
	%><div id="<%=area%>" class="<%=layoutClass%>"><%
}

boolean pageEmpty = true;
boolean editPage = !StringHelper.isTrue(request.getParameter(PageMirrorComponent.NOT_EDIT_PREVIEW_PARAM_NAME));
IContentVisualComponent specificComp = (IContentVisualComponent)request.getAttribute("specific-comp");
if (specificComp == null && request.getParameter("_only_component") != null) {
	ContentService contentService = ContentService.getInstance(ctx.getRequest()); 
	specificComp = contentService.getComponent(ctx, request.getParameter("_only_component"));
}

if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && !ctx.isPreviewOnly() && specificComp == null && editPage) {
	%><div id="one-component-edit"></div><%
}

if (area != null) {
	ctx.setArea(area);
} else {
	ctx.setArea(ComponentBean.DEFAULT_AREA);
	area=ComponentBean.DEFAULT_AREA;
}
request.setAttribute("area", area);
if (specificComp != null && !area.equals(specificComp.getArea())) {
	specificComp=null;
}

// if (specificComp != null) {
// 	area = specificComp.getArea();
// 	ctx.setArea(area);
// }

String path = request.getParameter("_wcms_content_path");
if (path!=null) {
	ctx = new ContentContext(ctx);
	ctx.setPath(path);
}

String manualContent = request.getParameter(ContentContext.FORCED_CONTENT_PREFIX+area);
if (manualContent != null) {
	%><%=globalContext.getForcedContent(manualContent)%><%
	return;
}

Boolean removeRepeat = StringHelper.isTrue(request.getParameter("_no-repeat"));
Boolean displayZone = StringHelper.isTrue(request.getParameter("_display-zone"));

AdminUserSecurity security = AdminUserSecurity.getInstance();
if ( ctx.getSpecialContentRenderer() != null && area.equals(ComponentBean.DEFAULT_AREA) && !ctx.getCurrentTemplate().isNosecureArea(area)) {
	%><jsp:include page="<%=ctx.getSpecialContentRenderer()%>" /><%
} else if ( ctx.getSpecialContentRenderer() != null && !ctx.getCurrentTemplate().isNosecureArea(area)) {
	%><!-- this area is empty because special renderer is defined. --><%
} else {

String forcePageName = request.getParameter("_force-page");
if (forcePageName != null) {
	ContentService contentService = ContentService.getInstance(ctx.getRequest()); 
	MenuElement forcedPage = contentService.getNavigation(ctx).searchChildFromName(forcePageName);
	if (forcedPage != null) {
		currentPage = forcedPage;
	}
}
Template template = ctx.getCurrentTemplate();
Stack<IContainer> containers = new Stack<IContainer>();

%><%-- <!-- DEBUG INFO -->
<ul style="padding: 10px; margin: 10px; border: 2px dashed red; list-style: none;">
<li>area = <%=area %></li>
<li>path = <%=ctx.getPath()%></li>
<li>currentPage = <%=currentPage.getName()%></li>
<li>template = <%=template.getName()%></li>
<li>area : <%=ctx.getArea()%> - lg:<%=ctx.getRequestContentLanguage()%></li>
</ul> --%><%

if ( (ctx.getSpecialContentRenderer() == null || !area.equals(ComponentBean.DEFAULT_AREA) ) || template.getAreasForceDisplay().contains(area)) { // display only if page contains only repeat content (supose it is teaser)

IContentComponentsList elems = null;
if (specificComp == null) {
	elems = currentPage.getContent(ctx);
}
IContentVisualComponent elem = null;
IContentVisualComponent previousElem = null;
InfoBean info = InfoBean.getCurrentInfoBean(ctx);
	boolean languageChange = !ctx.getContentLanguage().equals(ctx.getLanguage());
	if (ctx.getArea().equals(ComponentBean.DEFAULT_AREA) && !StringHelper.isEmpty(info.getPageNotFoundMessage())) {
		%><div lang="${contentContext.mainLanguage}" class="alert alert-info alert-bad-lang">${info.pageNotFoundMessage}</div><%
	}
	if (languageChange) {
		%><div lang="<%=ctx.getContentLanguage()%>"><%
	}	
	if (elems != null && !elems.hasNext(ctx) && EditContext.getInstance(globalContext, session).isPreviewEditionMode() && ctx.isAsPreviewMode() && editPage || displayZone) {
		%><span><%=ctx.getArea()%></span><%
	} else {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && !ctx.isPreviewOnly() && specificComp == null && editPage) {
			%><div id="comp_0" class="free-edit-zone editable-component"><span>${i18n.edit['component.insert.first']}</span></div><%
		}
	}
	
	if (!displayZone) {
	while (specificComp != null || elems.hasNext(ctx)) {
		pageEmpty = false;
		if (specificComp == null) {
			elem = elems.next(ctx);
		} else {
			elem = specificComp;
			specificComp = null;
		}
		
		if (!(removeRepeat && elem.isRepeat() && !elem.getPage().equals(currentPage))) {
			elem.clearReplacement();			
			elem.replaceAllInContent(currentPage.getReplacement());
		
			out.flush(); /* needed for jsp include */
			
			String savedValue = elem.getValue(ctx);
			String value = elem.getValue(ctx);
			
			savedValue = elem.getValue(ctx);
			value = elem.getValue(ctx);
			
			if (elem instanceof IContainer) {
				IContainer container = (IContainer)elem;
				if (container.isOpen(ctx)) {
					containers.push(container);
				} else {
					if (!containers.empty()) {
						containers.pop();
					}
				}
			}			
			if (elems != null) {%><%=elems.getPrefixXHTMLCode(ctx)%><%}
if (globalContext.isCollaborativeMode() && ctx.getRenderMode() == ContentContext.PREVIEW_MODE && !ctx.isPreviewOnly()) {
	request.setAttribute("elem", elem);	
	if (previousElem != null && previousElem.getAuthors().equals(elem.getAuthors())) {
		request.setAttribute("samePrevious", new Boolean(true));		
	} else {
		request.setAttribute("samePrevious", new Boolean(false));		
	}	
	request.setAttribute("creator", elem.getAuthors());	
	request.setAttribute("date", StringHelper.renderTime(elem.getModificationDate()));%><jsp:include page="display_user.jsp"></jsp:include><%
}
String xhtmlCode = elem.getXHTMLCode(ctx);
%><c:if test="${editPreview}"><%
if (!elem.isDisplayable(ctx)) {
	I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
	xhtmlCode = elem.getEmptyXHTMLCode(ctx);	
}%></c:if><%if (!elem.isHiddenInMode(ctx, ctx.getRenderMode(), ctx.isMobile())) {%><%=elem.isVisibleFromCookies(ctx)?elem.getPrefixViewXHTMLCode(ctx):""%><%=xhtmlCode%><%=elem.isVisibleFromCookies(ctx)?elem.getSuffixViewXHTMLCode(ctx):""%><%}
previousElem = elem;
if (elems != null) {%><%=elems.getSufixXHTMLCode(ctx)
%><%}%><%
		}	
	}
}
	while (!containers.empty()) {
		IContainer container = (IContainer)containers.pop();
		ctx.setNoCache(true);
		container.prepareView(ctx);
		container.setOpen(ctx, false);
		%><%=container.getXHTMLCode(ctx)%><%
		ctx.setNoCache(false);
		container.setOpen(ctx, true);
	}
	if (languageChange) {
		%></div><%
	}
}

if (TableContext.isInstance(ctx)) {
	TableContext tableContext = TableContext.getInstance(ctx, null);
	if (tableContext.isTableOpen()) {	
		%><%=TableBreak.closeTable(ctx, tableContext)%><%
	}
}

if (ColContext.isInstance(ctx)) {
	ColContext colContext = ColContext.getInstance(ctx, null);
	if (colContext.isOpen()) {
		%><%=OpenCol.closeRow(ctx, colContext)%><%
	}
}
if (displayZone) {
	%><script>pjq("#<%=area%>").addClass("_empty_area");</script><%
}%><%
String pageClass = "";
if (request.getAttribute("pageNumber") != null) {
	pageClass = ".page-"+request.getAttribute("pageNumber");
}
if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && !ctx.isPreviewOnly()) {
	if (pageEmpty) {
		%><script>pjq("<%=pageClass%> #<%=area%>").addClass("_empty_area");  pjq("<%=pageClass%> #<%=area%>").removeClass("_not_empty_area");</script><%
	} else {
		%><script>pjq("<%=pageClass%> #<%=area%>").removeClass("_empty_area"); pjq("#<%=area%>").removeClass("drop-selected");</script><%
	}
}

currentPage.endRendering(ctx);
} /* end else getSpecialContentRenderer() */

%><%if (ctx.getCurrentTemplate().isEndAreaTag()) {%><div class="end-area end-area-<%=area%>">&nbsp;</div><%}%><%
if (areaWrapper && area != null) {%></div><%}%>