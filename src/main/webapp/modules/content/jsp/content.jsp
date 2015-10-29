<%@page contentType="text/html"
        import="
        java.util.Stack,
        java.util.Map,
        java.util.Hashtable,
        java.util.LinkedList,
        java.util.Collection,
        org.javlo.service.ClipBoard,
        org.javlo.context.ContentContext,
        org.javlo.context.EditContext,
        org.javlo.service.ContentService,	    
	    org.javlo.i18n.I18nAccess,
   	    org.javlo.helper.URLHelper,
   	    org.javlo.helper.XHTMLHelper,
   	    org.javlo.context.GlobalContext,
   	    org.javlo.navigation.MenuElement,
   	    org.javlo.user.AdminUserSecurity,
   	    org.javlo.context.UserInterfaceContext,
   	    org.javlo.helper.StringHelper,
   	    org.javlo.component.core.ComponentContext,
	    org.javlo.component.core.IContentVisualComponent,
	    org.javlo.context.GlobalContext,
	    org.javlo.component.core.ComponentFactory,
	    org.javlo.component.container.IContainer,
	    org.javlo.component.core.IContentComponentsList"
%><%
ContentContext ctx = ContentContext.getContentContext ( request, response );

if (ctx.getCurrentPage() == null) {	
	return;
}

GlobalContext globalContext = GlobalContext.getInstance(request);
ContentService content = ContentService.getInstance(globalContext);
EditContext editContext = EditContext.getInstance(globalContext, request.getSession());

boolean admin = AdminUserSecurity.getInstance().isAdmin(editContext.getEditUser());

int openCount = 0;

/** force edit mode in previe mode **/
ctx.setRenderMode(ContentContext.EDIT_MODE);
ctx.setArea(editContext.getCurrentArea());

I18nAccess i18nAccess = I18nAccess.getInstance ( globalContext, request.getSession() );

ClipBoard clipBoard = ClipBoard.getInstance(request);

Stack<String> closeContainerStack = new Stack<String>();

IContentVisualComponent currentTypeComponent = ComponentFactory.getComponentWithType(ctx, editContext.getActiveType());

String typeName = StringHelper.getFirstNotNull( currentTypeComponent.getComponentLabel(ctx,globalContext.getEditLanguage(request.getSession())), i18nAccess.getText ( "content."+currentTypeComponent.getType()));
String insertHere = i18nAccess.getText("content.insert-here", new String[][] {{"type",typeName}});

String pastePageHere = null;
if (editContext.getContextForCopy(ctx) != null) {
	pastePageHere = i18nAccess.getText("content.paste-here", new String[][] { { "page", editContext.getContextForCopy(ctx).getCurrentPage().getName() } });
	request.setAttribute("cleanClipBoard","true");
}

String pasteHere = null;
if (clipBoard.getCopiedComponent(ctx) != null) {
	pasteHere = i18nAccess.getText("content.paste-comp", new String[][] { { "type", clipBoard.getCopiedComponent(ctx).getType() } });
	request.setAttribute("cleanClipBoard","true");
}

ComponentContext componentContext = ComponentContext.getInstance(request);
Collection<IContentVisualComponent> components = componentContext.getNewComponents();

if (components.size() == 0 || request.getParameter("firstLine") != null) { /* if no specific components asked than render all components for the current context or force first line */
String previousId = "0";
if (components.size() > 0 && components.iterator().next().getPreviousComponent() != null) {
	previousId = components.iterator().next().getPreviousComponent().getId();
}

/*** rendering ***/
if (!StringHelper.isTrue(request.getParameter("noinsert")) && !StringHelper.isTrue(request.getAttribute("noinsert"))) {
%>
<div class="insert-line" id="insert-line-<%=previousId%>">
	<a class="btn btn-default btn-xs ajax" onclick="scrollToFirstQuarter(jQuery('#content-edit'),this);" href="${info.currentURL}?${info.editPreview?'previewEdit=true&':''}webaction=insert&previous=<%=previousId%>&type=<%=currentTypeComponent.getType()%>"><%=insertHere%></a><%
	if (pastePageHere != null) {
	%><a class="btn btn-default btn-xs" onclick="scrollToFirstQuarter(jQuery('#content-edit'),this);" href="${info.currentURL}?${info.editPreview?'previewEdit=true&':''}webaction=pastePage&previous=<%=previousId%>"><%=pastePageHere%></a><%
	}
	if (pasteHere != null) {
	%><a class="btn btn-default btn-xs ajax" onclick="scrollToFirstQuarter(jQuery('#content-edit'),this);" href="${info.currentURL}?${info.editPreview?'previewEdit=true&':''}webaction=pasteComp&previous=<%=previousId%>"><%=pasteHere%></a><%
	}%>
</div>
<div id="comp-child-<%=previousId%>"></div>
<%
} else {%><div class="insert-line" id="insert-line-<%=previousId%>">&nbsp;</div><%}

}
if (components.size() == 0) {  /* if no specific components asked than render all components */
ComponentContext compCtx = ComponentContext.getInstance(request);
IContentComponentsList elems = ctx.getCurrentPage().getContent(ctx);
Collection<IContentVisualComponent> allComponents = new LinkedList<IContentVisualComponent>();
while (elems.hasNext(ctx)) {
	allComponents.add(elems.next(ctx));
}
components = allComponents;
} else { /* / if (components.length == 0) { */
	String previousId="0";
 	if (components.iterator().next().getPreviousComponent() != null) {
 		previousId=components.iterator().next().getPreviousComponent().getId();
 	}
	%><div class="new-component-container" id="comp-child-<%=previousId%>"></div><%
}

