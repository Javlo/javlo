<%@page contentType="text/html"
        import="
        org.javlo.ContentManager,
        org.javlo.ContentContext,
        org.javlo.helper.URLHelper,
        org.javlo.helper.XHTMLHelper,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.FormComponent,
        org.javlo.user.UserFactory"
%><%
FormComponent formComponent = (FormComponent)AbstractVisualComponent.getRequestComponent ( request );
ContentContext ctx = ContentContext.getContentContext ( request, response );
UserFactory fact = UserFactory.createUserFactory (session);
if (fact.getCurrentUser() == null) {
%>
<div id="form">
<form id="registration" action="" method="post">
<div><%=formComponent.getSpecialTag()%></div>

<div class="line">
<div class="label"><%=formComponent.getViewText("form.email")%></div>
<div class="input"><input type="text" name="email" value="<%=formComponent.getValue ("email", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("email")%></div>
</div>

<div class="line">
<div class="label"><%=formComponent.getViewText("form.firstName")%></div>
<div class="input"><input type="text" name="firstName" value="<%=formComponent.getValue ("firstName", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("firstName")%></div>
</div>

<div class="line">
<div class="label"><%=formComponent.getViewText("form.lastName")%></div>
<div class="input"><input type="text" name="lastName" value="<%=formComponent.getValue ("lastName", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("lastName")%></div>
</div>

<div class="line">
<div class="label"><%=formComponent.getViewText("form.address")%></div>
<div class="input"><textarea name="address" rows="4" cols="60"><%=formComponent.getValue ("address", "" )%></textarea></div>
<div class="message"><%=formComponent.getErrorMessage("address")%></div>
</div>

<div class="content_clear"><span></span></div>

<div class="line">
<div class="label"><%=formComponent.getViewText("form.postcode")%></div>
<div class="input"><input type="text" name="postcode" value="<%=formComponent.getValue ("postcode", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("postcode")%></div>
</div>

<div class="line">
<div class="label"><%=formComponent.getViewText("form.city")%></div>
<div class="input"><input type="text" name="city" value="<%=formComponent.getValue ("city", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("city")%></div>
</div>

<div class="line">
<div class="select"><input type="checkbox" name="mailing"/><%=formComponent.getViewText("form.mailing")%></div>
</div>
		
<div class="footer">
<%=XHTMLHelper.getLinkSubmit("registration", formComponent.getViewText("form.submit") )%>
</div>
</form>
</div>
<%} else { /* user already loggged */%>
<%=formComponent.getViewText("form.registred")%>
<%}%>