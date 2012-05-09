<%@page contentType="text/html"
        import="
        java.util.Stack,
        org.javlo.service.ClipBoard,
        org.javlo.context.ContentContext,
        org.javlo.context.EditContext,
        org.javlo.context.Content,	    
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
	    org.javlo.component.container.IContainer"
%><%
ContentContext ctx = ContentContext.getContentContext ( request, response );
Content content = Content.createContent(request);
GlobalContext globalContext = GlobalContext.getInstance(request);
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

ctx.setRenderMode(ContentContext.EDIT_MODE);
ctx.setArea(editContext.getCurrentArea());

I18nAccess i18nAccess = I18nAccess.getInstance ( globalContext, request.getSession() );
String insertLinkStr = request.getParameter("insert-link");
boolean insertLink = true;
if (insertLinkStr != null) {
	insertLink = StringHelper.isTrue(insertLinkStr);
}

ClipBoard clibBoard = ClipBoard.getClibBoard(request);

IContentVisualComponent currentTypeComponent = ComponentFactory.getComponentWithType(ctx, editContext.getActiveType());

String typeName = StringHelper.getFirstNotNull( currentTypeComponent.getComponentLabel(ctx,globalContext.getEditLanguage()), i18nAccess.getText ( "content."+currentTypeComponent.getType()));
String insertHere = i18nAccess.getText("content.insert-here", new String[][] {{"type",typeName}});

String ajaxLoaderMessage = "";
ajaxLoaderMessage = StringHelper.toJSString(ajaxLoaderMessage);

ComponentContext componentContext = ComponentContext.getInstance(request);
IContentVisualComponent[] components = componentContext.getNewComponents();

Stack<IContainer> containerStack = new Stack<IContainer>();

if (ComponentContext.getPreparedComponent(session) != null) {
	if (components.length == 0) {
		components = new IContentVisualComponent[] {ComponentContext.getPreparedComponent(session)};
	} else {
		ComponentContext.clearPreparedComponent(session);
	}
}


MenuElement currentPage = ctx.getCurrentPage();

String parentId = request.getParameter("number");
if (parentId == null) {	
	if ((components.length > 0)&&(components[0].previous() != null)) {
		parentId = components[0].previous().getId();
	} else {
		parentId="0";
	}
}

boolean inContainer = false;
/* check if component is in a container */
if (components.length > 0) {
	int countContainer = 0;
	IContentVisualComponent previous = components[0].previous();	
	while (previous != null) {
		if (previous.isContainer()) {
			countContainer++;
		}
		previous = previous.previous();
	}
	if (countContainer%2==1) {
		inContainer=true;
	}
}

%><%String newId = "lnk_"+StringHelper.getRandomId();

String ajaxCheckURL = URLHelper.addAllParams(URLHelper.createAjaxURL(ctx), "webaction=insertmsg", "number="+parentId);
String ajaxURL = URLHelper.addAllParams(URLHelper.createAjaxURL(ctx), "webaction=insert", "number="+parentId);

