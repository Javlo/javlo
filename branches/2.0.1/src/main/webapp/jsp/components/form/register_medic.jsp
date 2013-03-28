<%@page contentType="text/html"
        import="
        org.javlo.ContentManager,
        org.javlo.ContentContext,
        org.javlo.helper.URLHelper,
        org.javlo.I18nAccess,
        org.javlo.config.GlobalContext,
        org.javlo.helper.XHTMLHelper,
        org.javlo.GlobalMessage,
        org.javlo.GenericMessage,
        org.javlo.user.IUserFactory,
        org.javlo.message.MessageRepository,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.FormComponent,
        org.javlo.user.UserFactory"
%><%
FormComponent formComponent = (FormComponent)AbstractVisualComponent.getRequestComponent ( request );
ContentContext ctx = ContentContext.getContentContext ( request, response );
IUserFactory fact = UserFactory.createUserFactory(session);
I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
GlobalContext globalContext = GlobalContext.getInstance(request.getSession());
MessageRepository messageRepository = MessageRepository.getInstance(ctx);
String mailingSelected = "";
if (fact.getCurrentUser() != null) {
	messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("form.message.update"), GenericMessage.HELP));
	String[] roles = fact.getCurrentUser().getRoles();
	for (int i=0; i<roles.length; i++) {
		if (roles[i].equals("mailing") ) {
			mailingSelected=" checked=\"checked\"";
		}
	}
	
} else {
	messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("form.message.welcome"), GenericMessage.HELP));
}
%><%=XHTMLHelper.renderMessage(ctx, messageRepository.getGlobalMessage(), true)%>
<div id="form">
<form id="registration" action="" method="post">
<div><%=formComponent.getSpecialTag()%></div>

<fieldset>
<legend><%=i18nAccess.getViewText("form.title.account")%></legend><%
if (fact.getCurrentUser() == null) {%>
<div class="line">
<label for="email"><%=formComponent.getViewText("form.email")%>*</label>
<div class="input"><input id="email" type="text" name="email" value="<%=formComponent.getValue ("email", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("email")%></div>
</div>

<div class="line">
<label for="password"><%=formComponent.getViewText("form.password")%>*</label>
<div class="input"><input id="password" type="password" name="password" value=""/></div>
<div class="message"><%=formComponent.getErrorMessage("password")%></div>
</div>

<div class="line">
<label for="password2"><%=formComponent.getViewText("form.password2")%>*</label>
<div class="input"><input type="password" name="password2" id="password2" value=""/></div>
<div class="message"><%=formComponent.getErrorMessage("password2")%></div>
</div><%
} else {%>
<div class="line">
<%=formComponent.getViewText("form.email")%> : <%=fact.getCurrentUser().getLogin()%>
<input id="email" type="hidden" name="email" value="<%=fact.getCurrentUser().getLogin()%>"/>
</div>



<div class="line">
<label for="password"><%=i18nAccess.getViewText("form.new-password")%></label>
<div class="input"><input id="password" type="password" name="password" value=""/></div>
<div class="message"><%=formComponent.getErrorMessage("password")%></div>
</div>

<div class="line">
<label for="password2"><%=i18nAccess.getViewText("form.new-password2")%>*</label>
<div class="input"><input type="password" name="password2" id="password2" value=""/></div>
<div class="message"><%=formComponent.getErrorMessage("password2")%></div>
</div><%
}%>

</fieldset>
<fieldset>
<legend><%=i18nAccess.getViewText("form.title.user")%></legend>

<div class="line">
<label for="firstname"><%=formComponent.getViewText("form.firstName")%>*</label>
<div class="input"><input id="firstname" type="text" name="firstName" value="<%=formComponent.getValue ("firstName", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("firstName")%></div>
</div>

<div class="line">
<label for="lastname"><%=formComponent.getViewText("form.lastName")%>*</label>
<div class="input"><input id="lastname" type="text" name="lastName" value="<%=formComponent.getValue ("lastName", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("lastName")%></div>
</div>

<div class="line">
<label for="address"><%=formComponent.getViewText("form.address")%>*</label>
<div class="input"><textarea id="address" name="address"><%=formComponent.getValue ("address", "" )%></textarea></div>
<div class="message"><%=formComponent.getErrorMessage("address")%></div>
</div>

<div class="line">
<label for="postcode"><%=formComponent.getViewText("form.postcode")%>*</label>
<div class="input"><input id="postcode" type="text" name="postcode" value="<%=formComponent.getValue ("postcode", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("postcode")%></div>
</div>

<div class="line">
<label for="city"><%=formComponent.getViewText("form.city")%>*</label>
<div class="input"><input id="city" type="text" name="city" value="<%=formComponent.getValue ("city", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("city")%></div>
</div>

<div class="line">
<label for="country"><%=formComponent.getViewText("form.country")%>*</label>
<div class="input"><%=XHTMLHelper.getSelectOneCountry(ctx, "country", formComponent.getValue ("country", "BE" ))%></div>
<div class="message"><%=formComponent.getErrorMessage("country")%></div>
</div>

<div class="line">
<label for="phone"><%=formComponent.getViewText("form.phone")%></label>
<div class="input"><input id="phone" type="text" name="phone" value="<%=formComponent.getValue ("phone", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("phone")%></div>
</div>

<div class="line">
<label for="inami"><%=formComponent.getViewText("form.inami")%>*</label>
<div class="input"><input id="inami" type="text" name="inami" value="<%=formComponent.getValue ("inami", "" )%>"/></div>
<div class="message"><%=formComponent.getErrorMessage("inami")%></div>
</div>

</fieldset>

<div class="autorization">
<div class="select"><input<%=mailingSelected%> type="checkbox" id="mailing" name="mailing"/><label for="mailing"><%=i18nAccess.getViewText("form.mailing-legal", new String[][] {{"globaltitle", globalContext.getGlobalTitle()}})%></label></div>
</div>

<div class="footer"><%
if (fact.getCurrentUser() == null) {
%><%=XHTMLHelper.getLinkSubmit("registration", i18nAccess.getViewText("form.submit") )%><%
} else {
%><%=XHTMLHelper.getLinkSubmit("registration", i18nAccess.getViewText("form.update") )%><%
}%>
</div>
</form>
</div>