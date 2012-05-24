<%@page import="java.util.LinkedList"%>
<%@page import="java.util.Collection"%>
<%@page contentType="text/html"
        import="
        java.util.Stack,
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
GlobalContext globalContext = GlobalContext.getInstance(request);
ContentService content = ContentService.getInstance(globalContext);
EditContext editContext = EditContext.getInstance(globalContext, request.getSession());

String copiedPath = null;
if (editContext.getContextForCopy() != null) {
	MenuElement copiedPage = content.getNavigation(ctx).searchChild(ctx, editContext.getContextForCopy().getPath());
	if (copiedPage != null) { 
		copiedPath = copiedPage.getName()+" ("+editContext.getContextForCopy().getLanguage()+')';
	}
}

if (copiedPath == null) {
	copiedPath = "/";
}

int openCount = 0;
int totalComp = 0;

/** force edit mode in previe mode **/
ctx.setRenderMode(ContentContext.EDIT_MODE);
ctx.setArea(editContext.getCurrentArea());

I18nAccess i18nAccess = I18nAccess.getInstance ( globalContext, request.getSession() );

ClipBoard clibBoard = ClipBoard.getClibBoard(request);

IContentVisualComponent currentTypeComponent = ComponentFactory.getComponentWithType(ctx, editContext.getActiveType());

String typeName = StringHelper.getFirstNotNull( currentTypeComponent.getComponentLabel(ctx,globalContext.getEditLanguage()), i18nAccess.getText ( "content."+currentTypeComponent.getType()));
String insertHere = i18nAccess.getText("content.insert-here", new String[][] {{"type",typeName}});

ComponentContext componentContext = ComponentContext.getInstance(request);
IContentVisualComponent[] components = componentContext.getNewComponents();

if (components.length == 0) { /* if no specific components asked than render all components for the current context /*
/*** rendering ***/
%><div class="insert-line" id="insert-line-0">
	<a class="action-button ajax" href="${info.currentURL}?webaction=insert&previous=0&type=<%=currentTypeComponent.getType()%>"><%=insertHere%></a>
</div>
<div id="comp-child-0"></div>
<%
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
%>
 <div id="comp-<%=comp.getId()%>">
 <div class="tabs component">  	  
      <ul> 
      	  <li class="title"><span style="color: #<%=comp.getHexColor()%>"><%=comp.getComponentLabel(ctx, globalContext.getEditLanguage()) %></span></li>	
          <li><a href="#tab1<%=inputSuffix%>">${i18n.edit["global.content"]}</a></li>
          <li><a href="#tab2<%=inputSuffix%>">${i18n.edit["global.config"]}</a></li>          
          <%if (helpText != null) {%><li><a href="#tab3<%=inputSuffix%>">${i18n.edit["global.help"]}</a></li><%}%>
      </ul>
      <div class="header-action">
      	<a class="delete ajax" title="${i18n.edit['global.delete']}" href="${info.currentURL}?webaction=delete&id=<%=comp.getId()%>"></a>
      	<a class="copy" title="${i18n.edit['content.copy']}"></a>
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
      </div><%}%>
      <input type="hidden" name="id-<%=comp.getId()%>" value="true" /> 
  </div>
  <div class="insert-line" id="insert-line-<%=comp.getId()%>">
	<a class="action-button ajax" href="${info.currentURL}?webaction=insert&previous=<%=comp.getId()%>&type=<%=currentTypeComponent.getType()%>"><%=insertHere%></a>
  </div>
 </div>
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
%>

