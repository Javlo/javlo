<%@page import="org.javlo.component.links.PageMirrorComponent"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
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
		org.javlo.component.column.ColumnContext,
		org.javlo.component.container.IContainer,
		org.javlo.context.GlobalContext,
		org.javlo.user.User,
		org.javlo.user.UserFactory,
		org.javlo.user.IUserFactory,
		org.javlo.user.AdminUserSecurity,
		org.javlo.component.core.ComponentBean,				
		org.javlo.helper.URLHelper"
%><%
ContentContext ctx = ContentContext.getContentContext ( request, response );

GlobalContext globalContext = GlobalContext.getInstance(request);
boolean pageEmpty = true;

boolean editPage = !StringHelper.isTrue(request.getParameter(PageMirrorComponent.NOT_EDIT_PREVIEW_PARAM_NAME));

IContentVisualComponent specificComp = (IContentVisualComponent)request.getAttribute("specific-comp");

if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && specificComp == null && editPage) {
	%><div id="one-component-edit"></div><%
}

String area = (String)request.getAttribute(ContentContext.CHANGE_AREA_ATTRIBUTE_NAME);
if (area==null) {
	area = request.getParameter("area");
} else {
	request.removeAttribute(ContentContext.CHANGE_AREA_ATTRIBUTE_NAME);
}

if (area != null) {
	ctx.setArea(area);
} else {
	ctx.setArea(ComponentBean.DEFAULT_AREA);
	area=ComponentBean.DEFAULT_AREA;
}
if (specificComp != null) {
	area = specificComp.getArea();
	ctx.setArea(area);
}
request.setAttribute("area", area);

String path = request.getParameter("_wcms_content_path");
if (path!=null) {
	ctx = new ContentContext(ctx);
	ctx.setPath(path);
}

Boolean removeRepeat = StringHelper.isTrue(request.getParameter("_no-repeat"));

AdminUserSecurity security = AdminUserSecurity.getInstance();

if ( ctx.getSpecialContentRenderer() != null && area.equals(ComponentBean.DEFAULT_AREA)) {
	%><jsp:include page="<%=ctx.getSpecialContentRenderer()%>" /><%
} else if ( ctx.getSpecialContentRenderer() != null) {
	%><!-- this area is empty because special rederer is defined. --><%
} else {
	
MenuElement currentPage = ctx.getCurrentPage();

Template template = ctx.getCurrentTemplate();
Stack<IContainer> containers = new Stack<IContainer>();

%><%-- <!-- DEBUG INFO -->
<ul style="padding: 10px; margin: 10px; border: 2px dashed red;">
<li>area = <%=area %></li>
<li>path = <%=ctx.getPath()%></li>
<li>currentPage = <%=currentPage.getName()%></li>
<li>template = <%=template.getName()%></li>
</ul> --%><%

if ( (ctx.getSpecialContentRenderer() == null || !area.equals(ComponentBean.DEFAULT_AREA) ) || template.getAreasForceDisplay().contains(area)) { // display only if page contains only repeat content (supose it is teaser)

Map<String, String> replacement = currentPage.getReplacement();

IContentComponentsList elems = null;
if (specificComp == null) {
	elems = currentPage.getContent(ctx);
}
IContentVisualComponent elem = null;
IContentVisualComponent previousElem = null;

	boolean languageChange = !ctx.getContentLanguage().equals(ctx.getLanguage()); 
	if (languageChange) {
		%><div lang="<%=ctx.getContentLanguage()%>"><%
	}	
	if (!elems.hasNext(ctx) && EditContext.getInstance(globalContext, session).isEditPreview() && ctx.isAsPreviewMode() && editPage) {
		%><div class="_empty_area"><span><%=ctx.getArea()%></span></div><%
	} else {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && specificComp == null && editPage) {
			%><div id="comp_0" class="free-edit-zone editable-component"><span>&nbsp;</span></div><%
		}
	}
	
	while (specificComp != null || (elems != null && elems.hasNext(ctx))) {
		pageEmpty = false;
		if (specificComp == null) {
			elem = elems.next(ctx);
		} else {
			elem = specificComp;
			specificComp = null;
		}
		if (!(removeRepeat && elem.isRepeat() && !elem.getPage().equals(currentPage))) {
			elem.clearReplacement();
			elem.replaceAllInContent(replacement);
		
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
			
			%><%if (elems != null) {%><%=elems.getPrefixXHTMLCode(ctx)
%><%}
%><%
if (globalContext.isCollaborativeMode() && ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
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
if (StringHelper.removeTag(xhtmlCode).trim().length() == 0 && !xhtmlCode.toLowerCase().contains("<img")) {
	I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
	xhtmlCode = '['+i18nAccess.getText("content."+elem.getType(), elem.getType())+']';
}%></c:if><%=elem.getPrefixViewXHTMLCode(ctx)%><%=xhtmlCode%>
<%=elem.getSuffixViewXHTMLCode(ctx)%>
<%
previousElem = elem;
if (elems != null) {%><%=elems.getSufixXHTMLCode(ctx)
%><%}%><%		
			elem.setValue(savedValue);
			if (elem.next() == null) {
				ColumnContext columnContext = ColumnContext.getInstance(request);
				if (columnContext.isOpen()) {
					if (columnContext.isWithTable()) {
						%></td></tr></table><%
					} else {
						%></div><%
					}
				}			
				
			}
		}	
	}
	while (!containers.empty()) {
		%><%=containers.pop().getCloseCode(ctx)%><%
	}
	if (languageChange) {
		%></div><%
	}
}
currentPage.endRendering(ctx);
} /* end else getSpecialContentRenderer() */

%>