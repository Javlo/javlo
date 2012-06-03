<%@page contentType="text/html"
        import="
        org.javlo.ContentManager,
        org.javlo.ContentContext,
        org.javlo.helper.URLHelper,
        org.javlo.helper.XHTMLHelper,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.FormComponent,
        org.javlo.user.IUserFactory,
        org.javlo.user.UserFactory,
        org.javlo.message.MessageRepository"
%><%
ContentContext ctx = ContentContext.getContentContext ( request, response );
IUserFactory fact = UserFactory.createUserFactory (request.getSession());
MessageRepository messages = MessageRepository.getInstance(ctx);
if (false) {
%><%if (messages.haveGlobalMessage()) {%><div class="message"><p class="<%=messages.getGlobalMessage().getTypeLabel()%>"><%=messages.getGlobalMessage()%></p></div><%}%>
<div id="form">
<form id="registration" action="" method="post">
<div><input type="hidden" name="webaction" value="mailing-registration.submit" /></div>

<div class="line">
<div class="label"><%=formComponent.getViewText("form.email")%> : *</div>
<div class="input"><input type="text" name="email" value="<%=formComponent.getValue ("email", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("email")%></div>
</div>

<div class="line">
<div class="label"><%=formComponent.getViewText("form.firstName")%> : </div>
<div class="input"><input type="text" name="firstname" value="<%=formComponent.getValue ("firstname", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("firstName")%></div>
</div>

<div class="line">
<div class="label"><%=formComponent.getViewText("form.lastName")%> : </div>
<div class="input"><input type="text" name="lastname" value="<%=formComponent.getValue ("lastname", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("lastName")%></div>
</div>
<div class="line">
<div class="label">(*<%=formComponent.getViewText("form.required")%>)</div>
</div>
<div class="footer">
<input type="submit" name="validation" value="<%=formComponent.getViewText("form.submit")%>" />
</div>
</form>
</div><%
} else {
%><div class="message"><p class="info"><%=formComponent.getViewText("form.message")%></p></div>

<%
}%>


