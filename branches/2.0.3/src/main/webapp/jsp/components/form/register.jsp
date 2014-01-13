<%@page contentType="text/html"
        import="
        org.javlo.ContentManager,
        org.javlo.ContentContext,
        org.javlo.helper.URLHelper,
        org.javlo.helper.XHTMLHelper,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.FormComponent,
        org.javlo.user.UserFactory,
        org.javlo.user.IUserFactory"
%><%
FormComponent formComponent = (FormComponent)AbstractVisualComponent.getRequestComponent ( request );
ContentContext ctx = ContentContext.getContentContext ( request, response );
IUserFactory fact = UserFactory.createUserFactory (session);
%>
<form name="registration" class="form-register">
	<%=formComponent.getSpecialTag()%>
	<table>
		<tr>
			<td class="label"><%=formComponent.getViewText("form.login")%></td>
			<td class="in"><%
			if ( fact.getCurrentUser() == null ) {%>      
        <input type="text" name="login" value="<%=formComponent.getValue ("login", "" )%>"/>
      <%} else {%>
        <input type="hidden" name="login" value="<%=formComponent.getValue ("login", "" )%>"/>
        <%=formComponent.getValue ("login", "" )%>
      <%}
      %>
      </td>
			<td class="message">
        <%=formComponent.getErrorMessage("login")%>        
      </td>
		</tr>
		<tr>
			<td class="label"><%=formComponent.getViewText("form.password")%></td>
			<td class="in">
        <input type="password" name="password" value="<%=formComponent.getValue ("password", "" )%>"/>        
      </td>
			<td class="message"><%=formComponent.getErrorMessage("password")%></td>
		</tr>
		<tr>
			<td class="label"><%=formComponent.getViewText("form.password")%> (bis)</td>
			<td class="in"><input type="password" name="password2" value="<%=formComponent.getValue ("password2", "" )%>"/></td>
			<td class="message"><%=formComponent.getErrorMessage("password2")%></td>
		</tr>
		<tr>
			<td class="label"><%=formComponent.getViewText("form.email")%></td>
			<td class="in"><input type="text" name="email" value="<%=formComponent.getValue ("email", "" )%>" onkeyup="copyMail();"/></td>
			<td class="message"><%=formComponent.getErrorMessage("email")%></td>
		</tr>
		<tr>
			<td class="label"><%=formComponent.getViewText("form.firstName")%></td>
			<td class="in"><input type="text" name="firstName" value="<%=formComponent.getValue ("firstName", "" )%>"/></td>
			<td class="message"><%=formComponent.getErrorMessage("firstName")%></td>
		</tr>
		<tr>
			<td class="label"><%=formComponent.getViewText("form.lastName")%></td>
			<td class="in"><input type="text" name="lastName" value="<%=formComponent.getValue ("lastName", "" )%>"/></td>
			<td class="message"><%=formComponent.getErrorMessage("lastName")%></td>
		</tr>
		<tr><td class="footer" colspan="2">
				<%=XHTMLHelper.getLinkSubmit("registration", formComponent.getViewText("form.submit") )%></td>
			<td>&nbsp;</td>
		</tr>
	</table>
</form>
