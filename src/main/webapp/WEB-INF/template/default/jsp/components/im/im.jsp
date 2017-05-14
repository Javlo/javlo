<%@page import="
java.util.List,
java.util.ArrayList,
java.util.Map,
java.util.LinkedHashMap,
java.security.Principal,
org.javlo.context.GlobalContext,
org.javlo.context.ContentContext,
org.javlo.data.InfoBean,
org.javlo.service.IMService,
org.javlo.service.IMService.IMItem,
org.javlo.helper.XHTMLHelper,
org.javlo.helper.StringHelper
"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%
GlobalContext globalContext = GlobalContext.getInstance(request);
IMService imService = IMService.getInstance(session);
ContentContext ctx = ContentContext.getContentContext(request, response);
String currentSite = globalContext.getContextKey();
String currentUser = ctx.getCurrentUserId();

List<Principal> list = globalContext.getAllPrincipals();
Map<String, Map<String, String>> users = new LinkedHashMap<String, Map<String, String>>();
for (Principal principal : list) {
	Map<String, String> user = new LinkedHashMap<String, String>();
	user.put("username", principal.getName());
	user.put("color", imService.getUserColor(currentSite, principal.getName()));
	users.put(principal.getName(), user);
}

String message = request.getParameter("message");
String receiver = request.getParameter("receiver");
if (message != null && !message.trim().isEmpty()) {
	message = XHTMLHelper.autoLink(XHTMLHelper.escapeXHTML(message));
	imService.appendMessage(currentSite, currentUser, currentSite, receiver, message);
}

Long lastMessageId = StringHelper.safeParseLong(request.getParameter("lastMessageId"), null);
boolean queryUnreadNumber = new Long(-1).equals(lastMessageId);
if (queryUnreadNumber) {
	lastMessageId = imService.getLastReadMessageId(currentSite, currentUser);
}
List<IMItem> messages = new ArrayList<IMItem>();
lastMessageId = imService.fillMessageList(currentSite, currentUser, lastMessageId, messages);
if (!queryUnreadNumber) {
	imService.setLastReadMessageId(currentSite, currentUser, lastMessageId);
}

request.setAttribute("currentUser", currentUser);
request.setAttribute("lastMessageId", lastMessageId);
request.setAttribute("imMessages", messages);
request.setAttribute("users", users);

InfoBean.updateInfoBean(ctx);
%>
<div class="messagelist">
	<h4>${i18n.edit['im.title']}</h4>
	<ul class="im-messages" style="min-height: 50px; max-height: 350px; overflow: auto;">
		<c:forEach var="message" items="${imMessages}">
			<li class="im-message ${message.wizz?'im-wizz':''}">
				<span class="user" style="color: ${users[message.fromUser].color};">${message.fromUser}</span>
				<c:if test="${(not empty message.receiverUser) && (message.receiverUser != '_ALL')}">
					<small class="to" data-user="${message.receiverUser}" style="color: ${users[message.receiverUser].color};">${message.receiverUser} ></small>
				</c:if>
				<div class="body"><small>${message.messageAutoLink}</small></div>
			</li>
		</c:forEach>
	</ul>
	<br />
	<form ajax-action="${info.templateBean.rootURL}/jsp/components/im/im.jsp" action="${info.currentURL}" class="im-form form-inline">
		<input type="hidden" name="lastMessageId" value="${lastMessageId}" />
		<div class="form-group">
			<select name="receiver" class="form-control">
				<option value="">-${i18n.edit['im.label.all-users']}-</option>
				<c:forEach var="entry" items="${users}">
					<c:if test="${entry.key != currentUser}">
						<option style="color: ${entry.value.color};" value="${entry.value.username}">${entry.value.username}</option>
					</c:if>
				</c:forEach>
			</select>
		</div>
		<div class="form-group">
		<input name="message" type="text" placeholder="${i18n.edit['im.label.message']}" class="form-control new-message" />
		</div><div class="form-group">
		&nbsp;
		<button type="submit" title="${i18n.edit['im.action.send']}" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-menu-right"></span></button><%
		if (globalContext.isWizz()) {
		%>		
		<button id="im-send-wizz" type="button" title="${i18n.edit['im.action.send-wizz']}" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-bell"></span></button><%
		}%>		
		</div>
	</form>
</div>
<script>
preview = ${contentContext.preview}
<c:if test="${contentContext.preview}">editPreview.onIMLoad();</c:if>
<c:if test="${!contentContext.preview}">onIMLoad();</c:if>
</script>