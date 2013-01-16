<%@page contentType="text/css"
        import="org.javlo.component.core.ICSS,
        org.javlo.component.core.ComponentFactory,
        org.javlo.component.core.IContentVisualComponent,
        org.javlo.context.GlobalContext"
%><%
GlobalContext globalContext = GlobalContext.getInstance(request);
IContentVisualComponent[] components = ComponentFactory.getComponents ( globalContext );
for ( int i=0; i<components.length; i++ ) {
	if ( components[i] instanceof ICSS ) {
		ICSS comp = (ICSS)components[i];
		%><%=comp.getCSSCode(application)%>
<%	}
}%>
	


