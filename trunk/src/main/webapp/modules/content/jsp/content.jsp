<%@page import="org.javlo.user.AdminUserSecurity"%>
<%@page contentType="text/html"
        import="
        java.util.Stack,
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
int totalComp = 0;

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
IContentVisualComponent[] components = componentContext.getNewComponents();

if (components.length == 0 || request.getParameter("firstLine") != null) { /* if no specific components asked than render all components for the current context or force first line */
String previousId = "0";
if (components.length > 0 && components[0].getPreviousComponent() != null) {
	previousId = components[0].getPreviousComponent().getId();
}
/*** rendering ***/

if (!StringHelper.isTrue(request.getParameter("noinsert")) && !StringHelper.isTrue(request.getAttribute("lightInterface"))) {
%>

<div class="insert-line" id="insert-line-<%=previousId%>">
	<a class="action-button ajax" href="${info.currentURL}?webaction=insert&previous=<%=previousId%>&type=<%=currentTypeComponent.getType()%>"><%=insertHere%></a><%
	if (pastePageHere != null) {
	%><a class="action-button" href="${info.currentURL}?webaction=pastePage&previous=<%=previousId%>"><%=pastePageHere%></a><%
	}
	if (pasteHere != null) {
	%><a class="action-button ajax" href="${info.currentURL}?webaction=pasteComp&previous=<%=previousId%>"><%=pasteHere%></a><%
	}%>
</div>
<div id="comp-child-<%=previousId%>"></div>
<%
} else {%><div class="insert-line" id="insert-line-<%=previousId%>">&nbsp;</div><%}

}
if (components.length == 0) {  /* if no specific components asked than render all components */
ComponentContext compCtx = ComponentContext.getInstance(request);
IContentComponentsList elems = ctx.getCurrentPage().getContent(ctx);
Collection<IContentVisualComponent> allComponents = new LinkedList<IContentVisualComponent>();
while (elems.hasNext(ctx)) {
	allComponents.add(elems.next(ctx));
}
components = allComponents.toArray(components);
} else { /* / if (components.length == 0) { */
	String previousId="0";
 	if (components[0].getPreviousComponent() != null) {
 		previousId=components[0].getPreviousComponent().getId();
 	}
	%><div class="new-component-container" id="comp-child-<%=previousId%>"></div><%
}

for (int i=0; i<components.length; i++) {
	IContentVisualComponent comp = components[i];

	totalComp++;
	String inputSuffix = "-"+comp.getId();
	String helpText = componentContext.getHelpHTML(ctx, comp);
	if (comp instanceof IContainer && ((IContainer)comp).isOpen(ctx)) {
		closeContainerStack.push(((IContainer)comp).getCloseCode(ctx));
	     %><%=((IContainer)comp).getOpenCode(ctx)%><%
	
	}%>
 <div id="comp-<%=comp.getId()%>" class="<%=comp.getType()%>">
 <input type="hidden" name="components" value="<%=comp.getId()%>" />
 <div class="tabs component">  	  
      <ul> 
      	  <li class="title"><span style="color: #<%=comp.getHexColor()%>"><%=comp.getComponentLabel(ctx, globalContext.getEditLanguage(request.getSession())) %></span></li>	
          <li><a href="#tab1<%=inputSuffix%>">${i18n.edit["global.content"]}</a></li>
          <li><a href="#tab2<%=inputSuffix%>">${i18n.edit["global.config"]}</a></li>          
          <%if (helpText != null) {%><li><a href="#tab3<%=inputSuffix%>">${i18n.edit["global.help"]}</a></li><%}%>
          <%if (admin) {%><li><a href="#tab4<%=inputSuffix%>">raw</a></li><%}%>
      </ul>
      <div class="header-action">
      	<a class="delete ajax" title="${i18n.edit['global.delete']}" href="${info.currentURL}?webaction=delete&id=<%=comp.getId()%>"></a>
      	<a class="copy ajax" title="${i18n.edit['content.copy']}" href="${info.currentURL}?webaction=copy&id=<%=comp.getId()%>"></a>
      </div>
      <div class="header-info">
      	<%if (comp.isRepeat()) {%><span class="repeat" title="${i18n.edit['content.repeat']}"></span><%}
      	if (comp.isList(ctx)) {%><span class="inlist" title="${i18n.edit['component.inlist']}"></span><%}
      	if (comp.getStyle(ctx) != null && comp.getStyle(ctx).trim().length() > 0) {%><span class="style" title="<%=comp.getStyleTitle(ctx)%>"><%=comp.getStyleLabel(ctx)%></span><%}
      	%>
      </div>
      <div id="tab1<%=inputSuffix%>">
          <%=comp.getXHTMLCode(ctx)%>
      </div>
      <div id="tab2<%=inputSuffix%>" class="config">
      	<%=comp.getXHTMLConfig(ctx)%>
      </div>
      <%if (helpText != null) {%>
      <div id="tab3<%=inputSuffix%>" class="help">
      	<%=helpText%>
      </div><%}%><%if (admin) {%>
      <div id="tab4<%=inputSuffix%>" class="help">
      	<textarea rows="5" cols="10" id="raw_value_<%=comp.getId()%>" name="" onchange="var item=jQuery('#raw_value_<%=comp.getId()%>'); item.attr('name', item.attr('id'));"><%=comp.getValue(ctx)%></textarea>
      </div><%}%>
      <input type="hidden" name="id-<%=comp.getId()%>" value="true" /> 
  </div><%
  if (!StringHelper.isTrue(request.getParameter("noinsert")) && !StringHelper.isTrue(request.getAttribute("lightInterface"))) {%>  
  <div class="insert-line" id="insert-line-<%=comp.getId()%>">
	<a class="action-button ajax" href="${info.currentURL}?webaction=insert&previous=<%=comp.getId()%>&type=<%=currentTypeComponent.getType()%>"><%=insertHere%></a><%
	if (pastePageHere != null) {
	%><a class="action-button" href="${info.currentURL}?webaction=pastePage&previous=<%=comp.getId()%>"><%=pastePageHere%></a><%
	}
	if (pasteHere != null) {
	%><a class="action-button ajax" href="${info.currentURL}?webaction=pasteComp&previous=<%=comp.getId()%>"><%=pasteHere%></a><%
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
  if (totalComp > 40 && request.getParameter("display-all") == null) {
  %>
  <div class="insert-line">
	<a class="action-button" href="${info.currentURL}?display-all=true">${i18n.edit["edit.message.display-all-components"]}</a>
  </div><%
  i = components.length; // break  
}

  %>  
<%}
for(String closeCode : closeContainerStack) {
	%><%=closeCode%><%
}
%>