if (componentContext.isRenderLink()&&insertLink&&!currentPage.isRemote()) {
	String styleInsert = "";
	if (!currentTypeComponent.isInline()&&inContainer) {
		styleInsert="style=\"font-size: 0px; visibility: hidden;\"";
	}%>
<div id="<%=newId%>"><div id="insert-<%=parentId%>"><div class="insert"><span <%=styleInsert%> class="insert-component">
			<a class="edit" href="#" onclick="insertComponent('<%=newId%>', '<%=ajaxCheckURL%>','<%=ajaxURL%>', '<%=ajaxLoaderMessage%>');return false;"><%=insertHere%></a></span><%
				if ( !clibBoard.isEmpty(ctx) && ( !inContainer || (clibBoard.getCopiedComponent(ctx).isInline() ) ) ) {
					if ((clibBoard.getCopiedComponent(ctx) != null)) {
						String cutPasteLabel = i18nAccess.getText("content.paste")+" '"+i18nAccess.getText("content."+clibBoard.getCopiedComponent(ctx).getType(),clibBoard.getCopiedComponent(ctx).getType())+"'";
			%>&nbsp;&nbsp;&nbsp;<span class="cut-paste"><a class="edit" href="#" onclick="javascript:paste('<%=parentId%>', '');"><%=cutPasteLabel%></a></span><%
				}
				}
				if ( editContext.getContextForCopy() != null ) {
					String[][] tags = {{"path", copiedPath }};
					String textButton = i18nAccess.getText("action.paste-page",tags);
			%>&nbsp;&nbsp;&nbsp;<span class="cut-paste"><a href="#" onclick="javascript:pastePage('<%=parentId%>', '');"><%=textButton%></a></span><%
				}
			%>
		</div></div></div><%
			}
		for (int i=0; i<components.length; i++) {
			IContentVisualComponent elem = components[i];	
			boolean extractable = currentTypeComponent.isInsertable()&&elem.isExtractable();
			boolean closeContainer = false;
			if (inContainer && elem.isContainer() ) {
				closeContainer = true;
				inContainer = false;
			} else if (!inContainer && elem.isContainer() ) {
				inContainer = true;
			}
			
			if (!closeContainer) {
				String compId = "cp_"+elem.getId();
if (elem instanceof IContainer) {
	if (((IContainer)elem).isOpen(ctx)) {
		containerStack.push((IContainer)elem);
	}
	%><%=((IContainer)elem).getOpenCode(ctx)%><%
%><%
}%>
<div id="<%=compId%>" class="component_block">
<div class="component <%=elem.getType()%>">
		<div class="header" style="background-color: #<%=elem.getHexColor()%>;"><%
			String imageDown = "";
				  String imageUp = "";
				  if (insertLink) {
		%>
		  <div class="delete"><%
		  	imageDown = URLHelper.createStaticURL ( ctx, "/images/edit/del_component.gif" );
		  		  imageUp = URLHelper.createStaticURL ( ctx, "/images/edit/del_component_on.gif" );
		  		  String url = URLHelper.addAllParams(URLHelper.createAjaxURL(ctx), "webaction=remove&number="+elem.getId());
		  		  if (!currentPage.isRemote()) {
		  %><%=XHTMLHelper.getImageLink("delete", imageDown, imageUp, "javascript:deleteComponent ('"+elem.getId()+"', '"+compId+"', '"+url+"', '"+ajaxLoaderMessage+"');" )%><%
		  	} else {
		  %><%=XHTMLHelper.getIconesCode(ctx, "little_lock.png", "page locked.")%>		   
		   <%
		   		   	}
		   		   %></div><%
		   		   	}
		   		   %>
		   <div class="delete"><%
		   	if ((elem.getHelpURL(ctx, globalContext.getEditLanguage()) != null)&&(globalContext.isHelpLink())) {
		   	           imageDown = URLHelper.createStaticURL ( ctx, "/images/edit/help.png" );
		   	   imageUp = URLHelper.createStaticURL ( ctx, "/images/edit/help.png" );
		   %><%=XHTMLHelper.getImagePopupLink("help", imageDown, imageUp, elem.getHelpURL(ctx, globalContext.getEditLanguage()))%><%
		   	}
		   %>
		   </div><%
		   	if (extractable&&!globalContext.isEasy()&&!currentPage.isRemote()) {
		   %><div class="extract">
		   	<a href="#" onclick="if(extractContent('<%=compId%>', '<%=elem.getId()%>', '<%=ajaxURL%>', '<%=ajaxLoaderMessage%>', $('<%=elem.getContentName()%>'))){};return false;">[<%=i18nAccess.getText("content.extract")%>]</a>
		   	</div><%
		   		}
		   			   String compType = StringHelper.getFirstNotNull( components[i].getComponentLabel(ctx,globalContext.getEditLanguage()), i18nAccess.getText ( "content."+components[i].getType()));
		   	%><%--<span class="component-cursor"><%=XHTMLHelper.getIconesCode(ctx, "dragdrop.png", "drag&drop")%></span>--%><div class="text"><%=compType%>
		  <%
		  	if (!elem.isContainer()&&!currentPage.isRemote()) {
		  	   if (!globalContext.isEasy()&&!editContext.isLightInterface()) {
		  		   if (insertLink) {
		  %>
			    	   |<a href="javascript:copy ('<%=elem.getId()%>', '');"><%=XHTMLHelper.getIconesCode(ctx, "copy.png", i18nAccess.getText("content.copy"))%></a><%
		  	if (elem.isRepeatable()) {
		  		  		   if ( elem.isRepeat() ) {
		  %><a href="javascript:unrepeat ('<%=elem.getId()%>', '');"><%=XHTMLHelper.getIconesCode(ctx, "not-repeat.png", i18nAccess.getText("content.unrepeat"))%></a><%
		  	} else {
		  %><a href="javascript:repeat ('<%=elem.getId()%>', '');"><%=XHTMLHelper.getIconesCode(ctx, "repeat.png", i18nAccess.getText("content.repeat"))%></a><%
		  	}
		  	    	   }
		  		   }		    	   
		  	   }
		            }
		  %>
          </div><div class="input">
          <%=XHTMLHelper.getStyleComponentOneSelect(ctx, elem)%>
          <%=XHTMLHelper.getMarkerSelect(ctx, elem)%>
          <%
          	if (!inContainer) {
          %><%=XHTMLHelper.getListedSelection(ctx, elem)%><%
          	}
          %>
          </div>
		</div><%
			if (elem.isContainer()) {
		%><div class="container"><div class="content"><%
			out.flush();
		%><%=elem.getXHTMLCode(ctx)%><%
			} else {
		%>		
			<div class="content"><%
						out.flush();
					%>
			<input type="hidden" name="_comp_<%=elem.getId()%>" value="true" />
			<%=elem.getXHTMLCode(ctx)%></div><%
				}
			%><%=XHTMLHelper.renderMessage(ctx, elem.getMessage(), true)%><%
				}
				if (elem.isContainer()&&closeContainer) {
			%></div></div></div><%
				} else if (!elem.isContainer()) {
			%></div><%
				}
				String styleInsert = "";
				if (!currentTypeComponent.isInline()&&inContainer) {
					styleInsert="style=\"font-size: 0px; visibility: hidden;\"";
				}
				newId = StringHelper.getRandomId();
				ajaxCheckURL = URLHelper.addAllParams(URLHelper.createAjaxURL(ctx),"webaction=insertmsg","number="+parentId);
				ajaxURL = URLHelper.addAllParams(URLHelper.createAjaxURL(ctx),"webaction=insert","number="+elem.getId());
				//ajaxCheckURL = URLHelper.createAjaxURL(ctx)+"?webaction=insertmsg&number="+parentId;
				//ajaxURL = URLHelper.createAjaxURL(ctx)+"?webaction=insert&number="+elem.getId();
				if (insertLink&&!currentPage.isRemote()) {
			%>	
	<div id="<%=newId%>"><div id="insert-<%=elem.getId()%>"><div class="insert" ><span <%=styleInsert%> class="insert-component">
			<a class="edit" href="#" onclick="insertComponent('<%=newId%>', '<%=ajaxCheckURL%>', '<%=ajaxURL%>', '<%=ajaxLoaderMessage%>');return false;"><%=insertHere%></a></span>
			<%
			if ( !clibBoard.isEmpty(ctx) && ( !inContainer || (clibBoard.getCopiedComponent(ctx) != null) && (clibBoard.getCopiedComponent(ctx).isInline() ) ) ) {
				String cutPasteLabel = i18nAccess.getText("content.paste")+" '"+i18nAccess.getText("content."+clibBoard.getCopiedComponent(ctx).getType())+"'";
			%>
				&nbsp;&nbsp;&nbsp;<span class="cut-paste"><a class="edit" href="#" onclick="javascript:paste('<%=elem.getId()%>', '');"><%=cutPasteLabel%></a></span>
			<%}
			if ( editContext.getContextForCopy() != null ) {				
				String[][] tags = {{"path", copiedPath }};
				String textButton = i18nAccess.getText("action.paste-page",tags);
				%>&nbsp;&nbsp;&nbsp;<span class="cut-paste"><a href="#" onclick="javascript:pastePage('<%=elem.getId()%>', '');"><%=textButton%></a></span><%
			}
	%>
		</div></div></div><%		
	} else {
		%><span id="no-link-component"><br /></span><%
	}
	if (!closeContainer) {%>
	</div><%
	}

	
	if (elem instanceof IContainer) {
		if (!containerStack.empty()) {
			if (!((IContainer)elem).isOpen(ctx)) {
				containerStack.pop();
			}
			%><%=containerStack.size()%> <%=((IContainer)elem).getCloseCode(ctx)%><%
		}		
	%><%
	}
}		
while(!containerStack.empty()) {
	%><%=containerStack.pop().getCloseCode(ctx)%><%
}

%>