if (components.size() > 60 && request.getParameter("display-all") == null) {
	  %><div class="insert-line">
		<a class="btn btn-default btn-xs warning" href="${info.currentURL}?${info.editPreview?'previewEdit=true&':''}display-all=true">${i18n.edit["edit.message.display-all-components"]}</a>
	  </div><%	
} else {

for (IContentVisualComponent comp : components) {	 
	String inputSuffix = "-"+comp.getId();	
	if (comp instanceof IContainer && ((IContainer)comp).isOpen(ctx)) {
		closeContainerStack.push(((IContainer)comp).getCloseCode(ctx));
	     %><%=((IContainer)comp).getOpenCode(ctx)%><%
	
	}
	String readOnlyClass= "";
	String authors = "";
	if (!AdminUserSecurity.getInstance().canModifyConponent(ctx, comp.getId())) {
		readOnlyClass = "readonly ";
		authors = " <span class=\"authors\"> - "+comp.getAuthors()+"</span>";
	}
	%>
 <div id="comp-<%=comp.getId()%>" class="<%=readOnlyClass%><%=comp.getType()%><%if (components.size()==1) {%> new<%}%> component-wrapper">
 <input type="hidden" name="components" value="<%=comp.getId()%>" />
 <div class="component-title"><a style="color: #<%=comp.getHexColor()%>" href="#" onclick="scrollToFirstQuarter(jQuery('#content-edit'),this); return false;"><%=comp.getComponentLabel(ctx, globalContext.getEditLanguage(request.getSession()))+authors%></a></div>
 <div class="tabs component"> 	    	  
      <ul>      	  	
          <li><a href="#tab1<%=inputSuffix%>">${i18n.edit["global.content"]}</a></li>
          <%
          String linkTag = " href=\"#tab2"+inputSuffix+"\"";
          if (!comp.isConfig(ctx)) {
        	  linkTag = "";
          }
          String tabTitle = comp.getSpecialTagTitle(ctx);
          if (tabTitle != null) {
        	%><li><a href="#tab11<%=inputSuffix%>"><%=tabTitle%></a></li><% 
          }
          %>
          <li class="<%if (!comp.isConfig(ctx)) {%>disabled<%} else {%>enabled<%}%>"><a class="link"<%=linkTag%>>${i18n.edit["global.settings"]}</a></li>          
          <%if (admin) {%><li><a href="#tab4<%=inputSuffix%>">raw</a></li><%}%>
      </ul>
      <div class="header-action"><%
	     Map<String,String> params = new Hashtable<String,String>();
		 params.put("webaction", "edit.delete");
		 params.put("id", comp.getId());
      	 String actionURL = URLHelper.createURL(ctx, params);
      	 if (!ctx.isEditPreview() && AdminUserSecurity.getInstance().canModifyConponent(ctx, comp.getId())) {%><a class="delete ajax" title="${i18n.edit['global.delete']}" href="<%=actionURL%>"></a><%}
      	 if (!ctx.isEditPreview()) {%><a class="copy ajax" title="${i18n.edit['content.copy']}" href="${info.currentURL}?webaction=copy&id=<%=comp.getId()%>"></a><%}%>
      	 <%if(comp.isHelpURL(ctx)) {%><a class="help" title="help" target="_blanck" href="<%=comp.getHelpURL(ctx)%>"></a><%}%>
      </div>
      <div class="header-info">
      	<%
      	boolean sep=false;
      	if (comp.isRepeat()) {%><span class="repeat" title="${i18n.edit['content.repeat']}"></span><%}
      	if (comp.isList(ctx)) {%><span class="inlist" title="${i18n.edit['component.inlist']}"></span><%}      	
      	if (comp.getStyle(ctx) != null && comp.getStyle(ctx).trim().length() > 0) { sep=true;%><span class="style" title="<%=comp.getStyleTitle(ctx)%>"><%=comp.getStyleLabel(ctx)%></span><%}      	
      	if (comp.getCurrentRenderer(ctx) != null && comp.getCurrentRenderer(ctx).trim().length() > 0) {      		
      	%><%if (comp.getStyle(ctx) != null && comp.getStyle(ctx).trim().length() > 0) {sep=true;%><span class="sep">-</span><%}%><span class="renderer" title="<%=comp.getCurrentRenderer(ctx)%>"><%=comp.getCurrentRenderer(ctx)%></span><%}
      	if (comp.isRealContent(ctx)) {if(sep) {%><span class="sep">-</span><%}%><span class="realcontent" title="${i18n.edit['component.realcontent']}">${i18n.edit['component.realcontent']}</span><%}
      	%>
      </div>
      <div id="tab1<%=inputSuffix%>">
      	  <%if (comp.getContentMessage(ctx) != null) {%><div class="alert alert-<%=comp.getContentMessage(ctx).getBootstrapType()%>" role="alert"><%=comp.getContentMessage(ctx)%></div><%}
      	  %><%=comp.getXHTMLCode(ctx)%>
      </div><%
      if (tabTitle != null) {%>
      <div id="tab11<%=inputSuffix%>">
          <%if (comp.getTextMessage(ctx) != null) {%><div class="alert alert-<%=comp.getTextMessage(ctx).getBootstrapType()%>" role="alert"><%=comp.getTextMessage(ctx)%></div><%}
      	%><%=comp.getSpecialTagXHTML(ctx)%>
      </div><%
      }%>
      <div id="tab2<%=inputSuffix%>" class="config">
      	<%if (comp.getConfigMessage(ctx) != null) {%><div class="alert alert-<%=comp.getConfigMessage(ctx).getBootstrapType()%>" role="alert"><%=comp.getConfigMessage(ctx)%></div><%}
      	%><%=comp.getXHTMLConfig(ctx)%>
      </div>
      <%if (admin) {%>
      <div id="tab4<%=inputSuffix%>" class="help">
      	<textarea rows="5" cols="10" id="raw_value_<%=comp.getId()%>" name="" onchange="var item=jQuery('#raw_value_<%=comp.getId()%>'); item.attr('name', item.attr('id'));"><%=comp.getValue(ctx)%></textarea>
      </div><%}%>
      <input type="hidden" name="id-<%=comp.getId()%>" value="true" /> 
  </div><%
  if (!StringHelper.isTrue(request.getParameter("noinsert")) && !StringHelper.isTrue(request.getAttribute("noinsert")) && !StringHelper.isTrue(request.getAttribute("lightInterface"))) {%>  
  <div class="insert-line" id="insert-line-<%=comp.getId()%>">
  
	<a class="btn btn-default btn-xs ajax" onclick="scrollToFirstQuarter(jQuery('#content-edit'),this);" href="${info.currentURL}?${info.editPreview?'previewEdit=true&':''}webaction=insert&previous=<%=comp.getId()%>&type=<%=currentTypeComponent.getType()%>"><%=insertHere%></a><%
	if (pastePageHere != null) {
	%><a class="btn btn-default btn-xs" onclick="scrollToFirstQuarter(jQuery('#content-edit'),this);" href="${info.currentURL}?${info.editPreview?'previewEdit=true&':''}webaction=pastePage&previous=<%=comp.getId()%>"><%=pastePageHere%></a><%
	}
	if (pasteHere != null) {
	%><a class="btn btn-default btn-xs ajax" onclick="scrollToFirstQuarter(jQuery('#content-edit'),this);" href="${info.currentURL}?${info.editPreview?'previewEdit=true&':''}webaction=pasteComp&previous=<%=comp.getId()%>"><%=pasteHere%></a><%
	}%>
  </div>
 </div><%
 }
 if (comp instanceof IContainer && !((IContainer)comp).isOpen(ctx) && closeContainerStack.size() > 0) {
	 closeContainerStack.pop();
    %><%=((IContainer)comp).getCloseCode(ctx)%><%
}
%>
 <div class="new-component-container" id="comp-child-<%=comp.getId()%>"></div><%
  
  %>  
<%}
for(String closeCode : closeContainerStack) {
	%><%=closeCode%><%
}
}
%>